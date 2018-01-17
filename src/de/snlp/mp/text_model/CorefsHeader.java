package de.snlp.mp.text_model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class CorefsHeader {

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

	public static void clearCorefs() {
		corefs = new ArrayList<Corefs>();
	}
}
