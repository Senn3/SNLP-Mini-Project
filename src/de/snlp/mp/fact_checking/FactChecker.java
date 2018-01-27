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

	private static final File pathToFactRelatedTexts = new File("F:\\FactRelatedTexts Train");

	private static final StanfordLib stanfordLib = new StanfordLib();

	private static int rightPrognosis1 = 0;
	private static int wrongPrognosis1 = 0;

	private static int rightPrognosis2 = 0;
	private static int wrongPrognosis2 = 0;

	private static int rightPrognosis3 = 0;
	private static int wrongPrognosis3 = 0;

	public static void main(String[] args) {

		List<Fact> facts = FactFileHandler.readFactsFromFile(true);

		for (Fact f : facts) {
			String factStatement = Utils.replaceSpecialChars(f.getFactStatement());
			String factId = f.getFactId();
//			Utils.log(factId);

			List<String> nouns = Utils.getNounsFromTextModel(stanfordLib.getTextModel(factStatement), factStatement);
			List<String> verbs = Utils.getVerbsFromTextModel(stanfordLib.getTextModel(factStatement), factStatement);

			List<List<String>> synonyms = Utils.getSynonyms(nouns, POS.NOUN);
			// System.out.println(synonyms);
			synonyms.addAll(Utils.getSynonyms(verbs, POS.VERB));

			// files that contain the same words as the fact.
			File[] relatedFactFiles = getRelatedFactFiles(factId);

			if (relatedFactFiles != null && relatedFactFiles.length > 0) {
				// lines from relatedFactFiles that match every word from the fact or their synonyms.
				List<String> matchingLines = new ArrayList<String>();
				for (File fi : relatedFactFiles) {
					List<String> matchingLinesForFile = getMatchingLinesOfFile(fi, synonyms);
					if (matchingLinesForFile.size() != 0) {
						matchingLines.addAll(matchingLinesForFile);
					}
				}

				if (DEBUG) {
					if (matchingLines.size() == 0) {
						if (f.getTruthvalue() == 0)
							rightPrognosis1++;
						else
							wrongPrognosis1++;
					} else {
						if (f.getTruthvalue() == 1)
							rightPrognosis2++;
						else
							wrongPrognosis2++;
					}
				}
			} else {
				if (DEBUG) {
//					Utils.log("Fact statement has no related texts: " + factStatement + ", " + factId);
					if (f.getTruthvalue() == 0)
						rightPrognosis3++;
					else {
						wrongPrognosis3++;
						// Utils.log(factStatement+" "+synonyms);
					}
				}
				f.setTruthvalue(-1.0);
			}
		}

		if (DEBUG) {
			Utils.log("No matching line -> Right Prognosis: " + rightPrognosis1 + " - Wrong Prognosis: " + wrongPrognosis1);
			Utils.log("At least 1 matching line -> Right Prognosis: " + rightPrognosis2 + " - Wrong Prognosis: " + wrongPrognosis2);
			Utils.log("No files found -> Right Prognosis: " + rightPrognosis3 + " - Wrong Prognosis: " + wrongPrognosis3);
		}

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

}
