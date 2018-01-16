package de.snlp.mp.text_model;

import java.util.ArrayList;
import java.util.List;

public class TextModel {

	private List<Sentence> sentences;
	private List<CorefsHeader> corefs;

	public List<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(List<Sentence> sentences) {
		this.sentences = sentences;
	}

	public List<CorefsHeader> getCorefs() {
		return corefs;
	}

	public void setCorefs(List<CorefsHeader> corefs) {
		this.corefs = corefs;
	}

	public List<Corefs> getCorefsList() {
		List<Corefs> l = new ArrayList<Corefs>();
		if (corefs.get(0).getOne() != null) {
			for (Corefs c : corefs.get(0).getOne())
				l.add(c);
		}
		if (corefs.get(0).getTwo() != null) {
			for (Corefs c : corefs.get(0).getTwo())
				l.add(c);
		}
		if (corefs.get(0).getThree() != null) {
			for (Corefs c : corefs.get(0).getThree())
				l.add(c);
		}
		if (corefs.get(0).getFour() != null) {
			for (Corefs c : corefs.get(0).getFour())
				l.add(c);
		}
		if (corefs.get(0).getFive() != null) {
			for (Corefs c : corefs.get(0).getFive())
				l.add(c);
		}
		return l;
	}

}
