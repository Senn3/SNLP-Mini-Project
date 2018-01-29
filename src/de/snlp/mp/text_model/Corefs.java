package de.snlp.mp.text_model;

import java.util.List;

/**
 * This class represents a corefs from the text model, which is given by the stanford library.
 * @author Daniel Possienke
 *
 */
public class Corefs {

	private int id;
	private String text;
	private String type;
	private String number;
	private String gender;
	private String animacy;
	private int startIndex;
	private int endIndex;
	private int headIndex;
	private int sentNum;
	private List<Integer> position;
	private boolean isRepresentativeMention;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getAnimacy() {
		return animacy;
	}

	public void setAnimacy(String animacy) {
		this.animacy = animacy;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	public int getHeadIndex() {
		return headIndex;
	}

	public void setHeadIndex(int headIndex) {
		this.headIndex = headIndex;
	}

	public int getSentNum() {
		return sentNum;
	}

	public void setSentNum(int sentNum) {
		this.sentNum = sentNum;
	}

	public List<Integer> getPosition() {
		return position;
	}

	public void setPosition(List<Integer> position) {
		this.position = position;
	}

	public boolean isRepresentativeMention() {
		return isRepresentativeMention;
	}

	public void setRepresentativeMention(boolean isRepresentativeMention) {
		this.isRepresentativeMention = isRepresentativeMention;
	}
}
