package de.snlp.mp.utils;

import java.io.File;

public class RemoveFolder {

	public static void main(String[] args) {
		if (args.length != 1) {
			Utils.log("The number of arguments have to be 1!");
			Utils.log("1. The folder which should be removed");
			System.exit(255);
		}
		if (new File(args[0]).delete()) {
			Utils.log("The folder was removed successfully.");
		} else {
			Utils.log("The folder can't be removed.");
		}
	}

}
