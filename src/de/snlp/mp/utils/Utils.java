package de.snlp.mp.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.snlp.mp.text_model.Corefs;
import de.snlp.mp.text_model.TextModel;
import de.snlp.mp.text_model.Token;
import edu.mit.jwi.item.POS;

public class Utils {

	private static DateFormat df = new SimpleDateFormat("HH:mm:ss");

	private static SynonymDictionary synonymDictionary = new SynonymDictionary();

	public static List<String> getNounsFromTextModel(TextModel model, String factStatement) {
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

	public static List<String> getVerbsFromTextModel(TextModel model, String factStatement) {
		List<String> verbs = new ArrayList<String>();
		for (Token t : model.getSentences().get(0).getTokens()) {
			if (t.getPos().contains("VB") || t.getPos().contains("VBD") || t.getPos().contains("VBG") || t.getPos().contains("VBZ")
					|| t.getPos().contains("VBN") || t.getPos().contains("VBP")) {
				verbs.add(t.getOriginalText());
			}

		}
		return verbs;
	}

	public static boolean textContainsWordList(String text, List<List<String>> wordList, boolean printMatch) {
		String match = "";
		for (int i = 0; i < wordList.size(); i++) {
			boolean wordIsInContent = false;
			Synonyms: for (String s : wordList.get(i)) {
				if (Utils.textContainsWord(text, s)) {
					wordIsInContent = true;
					match += (s + " | ");
					break Synonyms;
				}
			}
			if (!wordIsInContent) {
				return false;
			}
		}
		if (printMatch)
			log("Match: \"" + text + "\" - " + match);
		return true;
	}

	private static boolean textContainsWord(String text, String word) {
		if (text.contains(" " + word) || text.contains("-" + word))
			return true;
		if (text.contains("\n" + word))
			return true;

		if (text.toCharArray().length >= word.toCharArray().length) {
			String firstWordInText = "";
			for (int i = 0; i < word.toCharArray().length; i++) {
				firstWordInText += text.charAt(i);
			}
			if (firstWordInText.equals(word))
				return true;
		}
		return false;
	}

	public static List<List<String>> getSynonyms(List<String> words, POS type) {
		if (synonymDictionary == null)
			synonymDictionary = new SynonymDictionary();
		;
		List<List<String>> wordsWithSynonyms = new ArrayList<List<String>>();
		for (int i = 0; i < words.size(); i++) {
			wordsWithSynonyms.add(new ArrayList<String>());

			for (String st : synonymDictionary.getSynonyms(words.get(i), type))
				wordsWithSynonyms.get(i).add(Utils.replaceSpecialChars(st).toLowerCase());
			if (wordsWithSynonyms.get(i).isEmpty())
				wordsWithSynonyms.get(i).add(Utils.replaceSpecialChars(words.get(i)).toLowerCase());
		}
		return wordsWithSynonyms;
	}

	public static void printSynonymList(String text, List<List<String>> wordsWithSynonyms) {
		String output = "";
		for (List<String> list : wordsWithSynonyms) {
			for (String word : list) {
				output += (word + " - ");
			}
			output += " | ";
		}
		output = output.substring(0, output.toCharArray().length - 3);
		Utils.log("Statement: " + text + "     Nouns: " + output);
	}

	public static void log(String s) {
		System.out.println(df.format(new Date()) + " - " + s);
	}

	public static String replaceSpecialChars(String text) {
		text = text.replace("ä", "ae").replace("Ä", "Ae").replace("ü", "ue").replace("Ü", "Ue").replace("ö", "oe").replace("ß", "ss");
		char[] array = text.toCharArray();
		for (int i = 0; i < array.length; i++) {
			int ascii = (int) array[i];
			if (ascii == 193) // Á
				array[i] = 'A';
			else if (ascii == 201) // É
				array[i] = 'E';
			else if (ascii == 225) // á
				array[i] = 'a';
			else if (ascii == 227) // ò
				array[i] = 'o';
			else if (ascii == 231) // ç
				array[i] = 'c';
			else if (ascii == 232) // è
				array[i] = 'e';
			else if (ascii == 233) // ù
				array[i] = 'u';
			else if (ascii == 237) // í
				array[i] = 'i';
			else if (ascii == 239) // ï
				array[i] = 'i';
			else if (ascii == 241) // ñ
				array[i] = 'n';
			else if (ascii == 243) // ó
				array[i] = 'o';
			else if (ascii == 248) // ø
				array[i] = 'o';
			else if (ascii == 250) // ú
				array[i] = 'u';
			else if (ascii == 253) // ý
				array[i] = 'y';
			else if (ascii == 257) // ā
				array[i] = 'a';
			else if (ascii == 263) // ć
				array[i] = 'c';
			else if (ascii == 268) // Č
				array[i] = 'C';
			else if (ascii == 269) // Č
				array[i] = 'c';
			else if (ascii == 275) // ē
				array[i] = 'e';
			else if (ascii == 283) // ě
				array[i] = 'e';
			else if (ascii == 299) // ī
				array[i] = 'i';
			else if (ascii == 322) // ł
				array[i] = 'l';
			else if (ascii == 326) // ņ
				array[i] = 'n';
			else if (ascii == 332) // ō
				array[i] = 'O';
			else if (ascii == 333) // ō
				array[i] = 'o';
			else if (ascii == 350) // Ş
				array[i] = 'S';
			else if (ascii == 351) // Ş
				array[i] = 's';
			else if (ascii == 352) // š
				array[i] = 'S';
			else if (ascii == 353) // š
				array[i] = 's';
			else if (ascii == 363) // ū
				array[i] = 'u';
		}
		return new String(array);
	}
}
