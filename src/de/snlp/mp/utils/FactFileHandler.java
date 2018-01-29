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

/**
 * This class contains methods to read the facts from the train and test file and writes a list of facts to the default result file.
 * @author Daniel Possienke
 *
 */
public class FactFileHandler {

	/**
	 * The default name of the training file.
	 */
	private static File trainFile = new File("train.tsv");

	/**
	 * The default name of the test file.
	 */
	private static File testFile = new File("test.tsv");

	/**
	 * The default name of the file in which the result should be saved.
	 */
	private static File resultFile = new File("result.ttl");

	/**
	 * Some constants which are needed to save the result.
	 */
	private static final String FACT_URI = "http://swc2017.aksw.org/task2/dataset/";
	private static final String PROP_URI = "<http://swc2017.aksw.org/hasTruthValue>";
	private static final String TYPE = "<http://www.w3.org/2001/XMLSchema#double>";

	/**
	 * Reads the test or the train file and returns a list of facts or null if the file can't be found.
	 * @param isTrain Should the the train or the test file be read
	 * @return The list of facts
	 */
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

	/**
	 * Writes a list of facts to the default result file.
	 * @param factList The list of facts which should be written to the result file.
	 */
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
