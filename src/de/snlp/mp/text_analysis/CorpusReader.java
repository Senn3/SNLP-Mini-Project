package de.snlp.mp.text_analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.snlp.mp.text_model.Article;

public class CorpusReader {

	private static final String START_TOKEN = "<doc";
	private static final String END_TOKEN = "</doc>";

	public static List<Article> readInput(File f, int minTextLength) {
		List<Article> articleList = new ArrayList<Article>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"))) {
			String line;
			List<String> content = new ArrayList<String>();
			String name = "";
			boolean readContent = false;
			while ((line = reader.readLine()) != null) {
				if (!line.equals("")) {
					if (isStartLine(line)) {
						if (readContent) {
							System.out.println("\nMissing end line: " + line + " in document: " + f.getAbsolutePath());
							name = "";
							content.clear();
						}

						do {
							name = reader.readLine();
						} while (name.equals(""));
						readContent = true;
					} else if (isEndLine(line)) {
						if (readContent) {
							readContent = false;
							if (content.size() >= minTextLength)
								articleList.add(new Article(name, content));
						} else {
							System.out.println("\nMissing start line: " + line + " in document: " + f.getAbsolutePath());
						}
						name = "";
						content.clear();
					} else if (readContent) {
						content.add(line + "\n");
					} else {
						name = "";
						content.clear();
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return articleList;

	}

	private static boolean isStartLine(String line) {
		if (line.length() >= START_TOKEN.length()) {
			if (line.substring(0, START_TOKEN.length()).equals(START_TOKEN)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isEndLine(String line) {
		if (line.length() >= END_TOKEN.length()) {
			if (line.substring(0, END_TOKEN.length()).equals(END_TOKEN)) {
				return true;
			}
		}
		return false;
	}

}
