package de.snlp.mp.fact_checking;

import java.util.List;

public class Fact {

	private String factId;
	private String factStatement;
	private double truthvalue;
	
	private List<List<String>> wordsWithSynonyms;

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
