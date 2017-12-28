package de.snlp.mp.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.snlp.mp.model.TextModel;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Main extends StanfordCoreNLP {

	private static String corpusName = "Wikipedia Corpus";
	private static File corpusFolder;

	private static List<String> processedDocsList = new ArrayList<String>();
	private static final File processedDocsFile = new File("processedDocs.txt");

	private static StanfordCoreNLP pipeline;

	private static int fileCounter;

	/*
	 * Wichtige Links: https://stanfordnlp.github.io/CoreNLP/ http://universaldependencies.org/introduction.html
	 */

	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			System.out.println("No argument found. Use \"Wikipedia Corpus\" as corpus folder.");
		} else {
			corpusName = args[1];
		}
		corpusFolder = new File(corpusName);
		if (!corpusFolder.exists()) {
			System.out.println("Cannot find the folder: " + corpusFolder.getAbsolutePath());
			return;
		}

		initStandFordLib();
		setFileCount(corpusFolder);
		if (!processedDocsFile.exists())
			processedDocsFile.createNewFile();
		readProcessedDocs();

		ObjectMapper mapper = new ObjectMapper();

		// Laufe solange, bis die Anzahl der bearbeiteten Artikel der Anzahl der .txt-Dateien im Korpus entspricht
		while (fileCounter > processedDocsList.size()) {
			File f = getRawRandomFile(corpusFolder);

			String fileContent = readFile(f);
			String jsonString = getJsonFile(f.getAbsolutePath(), fileContent);

			// Erstellt aus dem json-String ein Objekt
			TextModel model = mapper.readValue(jsonString, TextModel.class);

			processedDocsList.add(f.getName());
			// Für Testzwecke wurde das Hinzufügen zu der Liste auskommentiert.
			// addProcessedDoc(f.getName());
		}

	}

	/**
	 * Initialisiert die StanfordCoreNLP Bibliothek. Falls das Modell für die englische Sprache fehlt wird es das Programm beendet und eine
	 * entsprechende Nachricht ausgegeben.
	 */
	private static void initStandFordLib() {
		try {
			Properties props = new Properties();
			props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, relation");
			pipeline = new StanfordCoreNLP(props);
		} catch (RuntimeIOException e) {
			System.out.println("Can't find the english language model file.");
			donwloadModel();
			System.exit(255);
		}
	}

	/**
	 * Lädt das Modell für die englische Sprache herunter und beendet das Programm falls ein Fehler beim Download auftritt.
	 */
	private static void donwloadModel() {
		File file = new File("stanford-english-corenlp-models.jar");
		try {
			if (!file.exists()) {
				System.out.println("Download the model file for the english language.");
				URL url = new URL("https://nlp.stanford.edu/software/stanford-english-corenlp-2017-06-09-models.jar");
				FileUtils.copyURLToFile(url, file);
			}
			System.out.println(
					"Download finish. Add the \\\"stanford-english-corenlp-models.jar\\\" file to the build path and/or refresh the project.");
		} catch (IOException e) {
			System.out.println("Download failed. Exit program.");
			if (file.exists())
				file.delete();
			System.exit(255);
			e.printStackTrace();
		}
	}

	/**
	 * Lädt die Datei, in der die Namen der bereits abgearbeiteten Artikel drin stehen und fügt die zur Liste 'processedDocsFile' hinzu.
	 */
	private static void readProcessedDocs() {
		try (BufferedReader reader = new BufferedReader(new FileReader(processedDocsFile))) {
			String line = "";
			while ((line = reader.readLine()) != null) {
				processedDocsList.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fügt eine neuen Namen zur Liste der bereits abgearbeiteten Artikel hinzu.
	 * @param Name des neuen abgearbeiteten Artikels
	 */
	private static void addProcessedDoc(String name) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(processedDocsFile, true))) {
			writer.write(name + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Zählt die Anzahl der .txt-Dateien. Die Anzahl wird gebraucht, damit das Programm weiß, wann es mit der Bearbeitung fertig ist, da die
	 * Artikel zufällig und nicht chronologisch bearbeitet werden.
	 * @param Der Ordner mit dem Korpus
	 */
	private static void setFileCount(File folder) {
		for (File f : folder.listFiles()) {
			if (f.isDirectory())
				setFileCount(f);
			else if (f.getName().endsWith(".txt")) {
				fileCounter++;
			}
		}
	}

	/**
	 * Sucht rekursiv zufällig einen Artikel heraus, der noch nicht bearbeitet wurde, also noch nicht in der Liste vorhanden ist.
	 * @param Der Ordner mit dem Korpus
	 * @return Unbearbeiteter Artikel
	 */
	private static File getRawRandomFile(File folder) {
		int rnd = (int) (Math.random() * folder.listFiles().length);
		if (folder.listFiles()[rnd].isDirectory()) {
			return getRawRandomFile(folder.listFiles()[rnd]);
		} else {
			if (processedDocsList.contains(folder.listFiles()[rnd].getName())) {
				return getRawRandomFile(corpusFolder);
			} else {
				return folder.listFiles()[rnd];
			}
		}
	}

	/**
	 * Liest den Inhalt einer .txt-Datei ein und gibt diesen zurück
	 * @param Der Inhalt der Datei
	 */
	private static String readFile(File f) {
		try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
			String line = "";
			StringBuilder builder = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				builder.append(line + "\n");
			}
			return builder.toString();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error reading the content of the article: " + f.getAbsolutePath());
			return null;
		}
	}

	/**
	 * Wendet die StanfordNLP an und erstellt aus dem Resulutat eine Json-String
	 * @param Der Name des Artikels, der für das Verwerfen von Fehlermeldungen gebraucht wird
	 * @param Der unbearbeitete Kontent des Artikels
	 * @return Der Json-String von dem Artikel
	 */
	private static String getJsonFile(String fileName, String content) {
		try {
			Annotation annotation = new Annotation(content);
			Writer writer = new StringWriter();
			pipeline.annotate(annotation);
			pipeline.jsonPrint(annotation, writer);
			IOUtils.closeIgnoringExceptions(writer);
			return writer.toString();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error creating the json-file for the article: " + fileName);
			return null;
		}
	}

}
