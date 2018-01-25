package de.snlp.mp.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.snlp.mp.text_model.Fact;

public class FactFileHandler {

	private static File trainFile = new File("train.tsv");
	private static File testFile = new File("test.tsv");
	private static File resultFile = new File("result.ttl");

	private static final String FACT_URI = "http://swc2017.aksw.org/task2/dataset/";
	private static final String PROP_URI = "<http://swc2017.aksw.org/hasTruthValue>";
	private static final String TYPE = "<http://www.w3.org/2001/XMLSchema#double>";

	public static List<Fact> readFactsFromFile(boolean isTrain) {
		if ((isTrain && !trainFile.exists()) || (!isTrain && !testFile.exists())) {
			System.out.println("Cannot find the .tsv file.");
			return null;
		}
		File f = isTrain ? trainFile : testFile;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"))) {
			String line = "";
			List<Fact> factList = new ArrayList<Fact>();
			while ((line = reader.readLine()) != null) {
				if (!line.equals("")) {
					String[] lineArray = line.split("\t");
					if (!line.split("\t")[0].equals("FactID")) {
						int factId = Integer.parseInt(lineArray[0]);
						String factStatement = lineArray[1];
						if (!isTrain) {
							factList.add(new Fact(String.valueOf(factId), factStatement));
						} else {
							double truthvalue = Double.parseDouble(lineArray[2]);
							factList.add(new Fact(String.valueOf(factId), factStatement, truthvalue));
						}
					}
				}
			}
			return factList;
		} catch (

		Exception e) {
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
