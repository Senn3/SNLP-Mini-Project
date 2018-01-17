package de.snlp.mp.fact_checking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.snlp.mp.text_analysis.StanfordLib;
import de.snlp.mp.text_model.Corefs;
import de.snlp.mp.text_model.TextModel;
import de.snlp.mp.text_model.Token;
import edu.mit.jwi.item.POS;

public class FactChecker {

	/*
	 * The name of the file, which contains the statement to proof.
	 */
	private static final String statementFile = "Wikipedia Corpus Cutted/train.txt";

	private static final File pathToFactRelatedTexts = new File("FactRelatedTexts");

	private static final StanfordLib stanfordLib = new StanfordLib();

	/*
	 * List that contains all verbs from the statement
	 */
	// private static List<String> verbs = new ArrayList<String>();
	//
	// /*
	// * List that contains all nouns from the statement
	// */
	// private static List<String> nouns = new ArrayList<String>();
	//
	// /*
	// * List that contains all names from the statement
	// */
	// private static List<String> names = new ArrayList<String>();

	/*
	 * List that contains all the words (nouns, names and verbs), that are part of the statement or a synonym of that, and which have been found
	 * within a wikipedia article in this exact combination.
	 */
	private static List<String> usedWords = new ArrayList<String>();

	private static SynonymDictionary synonymDictionary = new SynonymDictionary();

	public static void main(String[] args) {

		// TODO don't use stems of words to compare with text
		// List<List<String>> nounsWithSynonyms = getSynonyms(nouns, POS.NOUN);
		// List<List<String>> verbsWithSynonyms = getSynonyms(verbs, POS.VERB);
		//
		// System.out.println(nounsWithSynonyms);
		// System.out.println(verbsWithSynonyms);

		// TODO use actual wikipedia articles here
		// String wikiFile = "angela.txt";
		//
		// List<List<String>> wordsWithSynonyms = new ArrayList<List<String>>();
		// wordsWithSynonyms.addAll(nounsWithSynonyms);
		// wordsWithSynonyms.addAll(verbsWithSynonyms);
		// System.out.println(isStatementAsAWholeInFileUsingSynonyms(statement.toLowerCase(), wikiFile, wordsWithSynonyms, nounsWithSynonyms.get(0),
		// nounsWithSynonyms.size() + verbsWithSynonyms.size()));

		// for (String s : names) {
		// List<List<String>> list = new ArrayList<List<String>>();
		// list.add(new ArrayList<String>());
		// list.get(0).add(s);
		// wordsWithSynonyms.addAll(list);
		// }
		// System.out.println("Test: "
		// + getLineOfWordsOfStatementInFileUsingSynonyms(wordsWithSynonyms, wordsWithSynonyms.get(0), "", wikiFile, wordsWithSynonyms.size(),
		// wordsWithSynonyms.size()));
		// System.out.println(usedWords);

		// String test = "Angela Merkel is working as the chancellor of Germany and inhabit in a topographic place. We need to fill this string with
		// some nonesense in order to get better results.";
		// System.out.println(
		// "Bool: " + areElementsOfListInStringUsingSynonyms(wordsWithSynonyms, wordsWithSynonyms.get(0), "", test, wordsWithSynonyms.size(),
		// wordsWithSynonyms.size()));

		stanfordLib.initStandFordLib();

		List<Fact> facts = FactFileHandler.readFactsFromFile();

		for (Fact f : facts) {
			String factStatement = f.getFactStatement();
			List<String> nouns = getNounsFromTextModel(stanfordLib.getTextModel(factStatement), factStatement);
			List<String> verbs = getVerbsFromTextModel(stanfordLib.getTextModel(factStatement), factStatement);
			System.out.println(nouns);
			System.out.println(verbs);

			List<List<String>> synonyms = getSynonyms(nouns, POS.NOUN);
			synonyms.addAll(getSynonyms(verbs, POS.VERB));

			// files that contain the same words.
			File[] relatedFactFiles = getRelatedFactFiles(f.getFactId());

			if (relatedFactFiles != null) {
				List<String> matchingLines = new ArrayList<String>();
				for (File fi : getRelatedFactFiles(f.getFactId())) {
					matchingLines.addAll(getMatchingLinesOfFile(fi, synonyms));
				}
				System.out.println(matchingLines);
			} else
				f.setTruthvalue(-1.0);
		}

		FactFileHandler.writeFactsToFile(facts);
	}

	private static List<List<String>> getSynonyms(List<String> words, POS type) {
		List<List<String>> wordsWithSynonyms = new ArrayList<List<String>>();

		for (int i = 0; i < words.size(); i++) {
			wordsWithSynonyms.add(new ArrayList<String>());

			for (String st : synonymDictionary.getSynonyms(words.get(i), type))
				wordsWithSynonyms.get(i).add(st.replaceAll("[^a-zA-Z]", " "));

			if (wordsWithSynonyms.get(i).isEmpty())
				wordsWithSynonyms.get(i).add(words.get(i));
		}

		return wordsWithSynonyms;
	}

	private static String getLineOfWordsOfStatementInFileUsingSynonyms(List<List<String>> wordsWithSynonyms, List<String> words, String wordsToCheck, String fileName, int level,
			int maxLevel) {
		if (level == 0) {
			String line = getLineOfWords(wordsToCheck, fileName);

			System.out.println(line);
			if (!line.isEmpty())
				return line;
		} else {
			for (int i = 0; i < words.size(); i++) {
				if (level == maxLevel) {
					wordsToCheck = "";
					usedWords.clear();
				}

				if (i != 0 && level != maxLevel) {
					wordsToCheck = wordsToCheck.replaceAll(words.get(i - 1) + "[A-Za-z0-9]*,?", words.get(i)) + ",";
					Collections.replaceAll(usedWords, words.get(i - 1), words.get(i));

					for (int j = usedWords.size() - 1; j > level; j--)
						usedWords.remove(j);
				} else {
					wordsToCheck += words.get(i) + ",";
					usedWords.add(words.get(i));
				}

				String line = "";
				if (level != 1) {
					line = getLineOfWordsOfStatementInFileUsingSynonyms(wordsWithSynonyms, wordsWithSynonyms.get(wordsWithSynonyms.size() - level + 1), wordsToCheck, fileName,
							level - 1, maxLevel);
					if (!line.isEmpty())
						return line;
				} else {
					line = getLineOfWordsOfStatementInFileUsingSynonyms(wordsWithSynonyms, wordsWithSynonyms.get(0), wordsToCheck, fileName, level - 1, maxLevel);
					if (!line.isEmpty())
						return line;
				}
			}
		}
		return "";
	}

	private static String getLineOfWords(String wordsToCheck, String fileName) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String line = "";
			while ((line = in.readLine()) != null) {
				String[] words = wordsToCheck.split(",");
				boolean contained = true;

				for (String s : words) {
					if (!line.toLowerCase().contains(s.toLowerCase())) {
						contained = false;
						break;
					}
				}

				if (contained == true) {
					return line;
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Checks whether all words of a given list or replaced by any of their synonyms are contained inside of a given string.
	 * 
	 * @param wordsWithSynonyms
	 *            A list of lists of words, which shall be found within the string.
	 * @param words
	 *            Has to be the first list of words of wordsWithSynonyms.
	 * @param wordsToCheck
	 *            Contains the words that are checked in the current recursion. Has to be an empty string "".
	 * @param text
	 *            The text to look in.
	 * @param level
	 *            The level of the current recursion. Start at maxLevel.
	 * @param maxLevel
	 *            The maximum level of the recursion. Has to be the size of wordsWithSynonyms.
	 * @return
	 */
	public static boolean areElementsOfListInStringUsingSynonyms(List<List<String>> wordsWithSynonyms, List<String> words, String wordsToCheck, String text, int level,
			int maxLevel) {
		if (level == 0) {
			return areWordsInText(wordsToCheck, text);
		} else {
			for (int i = 0; i < words.size(); i++) {
				if (level == maxLevel)
					wordsToCheck = "";

				if (i != 0 && level != maxLevel)
					wordsToCheck = wordsToCheck.replaceAll(words.get(i - 1) + "[A-Za-z0-9]*,?", words.get(i)) + ",";
				else
					wordsToCheck += words.get(i) + ",";

				boolean isInText = false;
				if (level != 1) {
					isInText = areElementsOfListInStringUsingSynonyms(wordsWithSynonyms, wordsWithSynonyms.get(wordsWithSynonyms.size() - level + 1), wordsToCheck, text, level - 1,
							maxLevel);
					if (isInText)
						return true;
				} else {
					isInText = areElementsOfListInStringUsingSynonyms(wordsWithSynonyms, wordsWithSynonyms.get(0), wordsToCheck, text, level - 1, maxLevel);
					if (isInText)
						return true;
				}
			}
		}
		return false;
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
		return nouns;
	}

	private static List<String> getVerbsFromTextModel(TextModel model, String factStatement) {
		List<String> verbs = new ArrayList<String>();
		for (Token t : model.getSentences().get(0).getTokens()) {
			if (t.getPos().contains("VB") || t.getPos().contains("VBD") || t.getPos().contains("VBG") || t.getPos().contains("VBZ") || t.getPos().contains("VBN")
					|| t.getPos().contains("VBP")) {
				verbs.add(t.getOriginalText());
			}

		}
		return verbs;
	}

	private static boolean areWordsInText(String wordsToCheck, String text) {

		String[] words = wordsToCheck.split(",");
		boolean contained = true;

		for (String s : words) {
			if (!text.toLowerCase().contains(s.toLowerCase())) {
				contained = false;
				break;
			}
		}

		if (contained == true)
			return true;
		return false;
	}

	private static File[] getRelatedFactFiles(int id) {
		File relatedFactFilePath = new File(pathToFactRelatedTexts, String.valueOf(id));

		if (!relatedFactFilePath.exists())
			return null;

		return relatedFactFilePath.listFiles();
	}

	private static List<String> getMatchingLinesOfFile(File f, List<List<String>> synonyms) {
		boolean[] eachWordInList = new boolean[synonyms.size()];
		List<String> lines = new ArrayList<String>();
		try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
			String line = "";
			while ((line = reader.readLine()) != null) {
				Arrays.fill(eachWordInList, false);
				
				for (int i = 0; i < eachWordInList.length; i++) {
					for (String s : synonyms.get(i)) {
						if (line.contains(s))
							eachWordInList[i] = true;
					}
				}
				
				if (arrayIsTrue(eachWordInList))
					lines.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lines;
	}

	private static boolean arrayIsTrue(boolean[] a) {
		for (boolean value : a)
			if (!value)
				return false;
		return true;
	}
}
