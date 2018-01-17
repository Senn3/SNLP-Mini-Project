package de.snlp.mp.text_analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.snlp.mp.fact_checking.Fact;
import de.snlp.mp.fact_checking.FactFileHandler;
import de.snlp.mp.fact_checking.SynonymDictionary;
import de.snlp.mp.text_model.Corefs;
import de.snlp.mp.text_model.TextModel;
import de.snlp.mp.text_model.Token;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class TextAnalyzer extends StanfordCoreNLP {

	private static final boolean DEBUG = true;

	private static DateFormat df = new SimpleDateFormat("HH:mm:ss");

	public static File corpus = new File("F://Wikipedia Corpus Cutted");

	private static File factFolder = new File("FactRelatedTexts");

	/**
	 * Die Liste die die Namen aller abgearbeiteten Dokumente enthält.
	 */
	private static List<String> processedFactsList = new ArrayList<String>();

	/**
	 * Die Datei in der die Namen aller abgearbeiteten Dokumente gespeichert werden, damit das Programm zwischendruch gestoppt werden kann.
	 */
	private static final File processedFactsFile = new File("ProcessedFacts.txt");

	private static SynonymDictionary synonymDictionary = new SynonymDictionary();

	private static int fileCounter;

	private static int currentfileCounter;

	private static int progress;

	public static void main(String[] args) {

		if (args.length == 0) {
			log("No argument found. Use \"" + corpus.getName() + "\" as corpus folder.");
		} else {
			corpus = new File(args[1]);
		}
		if (!corpus.exists()) {
			log("Cannot find the folder: " + corpus.getAbsolutePath());
			return;
		}

		if (!factFolder.exists())
			factFolder.mkdirs();

		// List<String> test = new ArrayList<String>();
		// test.add("Angela");
		// goThroughCorpus(new File(corpusName), new ArrayList<File>(), test);

		StanfordLib stanfordLib = new StanfordLib();
		List<Fact> facts = FactFileHandler.readFactsFromFile();
		readProcesseFacts();
		log(processedFactsList.size() + " out of " + facts.size() + " facts are already processed. ");

		setFileCount(corpus);
		log("Found " + fileCounter + " files in the corpus.");

		log("This program should be stopped with: \"" + ExitThread.QUIT_COMMAND + "\".");
		log("DONOT use cmd + c, otherwise no calculations will be saved.");
		ExitThread exitThread = new ExitThread();
		exitThread.start();

		for (Fact f : facts) {
			String factId = String.valueOf(f.getFactId());
			progress = 0;
			currentfileCounter = 0;
			if (!exitThread.isAlive()) {
				break;
			}
			if (!processedFactsList.contains(factId)) {
				log("Process fact: " + f.getFactId() + " - " + f.getFactStatement());
				createFactDirs(factId);
				String statement = f.getFactStatement().replaceAll("\\?", "");
				TextModel model = stanfordLib.getTextModel(statement);
				List<String> nouns = getNounsFromTextModel(model, f.getFactStatement());

				List<File> matches = new ArrayList<File>();
				goThroughCorpus(corpus, matches, getSynonyms(nouns, POS.NOUN));
				addFilesToDir(factId, matches);
				processedFactsList.add(factId);

			}
		}

		writeListToFile(processedFactsFile, new ArrayList<Object>(processedFactsList), false);
		log("Finished program. The result is saved.");
		System.exit(0);
	}

	private static List<List<String>> getSynonyms(List<String> words, POS type) {
		List<List<String>> wordsWithSynonyms = new ArrayList<List<String>>();
		for (int i = 0; i < words.size(); i++) {
			wordsWithSynonyms.add(new ArrayList<String>());

			for (String st : synonymDictionary.getSynonyms(words.get(i), type))
				wordsWithSynonyms.get(i).add(st.replaceAll("[^a-zA-Z]", " "));
			if (wordsWithSynonyms.get(i).size() == 0)
				wordsWithSynonyms.get(i).add(words.get(i));
		}
		if (DEBUG)
			printSynonymList(wordsWithSynonyms);
		return wordsWithSynonyms;
	}

	private static void printSynonymList(List<List<String>> wordsWithSynonyms) {
		String output = "";
		for (List<String> list : wordsWithSynonyms) {
			for (String word : list) {
				output += (word + " - ");
			}
		}
		output = output.substring(0, output.toCharArray().length - 3);
		log("Found following nouns: " + output);
	}

	/**
	 * Lädt die Datei, in der die Namen der bereits abgearbeiteten Artikel drin stehen und fügt die zur Liste 'processedDocsFile' hinzu.
	 */
	private static void readProcesseFacts() {
		if (!processedFactsFile.exists())
			return;
		try (BufferedReader reader = new BufferedReader(new FileReader(processedFactsFile))) {
			String line = "";
			while ((line = reader.readLine()) != null) {
				if (!line.equals(""))
					processedFactsList.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void writeListToFile(File f, List<Object> list, boolean append) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, append))) {
			for (Object o : list) {
				writer.write(o.toString() + "\n");
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void createFactDirs(String id) {
		if (!factFolder.exists() || factFolder.isFile())
			factFolder.mkdirs();
		if (!new File(factFolder + "/" + id).exists() || new File(factFolder + "/" + id).isFile())
			new File(factFolder + "/" + id).mkdirs();
	}

	private static List<String> getNounsFromTextModel(TextModel model, String factStatement) {
		List<String> nouns = new ArrayList<String>();
		for (Corefs c : model.getCorefs()) {
			if (c.getType().equals("PROPER")) {
				if (c.getText().contains(" , ")) {
					nouns.add(c.getText().split(" , ")[0]);
					nouns.add(c.getText().split(" , ")[1]);
				} else {
					nouns.add(c.getText().replaceAll(" 's", ""));
				}
			}

		}

		for (Token t : model.getSentences().get(0).getTokens()) {
			if (t.getPos().contains("NN") && !t.getPos().contains("P")) {
				nouns.add(t.getOriginalText());
			}

		}

		return nouns;
	}

	private static void goThroughCorpus(File f, List<File> matches, List<List<String>> synonyms) {
		if (f.isDirectory()) {
			for (File child : f.listFiles())
				goThroughCorpus(child, matches, synonyms);
		} else {
			String content = readFileContent(f);
			boolean[] eachWordInList = new boolean[synonyms.size()];
			for (int i = 0; i < eachWordInList.length; i++) {
				for (String s : synonyms.get(i)) {
					if (content.contains(s))
						eachWordInList[i] = true;
				}
			}

			if (arrayIsTrue(eachWordInList)) {
				if (DEBUG)
					log("Found file: " + f.getName());
				matches.add(f);
			}

			if (DEBUG) {
				currentfileCounter++;
				double v = (double) currentfileCounter / (double) fileCounter;
				for (int i = 1; i <= 20; i++) {
					if (v >= i * 0.05 && v < (i + 1) * 0.05 && i * 5 != progress) {
						progress = i * 5;
						if (progress == 100)
							System.out.println(progress + "%");
						else
							System.out.print(progress + "% - ");
					}
				}
			}
		}

	}

	private static boolean arrayIsTrue(boolean[] a) {
		for (boolean value : a)
			if (!value)
				return false;
		return true;
	}

	private static String readFileContent(File f) {
		try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
			String line = "";
			StringBuilder builder = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				builder.append(line + "\n");
			}
			return builder.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	private static void addFilesToDir(String id, List<File> matches) {
		for (File f : matches) {
			try {
				FileUtils.copyFile(f, new File(factFolder + "/" + id + "/" + f.getName()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void setFileCount(File folder) {
		for (File f : folder.listFiles()) {
			if (f.isDirectory())
				setFileCount(f);
			else if (f.getName().endsWith(".txt")) {
				fileCounter++;

			}
		}
	}

	public static void log(String s) {
		System.out.println(df.format(new Date()) + " - " + s);
	}

}
