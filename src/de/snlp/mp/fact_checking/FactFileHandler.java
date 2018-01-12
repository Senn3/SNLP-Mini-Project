package de.snlp.mp.fact_checking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FactFileHandler {

	private static File trainFile = new File("train.tsv");
	private static File resultFile = new File("result.ttl");

	private static final String FACT_URI = "http://swc2017.aksw.org/task2/dataset/";
	private static final String PROP_URI = "<http://swc2017.aksw.org/hasTruthValue>";
	private static final String TYPE = "<http://www.w3.org/2001/XMLSchema#double>";

	public static List<Fact> readFactsFromFile() {
		if (!trainFile.exists()) {
			System.out.println("Cannot find the training file \"" + trainFile.getName() + "\"");
			return null;
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(trainFile))) {
			String line = "";
			List<Fact> factList = new ArrayList<Fact>();
			while ((line = reader.readLine()) != null) {
				if (!line.equals("")) {
					String[] f = line.split("\t");
					if (!line.split("\t")[0].equals("FactID")) {
						int factId = Integer.parseInt(f[0]);
						String factStatement = f[1];
						double truthvalue = Double.parseDouble(f[2]);
						factList.add(new Fact(factId, factStatement, truthvalue));
					}
				}
			}
			return factList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void writeFactsToFile(List<Fact> factList) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile))) {
			for (Fact f : factList) {
				String line = "<" + FACT_URI + f.getFactId() + "> " + PROP_URI + " \"" + f.getTruthvalue() + "\"^^" + TYPE + " .";
				writer.write(line + "\n");
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
