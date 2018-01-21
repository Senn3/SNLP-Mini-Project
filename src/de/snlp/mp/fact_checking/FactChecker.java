package de.snlp.mp.fact_checking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import de.snlp.mp.text_analysis.StanfordLib;
import de.snlp.mp.text_model.Corefs;
import de.snlp.mp.text_model.TextModel;
import de.snlp.mp.text_model.Token;
import edu.mit.jwi.item.POS;

public class FactChecker {

	private static final File pathToFactRelatedTexts = new File("FactRelatedTexts");

	private static final StanfordLib stanfordLib = new StanfordLib();

	private static SynonymDictionary synonymDictionary = new SynonymDictionary();

	public static void main(String[] args) {

		List<Fact> facts = FactFileHandler.readFactsFromFile();

		for (Fact f : facts) {
			String factStatement = f.getFactStatement();
			List<String> nouns = getNounsFromTextModel(stanfordLib.getTextModel(factStatement), factStatement);
			List<String> verbs = getVerbsFromTextModel(stanfordLib.getTextModel(factStatement), factStatement);
			System.out.println(nouns);
			System.out.println(verbs);

			List<List<String>> synonyms = getSynonyms(nouns, POS.NOUN);
			synonyms.addAll(getSynonyms(verbs, POS.VERB));

			// files that contain the same words as the fact.
			File[] relatedFactFiles = getRelatedFactFiles(f.getFactId());

			if (relatedFactFiles != null) {
				// lines from relatedFactFiles that match every word from the fact or their synonyms.
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

	private static List<String> getNounsFromTextModel(TextModel model, String factStatement) {
		List<String> nouns = new ArrayList<String>();
		for (Corefs c : model.getCorefs()) {
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
			if (t.getPos().contains("VB") || t.getPos().contains("VBD") || t.getPos().contains("VBG") || t.getPos().contains("VBZ")
					|| t.getPos().contains("VBN") || t.getPos().contains("VBP")) {
				verbs.add(t.getOriginalText());
			}

		}
		return verbs;
	}

	private static File[] getRelatedFactFiles(String id) {
		File relatedFactFilePath = new File(pathToFactRelatedTexts, id);

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
