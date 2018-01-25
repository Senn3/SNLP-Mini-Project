package de.snlp.mp.text_analysis;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.snlp.mp.text_model.CorefsHeader;
import de.snlp.mp.text_model.TextModel;
import de.snlp.mp.utils.Utils;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class StanfordLib {

	private StanfordCoreNLP pipeline;

	/**
	 * Initialisiert die StanfordCoreNLP Bibliothek. Falls das Modell für die englische Sprache fehlt wird das Programm beendet und eine
	 * entsprechende Nachricht ausgegeben.
	 */
	public StanfordLib() {
		try {
			Utils.log("Initialze StanfordCoreNLP library.");
			Properties props = new Properties();
			props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, relation");
			pipeline = new StanfordCoreNLP(props);
			Utils.log("Finish initialization of the StanfordCoreNLP library.");
		} catch (RuntimeIOException e) {
			Utils.log("Can't find the english language model file.");
			donwloadModel();
			System.exit(255);
		}
	}

	/**
	 * Lädt das Modell für die englische Sprache herunter und beendet das Programm, falls ein Fehler beim Download auftritt.
	 */
	private void donwloadModel() {
		File file = new File("stanford-english-corenlp-models.jar");
		try {
			if (!file.exists()) {
				Utils.log("Download the model file for the english language.");
				URL url = new URL("https://nlp.stanford.edu/software/stanford-english-corenlp-2017-06-09-models.jar");
				FileUtils.copyURLToFile(url, file);
			}
			Utils.log("Download finish. Add the \\\"stanford-english-corenlp-models.jar\\\" file to the build path and/or refresh the project.");
		} catch (IOException e) {
			Utils.log("Download failed. Exit program.");
			if (file.exists())
				file.delete();
			System.exit(255);
			e.printStackTrace();
		}
	}

	/**
	 * Startet den Prozess, indem die Datei durch die StanfordNLP Library in ein json-String umgewandelt wird und anschließend in ein
	 * TextModel-Objekt konvertiert wird.
	 */
	public TextModel getTextModel(String content) {
		try {

			if (pipeline == null) {
				Utils.log("The StandfordLibrary hasn't been initialized yet. Use the method \"initStandFordLib\" first.");
				return null;
			}
			ObjectMapper mapper = new ObjectMapper();
			String json = getJsonFile(content);
			json = json.replaceAll("\"corefs\": \\{", "\"corefs\": \\[{").substring(0, json.toCharArray().length - 2) + " }]}";
			CorefsHeader.clearCorefs();
//			System.out.println(json);

			return mapper.readValue(json, TextModel.class);
		} catch (IOException e) {
			e.printStackTrace();
			Utils.log("Error creating the text model for the content: " + content);
			return null;
		}
	}

	/**
	 * Wendet die StanfordNLP an und erstellt aus dem Resulutat eine Json-String
	 * 
	 * @param Der
	 *            Name des Artikels, der für das Verwerfen von Fehlermeldungen gebraucht wird
	 * @param Der
	 *            unbearbeitete Kontent des Artikels
	 * @return Der Json-String von dem Artikel
	 */
	private String getJsonFile(String content) {
		try {
			Annotation annotation = new Annotation(content);
			Writer writer = new StringWriter();
			pipeline.annotate(annotation);
			pipeline.jsonPrint(annotation, writer);
			IOUtils.closeIgnoringExceptions(writer);
			return writer.toString();
		} catch (IOException e) {
			e.printStackTrace();
			Utils.log("Error creating the json-file for the content: " + content);
			return null;
		}

	}
}
