package de.snlp.mp.utils;

import java.util.ArrayList;
import java.util.List;
import simplenlg.features.Feature;
import simplenlg.features.Person;
import simplenlg.features.Tense;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.phrasespec.VPPhraseSpec;
import simplenlg.realiser.english.Realiser;

public class SimpleNLGDictionary {
	private Lexicon lexicon;
	
	private Realiser realiser;
	
	private NLGFactory nlgFactory;

	public SimpleNLGDictionary() {
		lexicon = Lexicon.getDefaultLexicon();
        realiser = new Realiser(lexicon);
        nlgFactory = new NLGFactory(lexicon);
	}
	
	public List<String> conjugateVerb(String word) {
		List<String> verbConjugations = new ArrayList<String>();
		
		for(Tense t : Tense.values()) {
			for (Person p : Person.values()) {
				for(int i = 0; i < 2 ; i++) {
					for (int j = 0; j < 2; j++) {
						for (int k =0; k< 2;k++) {
							VPPhraseSpec verb = nlgFactory.createVerbPhrase(word);
							SPhraseSpec clause = nlgFactory.createClause();
							clause.setFeature(Feature.TENSE, t);
							clause.setFeature(Feature.PERSON, p);
							if (i == 1)
								clause.setPlural(true);
							if (j == 1)
								clause.setFeature(Feature.PASSIVE, true);
							if (k == 1)
								clause.setFeature(Feature.PERFECT, true);
							
							clause.setVerb(verb);
							String conjugatedVerb = realiser.realiseSentence(clause);
							if (!verbConjugations.contains(conjugatedVerb)) {
								verbConjugations.add(conjugatedVerb.substring(0, conjugatedVerb.length()-1).toLowerCase());
							}
						}
					}
				}
			}
		}	
		return verbConjugations;
	}	
	
//	public static void main(String[] args) {
//		lexicon = Lexicon.getDefaultLexicon();
//        realiser = new Realiser(lexicon);
//        nlgFactory = new NLGFactory(lexicon);
//		
////        NPPhraseSpec subject = nlgFactory.createNounPhrase("Tim"); 
//		VPPhraseSpec verb = nlgFactory.createVerbPhrase("bear");
//		SPhraseSpec clause = nlgFactory.createClause();
//		
//		clause.setFeature(Feature.PERSON, Person.THIRD);
//		clause.setFeature(Feature.TENSE, Tense.FUTURE);
//		clause.setFeature(Feature.PERFECT, true);
//		clause.setFeature(Feature.PASSIVE, true);
//		clause.setPlural(false);
//		
////		clause.setSubject(subject);
//		clause.setVerb(verb);
//		
//		String result = realiser.realiseSentence(clause);
//		System.out.println(result);
//	}
}
