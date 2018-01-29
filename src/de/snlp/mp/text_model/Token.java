package de.snlp.mp.text_model;

/**
 * This class represents a token from a sentence from the text model, which is given by the stanford library.
 * @author Daniel Possienke
 *
 */
public class Token {

	private int index;
	private String word;
	private String originalText;
	private String lemma;
	private int characterOffsetBegin;
	private int characterOffsetEnd;
	private String pos;
	private String ner;
	private String normalizedNER;
	private String speaker;
	private String before;
	private String after;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getOriginalText() {
		return originalText;
	}

	public void setOriginalText(String originalText) {
		this.originalText = originalText;
	}

	public String getLemma() {
		return lemma;
	}

	public void setLemma(String lemma) {
		this.lemma = lemma;
	}

	public int getCharacterOffsetBegin() {
		return characterOffsetBegin;
	}

	public void setCharacterOffsetBegin(int characterOffsetBegin) {
		this.characterOffsetBegin = characterOffsetBegin;
	}

	public int getCharacterOffsetEnd() {
		return characterOffsetEnd;
	}

	public void setCharacterOffsetEnd(int characterOffsetEnd) {
		this.characterOffsetEnd = characterOffsetEnd;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getNer() {
		return ner;
	}

	public void setNer(String ner) {
		this.ner = ner;
	}

	public String getSpeaker() {
		return speaker;
	}

	public void setSpeaker(String speaker) {
		this.speaker = speaker;
	}

	public String getBefore() {
		return before;
	}

	public void setBefore(String before) {
		this.before = before;
	}

	public String getAfter() {
		return after;
	}

	public void setAfter(String after) {
		this.after = after;
	}

	public String getNormalizedNER() {
		return normalizedNER;
	}

	public void setNormalizedNER(String normalizedNER) {
		this.normalizedNER = normalizedNER;
	}

}
