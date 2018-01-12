package de.snlp.mp.fact_checking;

public class Fact {

	private int factId;
	private String factStatement;
	private double truthvalue;

	public Fact(int factId, String factStatement, double truthvalue) {
		this.factId = factId;
		this.factStatement = factStatement;
		this.setTruthvalue(truthvalue);
	}

	public int getFactId() {
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

}
