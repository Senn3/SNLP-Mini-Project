package de.snlp.mp.fact_checking;

import java.util.ArrayList;
import java.util.List;
import simplenlg.features.Feature;
import simplenlg.features.Person;
import simplenlg.features.Tense;
import simplenlg.framework.InflectedWordElement;
import simplenlg.framework.LexicalCategory;
import simplenlg.framework.WordElement;
import simplenlg.lexicon.Lexicon;
import simplenlg.realiser.english.Realiser;

public class SimpleNLGDictionary {
	private Lexicon lexicon;
	
	private Realiser realiser;

	public SimpleNLGDictionary() {
		lexicon = Lexicon.getDefaultLexicon();
        realiser = new Realiser(lexicon);
	}
	
	public List<String> conjugateVerb(String word) {
		List<String> verbConjugations = new ArrayList<String>();
		WordElement verb = lexicon.getWord(word, LexicalCategory.VERB);
		
		for(Tense t : Tense.values()) {
			for (Person p : Person.values()) {
				for(int i = 0; i < 2 ; i++) {
					InflectedWordElement infl = new InflectedWordElement(verb);
					infl.setFeature(Feature.TENSE, t);
					infl.setFeature(Feature.PERSON, p);
					if (i == 1)
						infl.setPlural(true);
					
					String conjugatedVerb = realiser.realise(infl).getRealisation();
					if (!verbConjugations.contains(conjugatedVerb)) {
						verbConjugations.add(conjugatedVerb);
					}
				}
			}
		}	
		return verbConjugations;
	}	
}
