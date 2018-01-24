package de.snlp.mp.text_analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.snlp.mp.fact_checking.Fact;
import de.snlp.mp.fact_checking.FactFileHandler;
import de.snlp.mp.fact_checking.SynonymDictionary;
import de.snlp.mp.text_model.TextModel;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class TextAnalyzer extends StanfordCoreNLP {

	private static final boolean DEBUG = false;

	public static File corpus = new File("C:\\Wikipedia Corpus Splitted");

	private static File factFolder = new File("FactRelatedTexts");

	private static SynonymDictionary synonymDictionary = new SynonymDictionary();

	private static int fileCounter;

	private static int removedFiles = 0;

	public static void main(String[] args) {
		if (args.length == 0) {
			Utils.log("No argument found. Use \"" + corpus.getName() + "\" as corpus folder.");
		} else {
			corpus = new File(args[1]);
		}
		if (!corpus.exists()) {
			Utils.log("Cannot find the folder: " + corpus.getAbsolutePath());
			return;
		}

		if (!factFolder.exists())
			factFolder.mkdirs();

		StanfordLib stanfordLib = new StanfordLib();
		List<Fact> facts = FactFileHandler.readFactsFromFile();

		setFileCount(corpus);
		Utils.log("Found " + fileCounter + " files in the corpus to process.");

		Utils.log("This program should be stopped with: \"" + ExitThread.QUIT_COMMAND + "\".");
		Utils.log("DONOT use cmd + c, otherwise no calculations will be saved.");
		ExitThread exitThread = new ExitThread();
		exitThread.start();

		Utils.log("Scan all facts and prepare the fact statements.");
		for (Fact f : facts) {
			createFactDirs(f.getFactId());
			String statement = f.getFactStatement().replaceAll("\\?", "");
			TextModel model = stanfordLib.getTextModel(statement);
			List<String> nouns = Utils.getNounsFromTextModel(model, f.getFactStatement());
			f.setWordsWithSynonyms(getSynonyms(nouns, POS.NOUN));
		}

		Utils.log("Start looking for fact statements in the corpus.");
		goThroughCorpus(corpus, facts, exitThread);

		Utils.log("Finished program. Removed " + removedFiles + " articles from the corpus.");
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
						if (Utils.textContainsWord(content, s.toLowerCase())) {
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
					Utils.log("The file \"" + f.getAbsolutePath() + "\" could not be deleted!");
				else
					removedFiles++;
			}
		}
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
		Utils.log("Found following nouns: " + output);
	}

	private static void createFactDirs(String id) {
		if (!factFolder.exists() || factFolder.isFile())
			factFolder.mkdirs();
		if (!new File(factFolder + "/" + id).exists() || new File(factFolder + "/" + id).isFile())
			new File(factFolder + "/" + id).mkdirs();
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

	

}
