package de.snlp.mp.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Main extends StanfordCoreNLP {

	private static final String corpus = "Wikipedia Corpus";

	private static StanfordCoreNLP pipeline;

	/*
	 * Wichtige Links: https://stanfordnlp.github.io/CoreNLP/ http://universaldependencies.org/introduction.html
	 */

	public static void main(String[] args) throws IOException {
		File folder = new File(corpus);
		if (!folder.exists()) {
			System.out.println("Cannot find the folder: " + folder.getAbsolutePath());
			return;
		}
		donwloadModel();
		initStandFordLib();
		goThroughCorpus(folder);
	}

	private static void donwloadModel() {
		File file = new File("stanford-english-corenlp-models.jar");
		try {
			if (!file.exists()) {
				System.out.println("Download the model file for the english language.");
				URL url = new URL("https://nlp.stanford.edu/software/stanford-english-corenlp-2017-06-09-models.jar");
				FileUtils.copyURLToFile(url, file);
				System.out.println("Download finish. Add the .jar file to the build path.");
				System.exit(1);
			}
		} catch (IOException e) {
			System.out.println("Download failed. Exit program.");
			if (file.exists())
				file.delete();
			System.exit(255);
			e.printStackTrace();
		}
	}

	private static void initStandFordLib() {
		try {
			Properties props = new Properties();
			props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, relation");
			pipeline = new StanfordCoreNLP(props);
		} catch (RuntimeIOException e) {
			System.out.println("Can't find the model file. Add the \"stanford-english-corenlp-models.jar\" file to the build path and refresh the project.");
			System.exit(255);
		}
	}

	private static void goThroughCorpus(File folder) {
		for (File f : folder.listFiles()) {
			if (f.isDirectory()) {
				goThroughCorpus(f);
			} else {
				readFile(f);
			}
		}
	}

	private static void readFile(File f) {
		try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
			String line = "";
			StringBuilder builder = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				builder.append(line + "\n");
			}
			createXMLFile(f, builder.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void createXMLFile(File f, String content) throws IOException {
		PrintWriter xmlOut = new PrintWriter(f.getName().replaceAll(".txt", ".xml"));
		Annotation annotation = new Annotation(content);
		pipeline.annotate(annotation);
		pipeline.xmlPrint(annotation, xmlOut);
		IOUtils.closeIgnoringExceptions(xmlOut);
	}

}
