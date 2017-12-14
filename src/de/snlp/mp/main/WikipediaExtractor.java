package de.snlp.mp.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WikipediaExtractor {

	private static final String START_TOKEN = "<doc";
	private static final String END_TOKEN = "</doc>";

	private static String inputFolder = "Input";
	private static String outputFolder = "Output";

	private static int process = 0;
	private static long currentfileCount = 0;
	private static long fileCount = 0;

	private static int counter = 0;

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Need 2 parameter as arguments.\n1. The input folder\n2. The output folder");
			return;
		}

		inputFolder = args[0];
		outputFolder = args[1];

		System.out.println("Start process...");
		if (new File(outputFolder).isDirectory())
			new File(outputFolder).mkdir();
		setFileCount(new File(inputFolder));
		readInput(new File(inputFolder));
		System.out.println("\nFinished process...");
		System.out.println("Ignore " + counter + " files");

	}

	private static void setFileCount(File folder) {
		for (File f : folder.listFiles()) {
			if (f.isDirectory())
				setFileCount(f);
			else
				fileCount++;
		}
	}

	private static void readInput(File folder) {
		for (File f : folder.listFiles()) {
			if (f.isDirectory()) {
				readInput(f);
			} else {
				try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
					String line;
					List<String> content = new ArrayList<String>();
					String name = "";
					boolean readContent = false;
					while ((line = reader.readLine()) != null) {
						if (!line.equals("")) {
							if (isStartLine(line)) {
								name = reader.readLine();
								if (readContent) {
									System.out.println("Missing end line: " + line + " in document: " + f.getName());
									name = "";
									content.clear();
								} else {
									readContent = true;
								}
							} else if (isEndLine(line)) {
								writeFile(name, content);
								name = "";
								content.clear();
								if (readContent) {
									readContent = false;
								} else {
									System.out.println("Missing start line: " + line + " in document: " + f.getName());
									name = "";
									content.clear();
								}
							} else {
								content.add(line + "\n");
								if (!readContent) {
									System.out.println("Missing start line: " + line + " in document: " + f.getName());
									name = "";
									content.clear();
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				currentfileCount++;

				// Print progress
				double v = (double) currentfileCount / (double) fileCount;
				for (int i = 1; i <= 20; i++) {
					if (v >= i * 0.05 && v < (i + 1) * 0.05 && i * 5 != process) {
						process = i * 5;
						System.out.print(process + "% - ");
					}
				}
			}
		}
	}

	private static boolean isStartLine(String line) {
		if (line.length() >= START_TOKEN.length()) {
			if (line.substring(0, START_TOKEN.length()).equals(START_TOKEN)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isEndLine(String line) {
		if (line.length() >= END_TOKEN.length()) {
			if (line.substring(0, END_TOKEN.length()).equals(END_TOKEN)) {
				return true;
			}
		}
		return false;
	}

	private static void writeFile(String name, List<String> content) {
		name = convertToWindowsFileNameRules(name);
		File f = new File(createPath(name));

		if (content.size() <= 3) {
			counter++;
			return;
		}

		if (f.exists()) {
			if (!(getFileLength(f) < content.size())) {
				return;
			} else {
				f.delete();
			}
		} else {
			try {
				if (!f.createNewFile())
					return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
			for (String s : content)
				writer.write(s);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String createPath(String name) {
		name = convertToWindowsFileNameRules(name);
		Character firstChar = name.toUpperCase().charAt(0);
		if (!Character.isAlphabetic(firstChar))
			firstChar = 'A';
		File parent = new File(outputFolder + "/" + firstChar.toString());
		if (!parent.isDirectory())
			parent.mkdirs();
		return parent.getPath() + "/" + name + ".txt";
	}

	private static int getFileLength(File f) {
		int counter = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
			while (reader.readLine() != null) {
				counter++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return counter;
	}

	private static String convertToWindowsFileNameRules(String name) {
		name = name.replace("<", " ");
		name = name.replace(">", " ");
		name = name.replace(":", " ");
		name = name.replace("\"", " ");
		name = name.replace("/", " ");
		name = name.replace("\\", " ");
		name = name.replace("|", " ");
		name = name.replace("?", " ");
		name = name.replace("*", " ");
		return name;
	}

}
