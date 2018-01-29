package de.snlp.mp.text_model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAnySetter;

/**
 * This class converts the json string, which is given by the stanford library to a list of corefs. This can't be done like all other
 * values, because the format of the json string isn't valid.
 * @author Daniel Possienke
 *
 */
public class CorefsHeader {

	/**
	 * The list of the corefs.
	 */
	private static List<Corefs> corefs = new ArrayList<Corefs>();

	@SuppressWarnings("unchecked")
	@JsonAnySetter
	public void convertCorefs(String name, Object o) {
		ArrayList<LinkedHashMap<String, Object>> list = (ArrayList<LinkedHashMap<String, Object>>) o;
		for (LinkedHashMap<String, Object> map : list) {
			Corefs c = new Corefs();
			c.setId((Integer) map.get("id"));
			c.setText((String) map.get("text"));
			c.setType((String) map.get("type"));
			c.setNumber((String) map.get("number"));
			c.setGender((String) map.get("gender"));
			c.setAnimacy((String) map.get("animacy"));
			c.setStartIndex((Integer) map.get("startIndex"));
			c.setEndIndex((Integer) map.get("endIndex"));
			c.setHeadIndex((Integer) map.get("headIndex"));
			c.setSentNum((Integer) map.get("sentNum"));
			c.setPosition((List<Integer>) map.get("position"));
			c.setRepresentativeMention((Boolean) map.get("isRepresentativeMention"));
			corefs.add(c);
		}
	}

	protected static List<Corefs> getCorefs() {
		return corefs;
	}

	/**
	 * This method is needed to reset the list of corefs. This is done each time before a new text or line is converted by the stanford
	 * library in the StanfordLib class.
	 */
	public static void clearCorefs() {
		corefs = new ArrayList<Corefs>();
	}
}
