package de.snlp.mp.fact_checking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import de.snlp.mp.text_analysis.StanfordLib;
import de.snlp.mp.text_analysis.Utils;
import edu.mit.jwi.item.POS;

public class FactChecker {

	private static final boolean DEBUG = true;
	
	private static final File pathToFactRelatedTexts = new File("FactRelatedTexts");

	private static final StanfordLib stanfordLib = new StanfordLib();

	private static SynonymDictionary synonymDictionary = new SynonymDictionary();

	public static void main(String[] args) {

		List<Fact> facts = FactFileHandler.readFactsFromFile();

		for (Fact f : facts) {
			String factStatement = f.getFactStatement();
			String factId = f.getFactId();
			if (DEBUG) {
				Utils.log("Processing fact: " + factStatement + ", " + factId);
			}
			
			List<String> nouns = Utils.getNounsFromTextModel(stanfordLib.getTextModel(factStatement), factStatement);
			List<String> verbs = Utils.getVerbsFromTextModel(stanfordLib.getTextModel(factStatement), factStatement);

			List<List<String>> synonyms = getSynonyms(nouns, POS.NOUN);
			synonyms.addAll(getSynonyms(verbs, POS.VERB));

			// files that contain the same words as the fact.
			File[] relatedFactFiles = getRelatedFactFiles(factId);

			if (relatedFactFiles.length > 0) {
				// lines from relatedFactFiles that match every word from the fact or their synonyms.
				List<String> matchingLines = new ArrayList<String>();
				for (File fi : relatedFactFiles) {
					for (String s : getMatchingLinesOfFile(fi, synonyms)) {
						if (!matchingLines.contains(s))
							matchingLines.add(s);
					}
				}
				
				if (isStatementPartOfMatchingLine(matchingLines, factStatement)) {
					f.setTruthvalue(1.0);
					if (DEBUG) {
						Utils.log("Statement is contained in matching line" + factStatement + ", " + factId);
					}
				}
			} else {
				if (DEBUG) {
					Utils.log("Fact statement has no related texts: " + factStatement + ", " + factId);
				}
				f.setTruthvalue(-1.0);
			}
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

	private static File[] getRelatedFactFiles(String id) {
		File relatedFactFilePath = new File(pathToFactRelatedTexts, id);
		return relatedFactFilePath.exists() ? relatedFactFilePath.listFiles() : null;
	}

	private static List<String> getMatchingLinesOfFile(File f, List<List<String>> synonyms) {
		List<String> lines = new ArrayList<String>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"))) {
			String line = "";
			while ((line = reader.readLine()) != null) {
				if (Utils.textContainsWordList(line.toLowerCase(), synonyms, false))
					lines.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lines;
	}
	
	private static boolean isStatementPartOfMatchingLine(List<String> matchingLines, String factStatement) {
		for (String s : matchingLines) {
			if (s.contains(factStatement)) {
				return true;
			}
		}
		return false;
	}
}
