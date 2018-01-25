package de.snlp.mp.fact_checking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.snlp.mp.text_analysis.StanfordLib;
import de.snlp.mp.text_model.Fact;
import de.snlp.mp.utils.FactFileHandler;
import de.snlp.mp.utils.Utils;
import edu.mit.jwi.item.POS;

public class FactChecker {

	private static final boolean DEBUG = true;

	private static final File pathToFactRelatedTexts = new File("FactRelatedTexts");

	private static final StanfordLib stanfordLib = new StanfordLib();

	private static int rightPrognosis = 0;
	private static int wrongPrognosis = 0;

	public static void main(String[] args) {

		List<Fact> facts = FactFileHandler.readFactsFromFile(true);

		for (Fact f : facts) {
			String factStatement = Utils.replaceSpecialChars(f.getFactStatement());
			String factId = f.getFactId();
			if (DEBUG) {
				// Utils.log("Processing fact: " + factStatement + ", " + factId);
			}

			List<String> nouns = Utils.getNounsFromTextModel(stanfordLib.getTextModel(factStatement), factStatement);
			List<String> verbs = Utils.getVerbsFromTextModel(stanfordLib.getTextModel(factStatement), factStatement);

			// TODO add stuff like this for more words? e.g. better half
			// add 'bear' to verbs if birth place or nascence place is used in statement
			if (nouns.contains("birth") || nouns.contains("nascence"))
				verbs.add("bear");

			List<List<String>> synonyms = Utils.getSynonyms(nouns, POS.NOUN);
			synonyms.addAll(Utils.getSynonyms(verbs, POS.VERB));

			// files that contain the same words as the fact.
			File[] relatedFactFiles = getRelatedFactFiles(factId);

			if (relatedFactFiles != null && relatedFactFiles.length > 0) {
				// lines from relatedFactFiles that match every word from the fact or their synonyms.
				List<String> matchingLines = new ArrayList<String>();
				for (File fi : relatedFactFiles) {
					List<String> matchingLinesForFile = getMatchingLinesOfFile(fi, synonyms);
					if (matchingLinesForFile.size() != 0) {
						if (DEBUG) {
							Utils.log("Found " + matchingLinesForFile.size() + " matching line(s) in text \"" + fi.getName());
						}
						matchingLines.addAll(matchingLinesForFile);
					}
				}

				if (isStatementPartOfMatchingLine(matchingLines, factStatement)) {
					f.setTruthvalue(1.0);
					if (DEBUG) {
						Utils.log("Statement is contained in matching line" + factStatement + ", " + factId);
						if (f.getTruthvalue() == 1)
							rightPrognosis++;
						else
							wrongPrognosis++;
					}
				} else {
					if (DEBUG) {
						if (matchingLines.size() == 0) {
							if (f.getTruthvalue() == 0)
								rightPrognosis++;
							else
								wrongPrognosis++;
						} else {
							if (f.getTruthvalue() == 1)
								rightPrognosis++;
							else
								wrongPrognosis++;
						}
					}
				}
			} else {
				if (DEBUG) {
					// Utils.log("Fact statement has no related texts: " + factStatement + ", " + factId);
					if (f.getTruthvalue() == 0)
						rightPrognosis++;
					else
						wrongPrognosis++;
				}
				f.setTruthvalue(-1.0);
			}
		}

		if (DEBUG)
			Utils.log("Right Prognosis: " + rightPrognosis + " - Wrong Prognosis: " + wrongPrognosis);

		FactFileHandler.writeFactsToFile(facts);
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
				line = Utils.replaceSpecialChars(line).toLowerCase();
				if (Utils.textContainsWordList(line, synonyms, false))
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
