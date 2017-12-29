package de.snlp.mp.text_analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.snlp.mp.text_model.Dependencie;
import de.snlp.mp.text_model.Sentence;
import de.snlp.mp.text_model.TextModel;
import de.snlp.mp.text_model.Token;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class TextAnalyzer extends StanfordCoreNLP {

	private static String corpusName = "Wikipedia Corpus Cutted";
	private static File corpusFolder;

	/**
	 * Die Liste die die Namen aller abgearbeiteten Dokumente enthält.
	 */
	private static List<String> processedDocsList = new ArrayList<String>();

	/**
	 * Die Datei in der die Namen aller abgearbeiteten Dokumente gespeichert werden, damit das Programm zwischendruch gestoppt werden kann.
	 */
	private static final File processedDocsFile = new File("ProcessedDocuments.txt");

	private static StanfordCoreNLP pipeline;

	/**
	 * Die Anzahl der Dokumente im Korpus.
	 */
	private static int fileCounter;

	private static List<WordDependencie> wordDependencies = new ArrayList<WordDependencie>();
	private static final File analysedTextFile = new File("AnalysedText.txt");

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("No argument found. Use \"" + corpusName + "\" as corpus folder.");
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
		if (!processedDocsFile.exists()) {
			try {
				processedDocsFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		readProcessedDocs();
		readWordDepListToFile();

		ObjectMapper mapper = new ObjectMapper();

		System.out.println("This program should be stopped with: \"" + ExitThread.QUIT_COMMAND + "\".");
		System.out.println("DONOT use cmd + c,otherwise no calculations will be saved.");
		ExitThread exitThread = new ExitThread();
		exitThread.start();

		System.out.println("--------------------------------------------------");
		// Laufe solange, bis die Anzahl der bearbeiteten Artikel der Anzahl der .txt-Dateien im Korpus entspricht
		while (fileCounter > processedDocsList.size() && exitThread.isAlive()) {
			try {
				File f = getRawRandomFile(corpusFolder);
				System.out.println("Process: " + f.getAbsolutePath());

				String fileContent = readFile(f);
				String jsonString = getJsonFile(f.getAbsolutePath(), fileContent);

				// Erstellt aus dem json-String ein Objekt
				TextModel model = mapper.readValue(jsonString, TextModel.class);
				processTextModel(model);

				processedDocsList.add(f.getName());
				addProcessedDoc(f.getName());
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		}
		System.out.println("--------------------------------------------------");

		writeWordDepListToFile();
		System.out.println("Finished program. The result is saved to: \"" + analysedTextFile.getName() + "\"");
		System.exit(0);
	}

	/**
	 * Initialisiert die StanfordCoreNLP Bibliothek. Falls das Modell für die englische Sprache fehlt wird es das Programm beendet und eine
	 * entsprechende Nachricht ausgegeben.
	 */
	private static void initStandFordLib() {
		try {
			System.out.println("Initialze StanfordCoreNLP library.");
			// Entferne den Konsolenoutput von StanfordCoreNLP
			PrintStream err = System.err;
			System.err.close();
			Properties props = new Properties();
			props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, relation");
			pipeline = new StanfordCoreNLP(props);
			System.setErr(err);
			System.out.println("Finish initialization of the StanfordCoreNLP library.");
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
				if (!line.equals(""))
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

	private static void processTextModel(TextModel model) {
		for (Sentence s : model.getSentences()) {
			List<Token> token = s.getTokens();
			for (Dependencie d : s.getBasicDependencies()) {
				WordDependencie w = createWordDependencie(d, token);
				if (!isWordDepInList(w)) {
					wordDependencies.add(w);
				}
			}
		}
	}

	private static void readWordDepListToFile() {
		try (BufferedReader reader = new BufferedReader(new FileReader(analysedTextFile))) {
			String line = "";
			while ((line = reader.readLine()) != null) {
				if (!line.equals("")) {
					WordDependencie w = WordDependencie.convertToWordDep(line);
					if (w != null)
						wordDependencies.add(w);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static WordDependencie createWordDependencie(Dependencie d, List<Token> token) {
		WordDependencie w = new WordDependencie();
		w.setWord(d.getGovernorGloss());
		w.setWord2(d.getDependentGloss());
		w.setRelation(d.getDep());
		if (d.getDep().equals("ROOT")) {
			w.setType("ROOT");
		} else {
			w.setType(token.get(d.getGovernor()).getPos());
		}
		return w;
	}

	private static boolean isWordDepInList(WordDependencie entry) {
		for (WordDependencie e : wordDependencies) {
			if (e.getWord().equals(entry.getWord()) && e.getWord2().equals(entry.getWord2()) && e.getType().equals(entry.getType())
					&& e.getRelation().equals(entry.getRelation())) {
				e.increaseOccurrence();
				return true;
			}
		}
		return false;
	}

	private static void writeWordDepListToFile() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(analysedTextFile))) {
			for (WordDependencie e : wordDependencies) {
				writer.write(e.toString() + "\n");
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
