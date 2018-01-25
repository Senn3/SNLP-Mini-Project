package de.snlp.mp.utils;

import java.io.File;

public class RemoveFolder {

	public static void main(String[] args) {
		if (args.length != 1) {
			Utils.log("The number of arguments have to be 1!");
			Utils.log("1. The folder which should be removed");
			System.exit(255);
		}

		File dir = new File("D:\\Test");
		if (dir.isDirectory())
			removeDirChilds(dir);
		removeFile(dir);
	}

	private static void removeFile(File f) {
		if (f.delete()) {
			Utils.log("The folder \"" + f.getAbsolutePath() + "\" was removed successfully.");
		} else {
			Utils.log("The folder \"" + f.getAbsolutePath() + "\" can't be removed.");
		}
	}

	private static void removeDirChilds(File folder) {
		for (File f : folder.listFiles()) {
			if (f.isDirectory()) {
				removeDirChilds(f);
				f.delete();
			} else {
				f.delete();
			}
		}
	}

}
