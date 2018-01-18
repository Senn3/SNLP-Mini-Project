package de.snlp.mp.text_extraction;

import java.util.Scanner;

public class PauseThread extends Thread {

	private boolean isRunning = true;

	public static final String PAUSE_COMMAND = "pause";

	public static final String CONTINUE_COMMAND = "continue";

	@Override
	public void run() {
		Scanner s = new Scanner(System.in);
		String line = "";
		while (this.isAlive()) {
			line = s.nextLine();
			if (line.equals(PAUSE_COMMAND)) {
				isRunning = false;
				System.out.println("\nPause the current thread.\n");
			}
			if (line.equals(CONTINUE_COMMAND)) {
				isRunning = true;
				System.out.println("\nContinue the current thread.\n");
			}
		}
		s.close();
	}

	public boolean isRunning() {
		return isRunning;
	}

}
