package de.snlp.mp.text_analysis;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.snlp.mp.text_model.TextModel;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class StanfordLibThread extends Thread {

	private static StanfordCoreNLP pipeline;

	/**
	 * Initialisiert die StanfordCoreNLP Bibliothek. Falls das Modell für die englische Sprache fehlt wird es das Programm beendet und eine
	 * entsprechende Nachricht ausgegeben.
	 */
	public static void initStandFordLib() {
		try {
			TextAnalyzer.log("Initialze StanfordCoreNLP library.");
			Properties props = new Properties();
			props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, relation");
			pipeline = new StanfordCoreNLP(props);
			TextAnalyzer.log("Finish initialization of the StanfordCoreNLP library.");
		} catch (RuntimeIOException e) {
			TextAnalyzer.log("Can't find the english language model file.");
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
				TextAnalyzer.log("Download the model file for the english language.");
				URL url = new URL("https://nlp.stanford.edu/software/stanford-english-corenlp-2017-06-09-models.jar");
				FileUtils.copyURLToFile(url, file);
			}
			TextAnalyzer.log(
					"Download finish. Add the \\\"stanford-english-corenlp-models.jar\\\" file to the build path and/or refresh the project.");
		} catch (IOException e) {
			TextAnalyzer.log("Download failed. Exit program.");
			if (file.exists())
				file.delete();
			System.exit(255);
			e.printStackTrace();
		}
	}

	private int thread;
	private String fileName;
	private String content;

	public StanfordLibThread(int thread, String fileName, String content) {
		this.thread = thread;
		this.fileName = fileName;
		this.content = content;
	}

	@Override
	public void run() {
		try {
			TextAnalyzer.log("Thread: " + thread + " - Process: " + fileName);
			ObjectMapper mapper = new ObjectMapper();
			String json = getJsonFile();
			TextAnalyzer.setTextModelResult(thread, mapper.readValue(json, TextModel.class));
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getFileName() {
		return fileName;
	}

	/**
	 * Wendet die StanfordNLP an und erstellt aus dem Resulutat eine Json-String
	 * @param Der Name des Artikels, der für das Verwerfen von Fehlermeldungen gebraucht wird
	 * @param Der unbearbeitete Kontent des Artikels
	 * @return Der Json-String von dem Artikel
	 */
	private String getJsonFile() {
		try {
			Annotation annotation = new Annotation(content);
			Writer writer = new StringWriter();
			pipeline.annotate(annotation);
			pipeline.jsonPrint(annotation, writer);
			IOUtils.closeIgnoringExceptions(writer);
			return writer.toString();
		} catch (IOException e) {
			e.printStackTrace();
			TextAnalyzer.log("Error creating the json-file for the article: " + fileName);
			return null;
		}

	}
}
