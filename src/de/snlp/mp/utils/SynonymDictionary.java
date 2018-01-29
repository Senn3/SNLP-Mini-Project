package de.snlp.mp.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;

/**
 * This class acts as a dictionary, which finds synonyms to a given word. It uses a 'WordNet' dictionary and the 'JWI' library to access it.
 * Additionally to finding synonyms of word, the class also conjugates verbs.
 * 
 * @author Patrick Thiele
 *
 */
public class SynonymDictionary {

	/**
	 * The path to the 'WordNet' dictionary.
	 */
	private final String path = "SynonymDictionary/dict";

	/**
	 * An instance of the 'SimpleNLG' dictionary, used to conjugate verbs.
	 */
	private SimpleNLGDictionary simpleNLGDictionary = new SimpleNLGDictionary();

	/**
	 * The 'WordNet' dictionary.
	 */
	private IDictionary dict;

	public SynonymDictionary() {
		try {
			URL url = new URL("file", null, path);
			dict = new Dictionary(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a list of synonyms to a given word. If the word is a verb, the word and its synonyms are also conjugated.
	 * @param word The word to find synonyms for.
	 * @param type The type of the word.
	 * @return A list that contains the synonyms of a word. If the word is a verb it also contains its conjugations. Empty if no synonyms
	 *         were found.
	 */
	public List<String> getSynonyms(String word, POS type) {
		try {
			dict.open();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Get stem of word, since only those are in the dictionary
		WordnetStemmer stemmer = new WordnetStemmer(dict);
		List<String> stems = stemmer.findStems(word, type);
		String stemmedWord = "";
		if (stems.isEmpty())
			stemmedWord = word;
		else
			stemmedWord = stems.get(0);

		IIndexWord idxWord = dict.getIndexWord(stemmedWord, type);
		// Word not contained in dictionary?
		if (idxWord == null)
			return new ArrayList<String>();

		IWordID wordId = idxWord.getWordIDs().get(0);
		IWord dictWord = dict.getWord(wordId);
		ISynset synset = dictWord.getSynset();

		dict.close();

		List<String> synonyms = new ArrayList<String>();
		for (IWord w : synset.getWords()) {
			synonyms.add(w.getLemma().toLowerCase());

			if (type == POS.VERB)
				synonyms = combineLists(simpleNLGDictionary.conjugateVerb(w.getLemma().toLowerCase()), synonyms);
		}

		return synonyms;
	}

	/**
	 * Combines two lists to one list. Also removes duplicate elements.
	 * @param l1 First list to combine.
	 * @param l2 Second list to combine.
	 * @return The combined list of {@link l1} and {@link l2}.
	 */
	private static List<String> combineLists(List<String> l1, List<String> l2) {
		List<String> list = new ArrayList<String>();

		list.addAll(l1);

		for (String s : l2) {
			if (!list.contains(s))
				list.add(s);
		}

		return list;
	}
}
