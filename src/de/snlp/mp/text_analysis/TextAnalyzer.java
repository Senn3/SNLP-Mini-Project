package de.snlp.mp.text_analysis;

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

import de.snlp.mp.text_model.Dependency;
import de.snlp.mp.text_model.Sentence;
import de.snlp.mp.text_model.TextModel;
import de.snlp.mp.text_model.Token;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class TextAnalyzer extends StanfordCoreNLP {

	private static DateFormat df = new SimpleDateFormat("HH:mm:ss");

	private static String corpusName = "Wikipedia Corpus Cutted";
	private static File corpusFolder;

	/**
	 * Die Liste die die Namen aller abgearbeiteten Dokumente enthält.
	 */
	private static List<String> processedDocsList = new ArrayList<String>();

	/**
	 * Die Datei in der die Namen aller abgearbeiteten Dokumente gespeichert werden, damit das Programm zwischendruch gestoppt werden kann.
	 */
	private static final File processedDocsFile = new File("ProcessedDocuments.txt");

	/**
	 * Die Anzahl der Dokumente im Korpus.
	 */
	private static int fileCounter;

	// private static List<WordDependency> wordDependencies = new ArrayList<WordDependency>();
	private static List<List<WordDependency>> wordDependencies = new ArrayList<List<WordDependency>>();

	private static final File analysedTextFile = new File("AnalysedText.txt");

	private static TextModel[] textModelResults;

	private static int cores;

	private static StanfordLibThread[] threads;

	private static ExitThread exitThread;

	public static void main(String[] args) {

		if (args.length == 0) {
			log("No argument found. Use \"" + corpusName + "\" as corpus folder.");
		} else {
			corpusName = args[1];
		}
		corpusFolder = new File(corpusName);
		if (!corpusFolder.exists()) {
			log("Cannot find the folder: " + corpusFolder.getAbsolutePath());
			return;
		}

		StanfordLibThread.initStandFordLib();
		setFileCount(corpusFolder);
		readProcessedDocs();
		log(processedDocsList.size() + " out of " + fileCounter + " articles are already processed. ");

		log("Read previous word dependencies out of the file \"" + analysedTextFile.getName() + "\".");
		readWordDepListToFile();
		log("Found " + countWordDependencies() + " different word dependencies.");

		log("This program should be stopped with: \"" + ExitThread.QUIT_COMMAND + "\".");
		log("DONOT use cmd + c,otherwise no calculations will be saved.");
		exitThread = new ExitThread();
		exitThread.start();

		cores = Runtime.getRuntime().availableProcessors();
		// cores = 1;
		threads = new StanfordLibThread[cores];
		textModelResults = new TextModel[cores];

		do {
			try {
				startThreadIfAvailable();
				processResultIfAvailable();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while ((fileCounter > processedDocsList.size() && exitThread.isAlive()) || threadsAreAlive(threads) || !resultsAreNull());

		log("Start saving the " + countWordDependencies() + " results.");
//		writeListToFile(processedDocsFile, new ArrayList<Object>(processedDocsList), false);
		for (int i = 0; i < wordDependencies.size(); i++) {
			if (i == 0)
				writeListToFile(analysedTextFile, new ArrayList<Object>(wordDependencies.get(i).subList(1, wordDependencies.get(i).size())),
						false);
			else
				writeListToFile(analysedTextFile, new ArrayList<Object>(wordDependencies.get(i).subList(1, wordDependencies.get(i).size())),
						true);
		}
		log("Finished program. The result is saved to: \"" + analysedTextFile.getName() + "\"");
		System.exit(0);
	}

	private static void startThreadIfAvailable() {
		for (int i = 0; i < cores; i++) {
			if (fileCounter > processedDocsList.size() && exitThread.isAlive()) {
				// Rechnet gerade einer der möglichen Threads nicht?
				if (!threadIsAlive(threads[i]) && textModelResults[i] == null) {
					File f = getRawRandomFile(corpusFolder);
					processedDocsList.add(f.getName());
					String fileContent = readFile(f);
					threads[i] = new StanfordLibThread(i, f.getAbsolutePath(), fileContent);
					threads[i].start();
				}
			}
		}
	}

	private static void processResultIfAvailable() {
		try {
			for (int i = 0; i < cores; i++) {
				if (!threadIsAlive(threads[i]) && textModelResults[i] != null) {
					processTextModel(textModelResults[i]);
					textModelResults[i] = null;
					threads[i] = null;
				}
			}
		} catch (NullPointerException e) {
			System.out.println("processResultIfAvailable");
			System.out.println((threads == null) + " " + (textModelResults == null));
			e.printStackTrace();
		}
	}

	public static void setTextModelResult(int thread, TextModel model) {
		textModelResults[thread] = model;
	}

	private static boolean resultsAreNull() {
		for (int i = 0; i < textModelResults.length; i++) {
			if (textModelResults[i] != null)
				return false;
		}
		return true;
	}

	private static boolean threadsAreAlive(StanfordLibThread[] threads) {
		for (int i = 0; i < threads.length; i++) {
			if (threadIsAlive(threads[i]))
				return true;
		}
		return false;
	}

	private static boolean threadIsAlive(StanfordLibThread thread) {
		if (thread != null && thread.isAlive())
			return true;
		else
			return false;
	}

	/**
	 * Lädt die Datei, in der die Namen der bereits abgearbeiteten Artikel drin stehen und fügt die zur Liste 'processedDocsFile' hinzu.
	 */
	private static void readProcessedDocs() {
		if (!processedDocsFile.exists())
			return;
		try (BufferedReader reader = new BufferedReader(new FileReader(processedDocsFile))) {
			String line = "";
			while ((line = reader.readLine()) != null) {
				if (!line.equals(""))
					processedDocsList.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Zählt die Anzahl der .txt-Dateien. Die Anzahl wird gebraucht, damit das Programm weiß, wann es mit der Bearbeitung fertig ist, da die
	 * Artikel zufällig und nicht chronologisch bearbeitet werden.
	 * @param Der Ordner mit dem Korpus
	 */
	private static void setFileCount(File folder) {
		for (File f : folder.listFiles()) {
			if (f.isDirectory())
				setFileCount(f);
			else if (f.getName().endsWith(".txt")) {
				fileCounter++;
			}
		}
	}

	/**
	 * Sucht rekursiv zufällig einen Artikel heraus, der noch nicht bearbeitet wurde, also noch nicht in der Liste vorhanden ist.
	 * @param Der Ordner mit dem Korpus
	 * @return Unbearbeiteter Artikel
	 */
	private static File getRawRandomFile(File folder) {
		int rnd = (int) (Math.random() * folder.listFiles().length);
		if (folder.listFiles()[rnd].isDirectory()) {
			return getRawRandomFile(folder.listFiles()[rnd]);
		} else {
			if (processedDocsList.contains(folder.listFiles()[rnd].getName())) {
				return getRawRandomFile(corpusFolder);
			} else {
				return folder.listFiles()[rnd];
			}
		}
	}

	/**
	 * Liest den Inhalt einer .txt-Datei ein und gibt diesen zurück
	 * @param Der Inhalt der Datei
	 */
	private static String readFile(File f) {
		try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
			String line = "";
			StringBuilder builder = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				builder.append(line + "\n");
			}
			return builder.toString();
		} catch (Exception e) {
			e.printStackTrace();
			log("Error reading the content of the article: " + f.getAbsolutePath());
			return null;
		}
	}

	/**
	 * Bearbeitet einen Text, indem von jedem Satz die Abhängigkeiten gespeichert werden
	 * @param model
	 */
	private static void processTextModel(TextModel model) {
		try {
			if (model == null)
				System.out.println("Model");
			for (Sentence s : model.getSentences()) {
				if (s == null)
					System.out.println("Sentence");
				List<Token> token = s.getTokens();
				if (token == null)
					System.out.println("Token");
				if (s.getBasicDependencies() != null) {
					for (Dependency d : s.getBasicDependencies()) {
						WordDependency w = createWordDependency(d, token);
						if (w != null) {
							if (!isWordDepInList(w)) {
								wordDependencies.get(getWordDependencyList(w)).add(w);
							}
						}
					}
				} else {
					System.out.println("dep == null");
				}
			}
		} catch (NullPointerException e) {
			System.out.println("processTextModel");
			e.printStackTrace();
		}
	}

	/**
	 * Liest die Datei in der die Wortabhängigkeiten gespeichert sind ein, falls vorhanden, und füllt die Liste "wordDependencies"
	 */
	private static void readWordDepListToFile() {
		if (!analysedTextFile.exists())
			return;
		try (BufferedReader reader = new BufferedReader(new FileReader(analysedTextFile))) {
			String line = "";
			while ((line = reader.readLine()) != null) {
				if (!line.equals("")) {
					WordDependency w = WordDependency.convertToWordDep(line);
					if (w != null) {
						wordDependencies.get(getWordDependencyList(w)).add(w);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Erstellt eine Wortabhängigkeit aus der Abhängigkeit und der Liste der Wörter, in der der Worttyp (bsp. NN) gespeichert ist.
	 * @param d
	 * @param token
	 * @return
	 */
	private static WordDependency createWordDependency(Dependency d, List<Token> token) {
		try {
			WordDependency w = new WordDependency();
			w.setWord(d.getGovernorGloss());
			w.setWord2(d.getDependentGloss());
			w.setRelation(d.getDep());
			if (d.getDep().equals("ROOT")) {
				w.setType("ROOT");
			} else {
				w.setType(token.get(d.getGovernor() - 1).getPos());
			}
			return w;
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Überprüft, ob eine Wortabhängigkeit genau so schon in der Liste vorkommt. Ist dies der Fall, so wird der Zähler von dieser um 1
	 * erhöht.
	 * @param entry
	 * @return
	 */
	private static boolean isWordDepInList(WordDependency entry) {
		try {
			List<WordDependency> list = wordDependencies.get(getWordDependencyList(entry));
			for (WordDependency e : list) {
				if (entry.getWord() != null && entry.getWord2() != null && entry.getType() != null && entry.getRelation() != null) {
					if (e.getWord().equals(entry.getWord()) && e.getWord2().equals(entry.getWord2()) && e.getType().equals(entry.getType())
							&& e.getRelation().equals(entry.getRelation())) {
						e.increaseOccurrence();
						return true;
					}
				} else {
					return true;
				}
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static int getWordDependencyList(WordDependency entry) {
		for (int i = 0; i < wordDependencies.size(); i++) {
			if (wordDependencies.get(i).get(0).getType().equals(entry.getType()))
				return i;
		}
		wordDependencies.add(new ArrayList<WordDependency>());
		WordDependency head = new WordDependency(entry.getType(), "HEAD", "HEAD", "HEAD", 0);
		wordDependencies.get(wordDependencies.size() - 1).add(head);
		return wordDependencies.size() - 1;
	}

	private static int countWordDependencies() {
		int count = 0;
		for (int i = 0; i < wordDependencies.size(); i++)
			count += wordDependencies.get(i).size() - 1;
		return count;
	}

	// 357285

	private static void writeListToFile(File f, List<Object> list, boolean append) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, append))) {
			for (Object o : list) {
				writer.write(o.toString() + "\n");
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void log(String s) {
		System.out.println(df.format(new Date()) + " - " + s);
	}

}
