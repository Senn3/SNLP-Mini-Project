package de.snlp.mp.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Main extends StanfordCoreNLP {

	private static final String corpus = "Wikipedia Corpus";

	private static StanfordCoreNLP pipeline;
	
	/*
	 * Wichtige Links:
	 * https://stanfordnlp.github.io/CoreNLP/
	 * http://universaldependencies.org/introduction.html
	 */

	public static void main(String[] args) throws IOException {
		File folder = new File(corpus);
		if (!folder.exists()) {
			System.out.println("Cannot find the folder: " + folder.getAbsolutePath());
			return;
		}
		initStandFordLib();
		goThroughCorpus(folder);
	}

	private static void initStandFordLib() {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, relation");
		pipeline = new StanfordCoreNLP(props);
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
