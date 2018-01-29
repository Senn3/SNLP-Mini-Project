package de.snlp.mp.text_analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.snlp.mp.text_model.Article;

/**
 * This class converts the wikipedia corpus, which is the result of the wikiextractor script (https://github.com/attardi/wikiextractor), to
 * a list of articles.
 * @author Daniel Possienke
 *
 */
public class CorpusReader {

	/**
	 * The start token, which is used to define where a new article starts.
	 */
	private static final String START_TOKEN = "<doc";

	/**
	 * The end token, which is used to define where an article ends.
	 */
	private static final String END_TOKEN = "</doc>";

	/**
	 * Converts a document to a list of articles.
	 * @param f The file, which should be converted.
	 * @param minTextLength The minimum number of lines an article needs to be part of the output list.
	 * @return The list of articles.
	 */
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

	/**
	 * This methods tests whether the given line contains a start token and is thus a start line.
	 * @param line The line which should be tested.
	 * @return Is the given line a start line?
	 */
	private static boolean isStartLine(String line) {
		if (line.length() >= START_TOKEN.length()) {
			if (line.substring(0, START_TOKEN.length()).equals(START_TOKEN)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This methods tests whether the given line contains a end token and is thus an end line.
	 * @param line The line which should be tested.
	 * @return Is the given line an end line?
	 */
	private static boolean isEndLine(String line) {
		if (line.length() >= END_TOKEN.length()) {
			if (line.substring(0, END_TOKEN.length()).equals(END_TOKEN)) {
				return true;
			}
		}
		return false;
	}

}
