package de.snlp.mp.model;

import java.util.List;

public class Sentence {

	private int index;
	private String parse;
	private List<Dependencie> basicDependencies;
	private List<Dependencie> enhancedDependencies;
	private List<Dependencie> enhancedPlusPlusDependencies;
	private List<Token> tokens;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getParse() {
		return parse;
	}

	public void setParse(String parse) {
		this.parse = parse;
	}

	public List<Dependencie> getBasicDependencies() {
		return basicDependencies;
	}

	public void setBasicDependencies(List<Dependencie> basicDependencies) {
		this.basicDependencies = basicDependencies;
	}

	public List<Dependencie> getEnhancedDependencies() {
		return enhancedDependencies;
	}

	public void setEnhancedDependencies(List<Dependencie> enhancedDependencies) {
		this.enhancedDependencies = enhancedDependencies;
	}

	public List<Dependencie> getEnhancedPlusPlusDependencies() {
		return enhancedPlusPlusDependencies;
	}

	public void setEnhancedPlusPlusDependencies(List<Dependencie> enhancedPlusPlusDependencies) {
		this.enhancedPlusPlusDependencies = enhancedPlusPlusDependencies;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public void setTokens(List<Token> tokens) {
		this.tokens = tokens;
	}
}
