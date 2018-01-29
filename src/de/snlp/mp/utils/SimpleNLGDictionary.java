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

/**
 * This class is used to conjugate verbs. It uses the 'SimpleNLG' dictionary.
 * @author Patrick Thiele
 *
 */
public class SimpleNLGDictionary {
	/**
	 * The lexicon used by the 'SimpleNLG' dictionary.
	 */
	private Lexicon lexicon;

	/**
	 * The realiser used by the 'SimpleNLG' dictionary.
	 */
	private Realiser realiser;

	/**
	 * The NLGFactory used by the 'SimpleNLG' dictionary.
	 */
	private NLGFactory nlgFactory;

	public SimpleNLGDictionary() {
		lexicon = Lexicon.getDefaultLexicon();
		realiser = new Realiser(lexicon);
		nlgFactory = new NLGFactory(lexicon);
	}

	/**
	 * Conjugates a given word.
	 * @param word The verb to be conjugated.
	 * @return A list containing the conjugation of the verb.
	 */
	public List<String> conjugateVerb(String word) {
		List<String> verbConjugations = new ArrayList<String>();

		for (Tense t : Tense.values()) {
			for (Person p : Person.values()) {
				for (int i = 0; i < 2; i++) {
					for (int j = 0; j < 2; j++) {
						for (int k = 0; k < 2; k++) {
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
								verbConjugations.add(conjugatedVerb.substring(0, conjugatedVerb.length() - 1).toLowerCase());
							}
						}
					}
				}
			}
		}
		return verbConjugations;
	}
}
