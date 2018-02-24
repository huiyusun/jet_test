package edu.nyu.jet.supervised;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class AddSubtypes {

	public static void Separate() {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			String dir = "/Users/nuist/documents/NlpResearch/Oracle-AL/SupervisedLDP/";
			input = new BufferedReader(new FileReader(dir + "patterns_supervised_train_typeSubtype"));
			output = new BufferedWriter(new FileWriter(dir + "patterns_supervised_train_typeSubtype_v1"));
			String curType = "";

			while ((inputLine = input.readLine()) != null) {
				if (!inputLine.contains("="))
					continue;
				String path = inputLine.split("=")[0].trim();
				String type = inputLine.split("=")[1].trim();
				if (path.substring(0, 3).equals("T: ")) {
					output.write(inputLine + "\n");
					curType = type;
				} else if (path.substring(0, 3).equals("S: ")) {
					if (!type.equals(curType))
						output.write(inputLine + "\n");
				}
			}

			input.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Separate();
	}
}