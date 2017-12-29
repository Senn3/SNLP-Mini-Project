package de.snlp.mp.text_model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TextModel {

	private List<Sentence> sentences;
	@JsonIgnore
	private List<Corefs> corefs;

	public List<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(List<Sentence> sentences) {
		this.sentences = sentences;
	}

	public List<Corefs> getCorefs() {
		return corefs;
	}

	public void setCorefs(List<Corefs> corefs) {
		this.corefs = corefs;
	}

}
