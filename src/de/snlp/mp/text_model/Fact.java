package de.snlp.mp.text_model;

import java.util.List;

/**
 * This class represents a fact.
 * @author Daniel Possienke
 *
 */
public class Fact {

	/**
	 * The id of a fact.
	 */
	private String factId;

	/**
	 * The fact statement.
	 */
	private String factStatement;

	/**
	 * The truth value of a fact.
	 */
	private double truthvalue;

	/**
	 * A list of lists, where each list contains a noun / verb and his synonyms.
	 */
	private List<List<String>> wordsWithSynonyms;

	public Fact(String factId, String factStatement) {
		this.factId = factId;
		this.factStatement = factStatement;
		this.setTruthvalue(0);
	}

	public Fact(String factId, String factStatement, double truthvalue) {
		this.factId = factId;
		this.factStatement = factStatement;
		this.setTruthvalue(truthvalue);
	}

	public String getFactId() {
		return factId;
	}

	public String getFactStatement() {
		return factStatement;
	}

	public double getTruthvalue() {
		return truthvalue;
	}

	public void setTruthvalue(double truthvalue) {
		this.truthvalue = truthvalue;
	}

	public List<List<String>> getWordsWithSynonyms() {
		return wordsWithSynonyms;
	}

	public void setWordsWithSynonyms(List<List<String>> wordsWithSynonyms) {
		this.wordsWithSynonyms = wordsWithSynonyms;
	}

}
