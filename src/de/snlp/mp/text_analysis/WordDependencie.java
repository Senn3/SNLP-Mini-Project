package de.snlp.mp.text_analysis;

public class WordDependencie {

	private String word;
	private String type;

	private String relation;
	private String word2;

	private int occurrence;

	public WordDependencie() {
		this.occurrence = 1;
	}

	public WordDependencie(String type, String word, String relation, String word2, int occurrence) {
		this.type = type;
		this.word = word;
		this.relation = relation;
		this.word2 = word2;
		this.occurrence = occurrence;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public String getWord2() {
		return word2;
	}

	public void setWord2(String word2) {
		this.word2 = word2;
	}

	public void increaseOccurrence() {
		this.occurrence++;
	}

	public int getOccurrence() {
		return this.occurrence;
	}

	@Override
	public String toString() {
		return type + "###" + word + "###" + relation + "###" + word2 + "###" + occurrence;
	}

	public static WordDependencie convertToWordDep(String s) {
		String[] w = s.split("###");
		if (w.length != 5) {
			System.out.println("Error while converting the string: " + s);
			return null;
		} else {
			return new WordDependencie(w[0], w[1], w[2], w[3], Integer.parseInt(w[4]));
		}
	}

}
