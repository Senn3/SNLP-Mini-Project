package de.snlp.mp.text_analysis;

import java.util.Scanner;

public class ExitThread extends Thread {

	/**
	 * Der Befehl der nötig ist, um das Programm sauber zu beenden, damit auch alle Ergebnisse gespeichert werden.
	 */
	public static final String QUIT_COMMAND = "quit";

	@Override
	public void run() {
		Scanner s = new Scanner(System.in);
		String line = "";
		while (!line.equalsIgnoreCase(QUIT_COMMAND)) {
			line = s.nextLine();
		}
		Utils.log("Stop command recognized.");
		s.close();
	}
}