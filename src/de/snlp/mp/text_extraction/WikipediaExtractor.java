package de.snlp.mp.text_extraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WikipediaExtractor {

	private static final String START_TOKEN = "<doc";
	private static final String END_TOKEN = "</doc>";

	private static String inputFolder = "F://Wikipedia Corpus";
	private static String outputFolder = "F://Test";

	private static int process = 0;
	private static long currentfileCount = 0;
	private static long fileCount = 0;

	private static int counter = 0;

	private static PauseThread p;

	private static DateFormat df = new SimpleDateFormat("HH:mm:ss");

	public static void main(String[] args) {
		if (args.length != 2) {
			log("Need 2 parameter as arguments.\n1. The input folder\n2. The output folder");
			return;
		}

		inputFolder = args[0];
		outputFolder = args[1];

		p = new PauseThread();
		p.start();

		if (new File(outputFolder).isDirectory())
			new File(outputFolder).mkdir();
		log("Start counting the files.");
		setFileCount(new File(inputFolder));
		log("Found " + fileCount + " files to process.");
		log("The process can be paused with \"pause\" and continued with \"continue\"");
		log("Start process...");
		readInput(new File(inputFolder));
		System.out.println();
		log("Finished process...");
		log("Ignored " + counter + " files");
		System.exit(0);
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
				while (!p.isRunning()) {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
					String line;
					List<String> content = new ArrayList<String>();
					String name = "";
					boolean readContent = false;
					while ((line = reader.readLine()) != null) {
						if (!line.equals("")) {
							if (isStartLine(line)) {
								if (readContent) {
									System.out.println("\nMissing end line: " + line + " in document: " + f.getAbsolutePath());
									name = "";
									content.clear();
								}

								do {
									name = reader.readLine();
								} while (name.equals(""));
								readContent = true;
							} else if (isEndLine(line)) {
								if (readContent) {
									readContent = false;
									writeFile(name, content);
								} else {
									System.out.println("\nMissing start line: " + line + " in document: " + f.getAbsolutePath());
								}
								name = "";
								content.clear();
							} else if (readContent) {
								content.add(line + "\n");
							} else {
								name = "";
								content.clear();
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
		Character firstChar;
		Character secondChar;
		Character thirdChar;
		if (name.toCharArray().length >= 1 && Character.isAlphabetic(name.toUpperCase().charAt(0)))
			firstChar = name.toUpperCase().charAt(0);
		else
			firstChar = getRandomChar();
		if (name.toCharArray().length >= 2 && Character.isAlphabetic(name.toUpperCase().charAt(1)))
			secondChar = name.toUpperCase().charAt(1);
		else
			secondChar = getRandomChar();
		if (name.toCharArray().length >= 3 && Character.isAlphabetic(name.toUpperCase().charAt(2)))
			thirdChar = name.toUpperCase().charAt(2);
		else
			thirdChar = getRandomChar();
		File parent = new File(outputFolder + "/" + firstChar.toString() + "/" + secondChar.toString() + "/" + thirdChar.toString());
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

	private static char getRandomChar() {
		return (char) ((int) (Math.random() * 26 + 65));
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

	public static void log(String s) {
		System.out.println(df.format(new Date()) + " - " + s);
	}

}
