package de.snlp.mp.text_model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the model, which is given by the stanford library.
 * @author Daniel Possienke
 *
 */
public class TextModel {

	private List<Sentence> sentences;
	@SuppressWarnings("unused")
	private List<CorefsHeader> corefs = new ArrayList<CorefsHeader>();

	public List<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(List<Sentence> sentences) {
		this.sentences = sentences;
	}

	public List<Corefs> getCorefs() {
		return CorefsHeader.getCorefs();
	}

}
