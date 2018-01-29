package de.snlp.mp.text_model;

import java.util.List;

/**
 * This class represents a sentence from the text model, which is given by the stanford library.
 * @author Daniel Possienke
 *
 */
public class Sentence {

	private int index;
	private String parse;
	private List<Dependency> basicDependencies;
	private List<Dependency> enhancedDependencies;
	private List<Dependency> enhancedPlusPlusDependencies;
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

	public List<Dependency> getBasicDependencies() {
		return basicDependencies;
	}

	public void setBasicDependencies(List<Dependency> basicDependencies) {
		this.basicDependencies = basicDependencies;
	}

	public List<Dependency> getEnhancedDependencies() {
		return enhancedDependencies;
	}

	public void setEnhancedDependencies(List<Dependency> enhancedDependencies) {
		this.enhancedDependencies = enhancedDependencies;
	}

	public List<Dependency> getEnhancedPlusPlusDependencies() {
		return enhancedPlusPlusDependencies;
	}

	public void setEnhancedPlusPlusDependencies(List<Dependency> enhancedPlusPlusDependencies) {
		this.enhancedPlusPlusDependencies = enhancedPlusPlusDependencies;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public void setTokens(List<Token> tokens) {
		this.tokens = tokens;
	}
}
