package de.snlp.mp.text_analysis;

public class WordDependency {

	/**
	 * Das eigentliche Word.
	 */
	private String word;

	/**
	 * Der Typ des Wortes (siehe https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html).
	 */
	private String type;

	/**
	 * Die Beziehung zwischen dem eigntlichen Wort und Wort2.
	 */
	private String relation;

	/**
	 * Das Wort, zudem das erste Wort in einer Beziehung steht (siehe http://universaldependencies.org/introduction.html).
	 */
	private String word2;

	/**
	 * Ein Zähler der angibt, wie oft exakt das Wort mit dem Typ und der Beziehung zu dem zweiten Wort schon gefunden wurde in der Menge von
	 * Texten. Durch den Zähler soll Arbeitsspeicher gespart werden, indem nicht von jedem Wort ein neues Objekt in die Liste eingefügt
	 * werden muss.
	 */
	private int occurrence;

	/**
	 * Der Text der die Wörter / Typen / Beziehungen / etc. am Ende in der Textdatei voneinander trennt.
	 */
	private static final String SPLIT_STRING = " ";

	public WordDependency() {
		this.occurrence = 1;
	}

	public WordDependency(String type, String word, String relation, String word2, int occurrence) {
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
		return type + SPLIT_STRING + word + SPLIT_STRING + relation + SPLIT_STRING + word2 + SPLIT_STRING + occurrence;
	}

	/**
	 * Wandelt einen String in ein WordDependency-Objekt um, vorausgesetzt, der String hat das richtige Format. Ansonsten wird null
	 * zurückgegeben.
	 * @param Der Text, der umgewandelt werden soll.
	 * @return
	 */
	public static WordDependency convertToWordDep(String s) {
		try {
			String[] w = s.split(SPLIT_STRING);
			if (w.length != 5) {
				throw new Exception();
			} else {
				return new WordDependency(w[0], w[1], w[2], w[3], Integer.parseInt(w[4]));
			}
		} catch (Exception e) {
			TextAnalyzer.log("Error converting the string: " + s);
			return null;
		}
	}

}
