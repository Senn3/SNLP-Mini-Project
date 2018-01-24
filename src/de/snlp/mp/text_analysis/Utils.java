package de.snlp.mp.text_analysis;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.snlp.mp.text_model.Corefs;
import de.snlp.mp.text_model.TextModel;
import de.snlp.mp.text_model.Token;

public class Utils {
	
	private static DateFormat df = new SimpleDateFormat("HH:mm:ss");

	public static List<String> getNounsFromTextModel(TextModel model, String factStatement) {
		List<String> nouns = new ArrayList<String>();
		for (Corefs c : model.getCorefs()) {
			if (c.getType().equals("PROPER")) {
				if (c.getText().contains(" , ")) {
					nouns.add(c.getText().split(" , ")[0]);
					nouns.add(c.getText().split(" , ")[1]);
				} else {
					nouns.add(c.getText().replaceAll(" 's", ""));
				}
			}
		}

		for (Token t : model.getSentences().get(0).getTokens()) {
			if (t.getPos().contains("NN") && !t.getPos().contains("P")) {
				nouns.add(t.getOriginalText());
			}

		}
		return nouns;
	}

	public static List<String> getVerbsFromTextModel(TextModel model, String factStatement) {
		List<String> verbs = new ArrayList<String>();
		for (Token t : model.getSentences().get(0).getTokens()) {
			if (t.getPos().contains("VB") || t.getPos().contains("VBD") || t.getPos().contains("VBG") || t.getPos().contains("VBZ")
					|| t.getPos().contains("VBN") || t.getPos().contains("VBP")) {
				verbs.add(t.getOriginalText());
			}

		}
		return verbs;
	}
	
	public static void log(String s) {
		System.out.println(df.format(new Date()) + " - " + s);
	}
}
