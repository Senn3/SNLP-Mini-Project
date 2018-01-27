package de.snlp.mp.text_analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import de.snlp.mp.text_model.Article;
import de.snlp.mp.text_model.Fact;
import de.snlp.mp.text_model.TextModel;
import de.snlp.mp.utils.FactFileHandler;
import de.snlp.mp.utils.Utils;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class TextAnalyzer extends StanfordCoreNLP {

	private static final boolean DEBUG = false;

	public static File corpus = new File("C:\\Wikipedia Corpus");

	private static File factFolder = new File("F:\\FactRelatedTexts Train");

	private static File processedFilesSave = new File("ProcessedFiles.txt");

	private static int fileCounter;

	private static int minTextLength = 5;

	private static List<String> processedFiles = new ArrayList<String>();

	public static void main(String[] args) {
		if (args.length == 0) {
			Utils.log("No argument found. ");
			Utils.log("Use \"" + corpus.getAbsolutePath() + "\" as corpus folder.");
			Utils.log("Use \"" + factFolder.getAbsolutePath() + "\" as result folder.");
			Utils.log("Set minimum number of text lines to " + minTextLength + ".");
		} else if (args.length == 3) {
			corpus = new File(args[0]);
			factFolder = new File(args[1]);
			minTextLength = Integer.parseInt(args[2]);
		} else {
			Utils.log("The number of parameters have to be 3:");
			Utils.log("1. The corpus folder.");
			Utils.log("2. The folder in which the result should be saved.");
			Utils.log("3. The minimum number of lines a text needs.");
		}
		if (!corpus.exists()) {
			Utils.log("Cannot find the folder: " + corpus.getAbsolutePath());
			return;
		}

		if (!factFolder.exists())
			factFolder.mkdirs();

		StanfordLib stanfordLib = new StanfordLib();
		List<Fact> facts = FactFileHandler.readFactsFromFile(true);

		setFileCount(corpus);
		Utils.log("Found " + fileCounter + " files in the corpus to process.");
		readProcessedDocs();
		Utils.log("Already processed " + processedFiles.size() + " files.");

		Utils.log("The process can be quit with \"q\", paused with \"p\" and continued with \"c\".");
		PauseThread pauseThread = new PauseThread();
		pauseThread.start();

		Utils.log("Scan all facts and prepare the fact statements.");
		for (Fact f : facts) {
			createFactDirs(f.getFactId());
			String statement = f.getFactStatement();
			statement = Utils.replaceSpecialChars(statement);
			TextModel model = stanfordLib.getTextModel(statement);
			List<String> nouns = Utils.getNounsFromTextModel(model, f.getFactStatement());
			f.setWordsWithSynonyms(Utils.getSynonyms(nouns, POS.NOUN));
		}

		Utils.log("Start looking for fact statements in the corpus.");
		goThroughCorpus(corpus, facts, pauseThread);

		if (processedFiles.size() < fileCounter)
			writeListToFile(processedFilesSave, processedFiles, false);
		Utils.log("Finished program with "+processedFiles.size()+" processed files.");
		System.exit(0);
	}

	private static void goThroughCorpus(File f, List<Fact> facts, PauseThread pauseThread) {
		if (f.isDirectory()) {
			for (File child : f.listFiles()) {
				if (pauseThread.isAlive())
					goThroughCorpus(child, facts, pauseThread);
			}
		} else {
			while (pauseThread.isAlive() && !pauseThread.isRunning()) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (!processedFiles.contains(f.getAbsolutePath())) {
				processedFiles.add(f.getAbsolutePath());
				List<Article> articleList = CorpusReader.readInput(f, minTextLength);
				for (Article a : articleList) {
					String content = Utils.replaceSpecialChars(a.getContent().toLowerCase());
					for (Fact fact : facts) {
						if (Utils.textContainsWordList(content, fact.getWordsWithSynonyms(), DEBUG)) {
							addFileToDir(fact.getFactId(), a);
						}
					}
				}
			}
		}
	}

	private static void createFactDirs(String id) {
		if (!factFolder.exists() || factFolder.isFile())
			factFolder.mkdirs();
		if (!new File(factFolder + "/" + id).exists() || new File(factFolder + "/" + id).isFile())
			new File(factFolder + "/" + id).mkdirs();
	}

	private static void addFileToDir(String id, Article a) {
		File f = new File(factFolder + "/" + id + "/" + convertToWindowsFileNameRules(a.getName()) + ".txt");
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
			writer.write(a.getContent());
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String convertToWindowsFileNameRules(String name) {
		name = name.replace("<", " ");
		name = name.replace(">", " ");
		name = name.replace(":", " ");
		name = name.replace("\"", " ");
		name = name.replace("/", " ");
		name = name.replace("\\", " ");
		name = name.replace("|", " ");
		name = name.replace("?", " ");
		name = name.replace("*", " ");
		return name;
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

	private static void readProcessedDocs() {
		if (!processedFilesSave.exists())
			return;
		try (BufferedReader reader = new BufferedReader(new FileReader(processedFilesSave))) {
			String line = "";
			while ((line = reader.readLine()) != null) {
				if (!line.equals(""))
					processedFiles.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void writeListToFile(File f, List<String> list, boolean append) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, append))) {
			for (String s : list) {
				writer.write(s + "\n");
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
