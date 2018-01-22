package de.snlp.mp.text_analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

	private static final boolean DEBUG = false;

	private static DateFormat df = new SimpleDateFormat("HH:mm:ss");

	public static File corpus = new File("C:\\Wikipedia Corpus Splitted");
	// public static File corpus = new File("Test");

	private static File factFolder = new File("FactRelatedTexts");

	private static SynonymDictionary synonymDictionary = new SynonymDictionary();

	private static int fileCounter;

	private static int removedFiles = 0;

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

		StanfordLib stanfordLib = new StanfordLib();
		List<Fact> facts = FactFileHandler.readFactsFromFile();

		setFileCount(corpus);
		log("Found " + fileCounter + " files in the corpus to process.");

		log("This program should be stopped with: \"" + ExitThread.QUIT_COMMAND + "\".");
		log("DONOT use cmd + c, otherwise no calculations will be saved.");
		ExitThread exitThread = new ExitThread();
		exitThread.start();

		log("Scan all facts and prepare the fact statements.");
		for (Fact f : facts) {
			createFactDirs(f.getFactId());
			String statement = f.getFactStatement().replaceAll("\\?", "");
			TextModel model = stanfordLib.getTextModel(statement);
			List<String> nouns = getNounsFromTextModel(model, f.getFactStatement());
			f.setWordsWithSynonyms(getSynonyms(nouns, POS.NOUN));
		}

		log("Start looking for fact statements in the corpus.");
		goThroughCorpus(corpus, facts, exitThread);

		// removeEmptyFolder(corpus);
		log("Finished program. Removed " + removedFiles + " articles from the corpus.");
		System.exit(0);
	}

	private static void goThroughCorpus(File f, List<Fact> facts, ExitThread exitThread) {
		if (f.isDirectory()) {
			for (File child : f.listFiles()) {
				if (exitThread.isAlive()) {
					goThroughCorpus(child, facts, exitThread);
				}
			}
		} else {
			String content = readFileContent(f).toLowerCase();
			for (Fact fact : facts) {
				boolean wordIsInStatement = true;
				String match = "";
				Statement: for (int i = 0; i < fact.getWordsWithSynonyms().size(); i++) {
					boolean wordIsInContent = false;
					Synonyms: for (String s : fact.getWordsWithSynonyms().get(i)) {
						if (textContainsWord(content, s.toLowerCase())) {
							wordIsInContent = true;
							match += (s + " | ");
							break Synonyms;
						}
					}
					if (!wordIsInContent) {
						wordIsInStatement = false;
						break Statement;
					}
				}

				if (wordIsInStatement) {
					// log("Match: \"" + f.getName() + "\" - \"" + fact.getFactStatement() + "\" - " + match);
					addFileToDir(fact.getFactId(), f);
				}
			}
			if (!DEBUG) {
				if (!f.delete())
					log("The file \"" + f.getAbsolutePath() + "\" could not be deleted!");
				else
					removedFiles++;
			}
		}
	}

	private static boolean textContainsWord(String text, String word) {
		if (text.contains(" " + word) || text.contains("-" + word))
			return true;
		if (text.contains("\n" + word))
			return true;

		if (text.toCharArray().length >= word.toCharArray().length) {
			String firstWordInText = "";
			for (int i = 0; i < word.toCharArray().length; i++) {
				firstWordInText += text.charAt(i);
			}
			if (firstWordInText.equals(word))
				return true;
		}
		return false;
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
			output += " | ";
		}
		output = output.substring(0, output.toCharArray().length - 3);
		log("Found following nouns: " + output);
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

	private static String readFileContent(File f) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"))) {
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

	private static void addFileToDir(String id, File f) {
		try {
			FileUtils.copyFile(f, new File(factFolder + "/" + id + "/" + f.getName()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void setFileCount(File folder) {
		for (File f : folder.listFiles()) {
			if (f.isDirectory())
				setFileCount(f);
			else {
				fileCounter++;

			}
		}
	}

	private static void removeEmptyFolder(File folder) {
		for (File f : folder.listFiles()) {
			if (f != null && f.isDirectory() && f.listFiles().length == 0) {
				f.delete();
			} else {
				removeEmptyFolder(f);
			}
		}
	}

	public static void log(String s) {
		System.out.println(df.format(new Date()) + " - " + s);
	}

}
