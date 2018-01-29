package de.snlp.mp.text_analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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

/**
 * This class analyzes the corpus. The first step for this is to extract all articles from the original corpus, which is created by the
 * wikiextractor script (https://github.com/attardi/wikiextractor). Afterwards it checks whether an article contains all the nouns (or the
 * corresponding synonyms) of one of the facts. In the case that all nouns or synonyms are part of an article, it will be copied to a
 * folder, which is named after the fact id. This program result in a folder with 1301 (the number of facts) subfolders, where each folder
 * can contain wikipedia articles according to the number of matches between the nouns and synonyms of the statement and the content of the
 * articles.
 * @author Daniel Possienke
 *
 */
public class TextAnalyzer extends StanfordCoreNLP {

	/**
	 * This variable defines whether additional output should be given.
	 */
	private static final boolean DEBUG = false;

	/**
	 * The default folder, where the corpus is saved.
	 */
	public static File corpus = new File("C:\\Wikipedia Corpus");

	/**
	 * The default result folder. In this folder will be a separate folder for each fact created, in which the corresponding articles will
	 * be copied.
	 */
	private static File factFolder = new File("F:\\FactRelatedTexts Test");

	/**
	 * The file, where the names of the processed files are saved.
	 */
	private static File processedFilesSave = new File("ProcessedFiles.txt");

	/**
	 * The number of files in the corpus.
	 */
	private static int fileCounter;

	/**
	 * The default minimum number of lines an article needs to be processed.
	 */
	private static int minTextLength = 5;

	/**
	 * The list, where the names of the processed files are saved.
	 */
	private static List<String> processedFiles = new ArrayList<String>();

	public static void main(String[] args) {
		// If no argument is given, use the default values.
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
		List<Fact> facts = FactFileHandler.readFactsFromFile(false);

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
		processCorpus(corpus, facts, pauseThread);

		if (processedFiles.size() < fileCounter)
			Utils.writeListToFile(processedFilesSave, processedFiles, false);
		Utils.log("Finished program with " + processedFiles.size() + " processed files.");
		System.exit(0);
	}

	/**
	 * This methods goes recursive through the corpus and process each files.
	 * @param f The current file.
	 * @param facts The list of facts.
	 * @param pauseThread The thread, which is needed to pause, continue and quit the process.
	 */
	private static void processCorpus(File f, List<Fact> facts, PauseThread pauseThread) {
		if (f.isDirectory()) {
			for (File child : f.listFiles()) {
				if (pauseThread.isAlive())
					processCorpus(child, facts, pauseThread);
			}
		} else {
			// Wait until the user continues or quit the thread
			while (pauseThread.isAlive() && !pauseThread.isRunning()) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// Is the current file already processed?
			if (!processedFiles.contains(f.getAbsolutePath())) {
				processedFiles.add(f.getAbsolutePath());
				List<Article> articleList = CorpusReader.readInput(f, minTextLength);
				// Go through the list of articles of a document.
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

	/**
	 * Create a directory where the matching articles for the fact should be saved.
	 * @param id The id of the fact.
	 */
	private static void createFactDirs(String id) {
		if (!factFolder.exists() || factFolder.isFile())
			factFolder.mkdirs();
		if (!new File(factFolder + "/" + id).exists() || new File(factFolder + "/" + id).isFile())
			new File(factFolder + "/" + id).mkdirs();
	}

	/**
	 * Copies an article to the folder where all articles for the corresponding fact are saved.
	 * @param id The id of the fact.
	 * @param a The article.
	 */
	private static void addFileToDir(String id, Article a) {
		File f = new File(factFolder + "/" + id + "/" + convertToWindowsFileNameRules(a.getName()) + ".txt");
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
			writer.write(a.getContent());
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Converts a file name to match the windows file name rules.
	 * @param name The file name.
	 * @return The file name according to the windows file name rules.
	 */
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

	/**
	 * Goes through the corpus and count the number of files.
	 * @param folder The corpus.
	 */
	private static void setFileCount(File folder) {
		for (File f : folder.listFiles()) {
			if (f.isDirectory())
				setFileCount(f);
			else {
				fileCounter++;

			}
		}
	}

	/**
	 * Reads the file where the processed documents are saved and writes them to the list "processedFiles"
	 */
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

}
