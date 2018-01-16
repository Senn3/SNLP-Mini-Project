package de.snlp.mp.text_extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import de.snlp.mp.text_analysis.TextAnalyzer;

public class TextExtractor {

	/*public static void main(String[] args) {
		long millis = System.currentTimeMillis();
		for (String s : getTextsByNoun(new String[] { "Angela Merkel", "Berlin" }))
			System.out.println(s);
		System.out.println("Finished after: " + (System.currentTimeMillis() - millis));
	}*/

	public static List<String> getTextsByNoun(String[] nouns) {
		List<String> matches = new ArrayList<String>();
		goThroughCorpus(new File(TextAnalyzer.corpusName), matches, nouns);
		return matches;
	}

	private static void goThroughCorpus(File f, List<String> matches, String[] nouns) {
		if (f.isDirectory()) {
			for (File child : f.listFiles())
				goThroughCorpus(child, matches, nouns);
		} else {
			boolean inText = true;
			for (String noun : nouns) {
				if (!readFileContent(f).contains(noun)) {
					inText = false;
				}
			}
			if (inText) {
				matches.add(f.getAbsolutePath());
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
}
