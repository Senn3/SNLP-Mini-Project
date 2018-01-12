package de.snlp.mp.fact_checking;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import edu.mit.jwi.item.POS;

public class FactChecker {

	/*
	 * The name of the file, which contains the statement to proof.
	 */
	private static final String statementFile = "Wikipedia Corpus Cutted/train.txt";
	
	/*
	 * The name of the file, which contains the analyzed statement.
	 */
	private static final String analyzedStatementFile = "AnalysedText.txt";

	/*
	 * List that contains all verbs from the statement
	 */
	private static List<String> verbs = new ArrayList<String>();

	/*
	 * List that contains all nouns from the statement
	 */
	private static List<String> nouns = new ArrayList<String>();

	/*
	 * List that contains all names from the statement
	 */
	private static List<String> names = new ArrayList<String>();

	public static void main(String[] args) {
		String statement = getStatement();
		getWordsFromStatement();

		System.out.println(verbs);
		System.out.println(nouns);
		System.out.println(names);

		List<List<String>> nounsWithSynonyms = getSynonyms(nouns, POS.NOUN);
		List<List<String>> verbsWithSynonyms = getSynonyms(verbs, POS.VERB);

		System.out.println(nounsWithSynonyms);
		System.out.println(verbsWithSynonyms);
		
		//TODO use actual wikipedia articles here
		String wikiFile = "angela.txt";
		System.out.println(isStatementAsAWholeInFile(statement, wikiFile));
	}

	/*
	 * Gets verbs and nouns from analyzed facts Could cause problems if executed on more than one fact at a time.
	 */
	private static void getWordsFromStatement() {
		try {
			// Read in analyzed fact
			BufferedReader in = new BufferedReader(new FileReader(analyzedStatementFile));
			String line = "", previous = "", type = "";
			int numNouns = 0, numVerbs = 0, numNames = 0;
			boolean start = true, compoundSeen = false;
			String[] compound = new String[10];

			while ((line = in.readLine()) != null) {
				String[] wordsFromCurrentLine = line.split(" ");

				if (nouns.size() - numNouns > 1)
					numNouns++;
				if (verbs.size() - numVerbs > 1)
					numVerbs++;
				if (names.size() - numNames > 1)
					numNames++;

				if ((!previous.equals(wordsFromCurrentLine[1]) || (!wordsFromCurrentLine[2].equals("compound") && compoundSeen)) && !start
						&& compound[0] != null) {
					addCompoundWordToList(compound, type, numNouns, numVerbs, numNames);
					Arrays.fill(compound, null);
					compoundSeen = false;

					switch (type) {
					case "NN":
						numNouns++;
						break;

					case "VB":
						numVerbs++;
						break;

					case "NNP":
						numNames++;
						break;
					}
				}
				type = getWordTypeAndSaveWordToList(wordsFromCurrentLine, numNouns, numVerbs, numNames);

				if (wordsFromCurrentLine[2].equals("compound")) {
					compound = getCompoundWordArray(wordsFromCurrentLine, compound);
					compoundSeen = true;
				}
				start = false;
				previous = wordsFromCurrentLine[1];
			}
			in.close();

			// add last compound word, happens if compound is in last line of file
			if (compound[0] != null) {
				addCompoundWordToList(compound, type, numNouns, numVerbs, numNames);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void addCompoundWordToList(String[] compound, String type, int numNouns, int numVerbs, int numNames) {
		String compoundWord = "";
		for (int i = compound.length - 1; i >= 0; i--) {
			if (compound[i] != null) {
				compoundWord = compound[i] + " ";
			} else
				continue;
			
			switch (type) {
			case "NN":
				nouns.set(numNouns, compoundWord + nouns.get(numNouns));
				break;

			case "VB":
				verbs.set(numVerbs, compoundWord + verbs.get(numVerbs));
				break;

			case "NNP":
				names.set(numNames, compoundWord + names.get(numNames));
				break;
			}
		}
	}

	private static String getWordTypeAndSaveWordToList(String[] wordsFromCurrentLine, int numNouns, int numVerbs, int numNames) {
		if (wordsFromCurrentLine[0].equals("NN") || wordsFromCurrentLine[0].equals("NNS")) {
			if (!nouns.contains(wordsFromCurrentLine[1]))
				if (numNouns > 0) {
					if (!nouns.get(numNouns - 1).contains(wordsFromCurrentLine[1]))
						nouns.add(wordsFromCurrentLine[1]);
				} else
					nouns.add(wordsFromCurrentLine[1]);
			return "NN";
		} else if (wordsFromCurrentLine[0].equals("VB") || wordsFromCurrentLine[0].equals("VBD") || wordsFromCurrentLine[0].equals("VBG")
				|| wordsFromCurrentLine[0].equals("VBN") || wordsFromCurrentLine[0].equals("VBP") || wordsFromCurrentLine[0].equals("VBZ")) {
			if (!verbs.contains(wordsFromCurrentLine[1]))
				if (numVerbs > 0) {
					if (!verbs.get(numVerbs - 1).contains(wordsFromCurrentLine[1]))
						verbs.add(wordsFromCurrentLine[1]);
				} else
					verbs.add(wordsFromCurrentLine[1]);
			return "VB";
		} else if (wordsFromCurrentLine[0].equals("NNP") || wordsFromCurrentLine[0].equals("NNPS")) {
			if (!names.contains(wordsFromCurrentLine[1]))
				if (numNames > 0) {
					if (!names.get(numNames - 1).contains(wordsFromCurrentLine[1]))
						names.add(wordsFromCurrentLine[1]);
				} else
					names.add(wordsFromCurrentLine[1]);
			return "NNP";
		}
		return "";
	}

	private static String[] getCompoundWordArray(String[] wordsFromCurrentLine, String[] compound) {
		for (int i = 0; i < compound.length; i++) {
			if (compound[i] == null) {
				compound[i] = wordsFromCurrentLine[3];
				break;
			}
		}
		return compound;
	}

	private static List<List<String>> getSynonyms(List<String> words, POS type) {
		SynonymDictionary synonymDictionary = new SynonymDictionary();
		List<List<String>> wordsWithSynonyms = new ArrayList<List<String>>();

		for (int i = 0; i < words.size(); i++) {
			wordsWithSynonyms.add(new ArrayList<String>());

			for (String st : synonymDictionary.getSynonyms(words.get(i), type))
				wordsWithSynonyms.get(i).add(st.replaceAll("[^a-zA-Z]", " "));
			System.out.println();
		}

		return wordsWithSynonyms;
	}
	
	private static String getStatement() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(statementFile));
			String line = "";
			if ((line = in.readLine()) != null) {
				in.close();
				return line;
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private static boolean isStatementAsAWholeInFile(String statement, String fileName) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String line = "";
			while ((line = in.readLine()) != null) {
				if(line.contains(statement))
					in.close();
					return true;
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}