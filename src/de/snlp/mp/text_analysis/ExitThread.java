package de.snlp.mp.text_analysis;

import java.util.Scanner;

public class ExitThread extends Thread {

	public static final String QUIT_COMMAND = "quit";

	@Override
	public void run() {
		Scanner s = new Scanner(System.in);
		String line = "";
		while (!line.equalsIgnoreCase(QUIT_COMMAND)) {
			line = s.nextLine();
		}
		System.out.println("Stop command recognized.");
		s.close();
	}
}
