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
import de.snlp.mp.text_model.Corefs;
import de.snlp.mp.text_model.TextModel;
import de.snlp.mp.text_model.Token;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class TextAnalyzer extends StanfordCoreNLP {

	private static DateFormat df = new SimpleDateFormat("HH:mm:ss");

	public static String corpusName = "Wikipedia Corpus Cutted";
	private static File corpusFolder;

	private static File factFolder = new File("FactRelatedTexts");

	/**
	 * Die Liste die die Namen aller abgearbeiteten Dokumente enthält.
	 */
	private static List<String> processedFactsList = new ArrayList<String>();

	/**
	 * Die Datei in der die Namen aller abgearbeiteten Dokumente gespeichert werden, damit das Programm zwischendruch gestoppt werden kann.
	 */
	private static final File processedFactsFile = new File("ProcessedFacts.txt");

	public static void main(String[] args) {

		if (args.length == 0) {
			log("No argument found. Use \"" + corpusName + "\" as corpus folder.");
		} else {
			corpusName = args[1];
		}
		corpusFolder = new File(corpusName);
		if (!corpusFolder.exists()) {
			log("Cannot find the folder: " + corpusFolder.getAbsolutePath());
			return;
		}

		if (!factFolder.exists())
			factFolder.mkdirs();

		// List<String> test = new ArrayList<String>();
		// test.add("Angela");
		// goThroughCorpus(new File(corpusName), new ArrayList<File>(), test);

		StanfordLib.initStandFordLib();
		List<Fact> facts = FactFileHandler.readFactsFromFile();
		readProcesseFacts();
		log(processedFactsList.size() + " out of " + facts.size() + " facts are already processed. ");

		log("This program should be stopped with: \"" + ExitThread.QUIT_COMMAND + "\".");
		log("DONOT use cmd + c, otherwise no calculations will be saved.");
		ExitThread exitThread = new ExitThread();
		exitThread.start();

		for (Fact f : facts) {
			String factId = String.valueOf(f.getFactId());
			if (!exitThread.isAlive()) {
				break;
			}
			if (!processedFactsList.contains(factId)) {
				log("Process fact: " + f.getFactId() + " - " + f.getFactStatement());
				createFactDirs(factId);
				String statement = f.getFactStatement().replaceAll("\\?", "");
				TextModel model = StanfordLib.getTextModel(statement);
				List<String> nouns = getNounsFromTextModel(model, f.getFactStatement());

				List<File> matches = new ArrayList<File>();
				goThroughCorpus(new File(corpusName), matches, nouns);
				addFilesToDir(factId, matches);
				processedFactsList.add(factId);

			}
		}

		writeListToFile(processedFactsFile, new ArrayList<Object>(processedFactsList), false);
		log("Finished program. The result is saved.");
		System.exit(0);
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
		for (Corefs c : model.getCorefsList()) {
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

		String n = "";
		for (String s : nouns)
			n += (s + " - ");
		log("Found following nouns: " + n);
		return nouns;
	}

	private static void goThroughCorpus(File f, List<File> matches, List<String> nouns) {
		if (f.isDirectory()) {
			for (File child : f.listFiles())
				goThroughCorpus(child, matches, nouns);
		} else {
			if (readFileContent(f).contains(nouns.get(0))) {
				matches.add(f);
			}
		}
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

	public static void log(String s) {
		System.out.println(df.format(new Date()) + " - " + s);
	}

}
