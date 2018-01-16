package de.snlp.mp.text_model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CorefsHeader {

	@JsonProperty("1")
	private List<Corefs> one;
	@JsonProperty("2")
	private List<Corefs> two;
	@JsonProperty("3")
	private List<Corefs> three;
	@JsonProperty("4")
	private List<Corefs> four;
	@JsonProperty("5")
	private List<Corefs> five;

	public List<Corefs> getOne() {
		return one;
	}

	public void setOne(List<Corefs> one) {
		this.one = one;
	}

	public List<Corefs> getTwo() {
		return two;
	}

	public void setTwo(List<Corefs> two) {
		this.two = two;
	}

	public List<Corefs> getThree() {
		return three;
	}

	public void setThree(List<Corefs> three) {
		this.three = three;
	}

	public List<Corefs> getFour() {
		return four;
	}

	public void setFour(List<Corefs> four) {
		this.four = four;
	}

	public List<Corefs> getFive() {
		return five;
	}

	public void setFive(List<Corefs> five) {
		this.five = five;
	}

}
