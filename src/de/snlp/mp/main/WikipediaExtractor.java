package de.snlp.mp.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class WikipediaExtractor {

	public static void main(String[] args) {
		File f = new File("Lufthansa.FBX");
		try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
			for (int i = 0; i < 1; i++) {
				System.out.println(reader.readLine());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
