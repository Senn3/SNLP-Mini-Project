package de.snlp.mp.fact_checking;

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

public class SynonymDictionary {
	
	private final String path = "SynonymDictionary/dict";
	
	private SimpleNLGDictionary simpleNLGDictionary = new SimpleNLGDictionary();
	
	private IDictionary dict;
	
	public SynonymDictionary() {
		try {
			URL url = new URL("file", null, path);
			dict = new Dictionary(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} 	
	}
	
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
		if(stems.isEmpty())
			stemmedWord = word;
		else
			stemmedWord = stems.get(0);
		
		IIndexWord idxWord = dict.getIndexWord(stemmedWord, type);
		// Word not contained in dictionary?
		if(idxWord == null)
			return new ArrayList<String>();
		
		IWordID wordId = idxWord.getWordIDs().get(0);
		IWord dictWord = dict.getWord(wordId);
		ISynset synset = dictWord.getSynset();
		
		dict.close();
		
		List<String> synonyms = new ArrayList<String>();
		for(IWord w : synset.getWords()) {
			synonyms.add(w.getLemma().toLowerCase());
			
			if (type == POS.VERB)
				synonyms = combineLists(simpleNLGDictionary.conjugateVerb(w.getLemma().toLowerCase()), synonyms);
		}
		
		return synonyms;
	}
	
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
