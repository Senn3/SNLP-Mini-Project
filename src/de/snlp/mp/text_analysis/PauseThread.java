package de.snlp.mp.text_analysis;

import java.util.Scanner;

/**
 * This class is needed to pause, continue and quit the text analysis.
 * @author Daniel Possienke
 *
 */
public class PauseThread extends Thread {

	private boolean isRunning = true;

	public static final String PAUSE_COMMAND = "p";

	public static final String CONTINUE_COMMAND = "c";

	public static final String QUIT_COMMAND = "q";

	@Override
	public void run() {
		Scanner s = new Scanner(System.in);
		String line = "";
		while (this.isAlive()) {
			line = s.nextLine();
			if (line.equals(PAUSE_COMMAND)) {
				isRunning = false;
				System.out.print("-P-");
			}
			if (line.equals(CONTINUE_COMMAND)) {
				isRunning = true;
				System.out.print("-C-");
			}
			if (line.equals(QUIT_COMMAND)) {
				System.out.print("-Q-");
				break;
			}

		}
		s.close();
	}

	public boolean isRunning() {
		return isRunning;
	}

}
