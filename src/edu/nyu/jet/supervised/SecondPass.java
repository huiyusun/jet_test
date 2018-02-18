package edu.nyu.jet.supervised;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class SecondPass {

	public static void Separate() {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			input = new BufferedReader(
					new FileReader("/Users/nuist/documents/NlpResearch/ice-eval/patterns_supervised_train_typeSubtype"));
			output = new BufferedWriter(
					new FileWriter("/Users/nuist/documents/NlpResearch/ice-eval/patterns_supervised_train_typeSubtype_good"));
			boolean isBadType = false;

			while ((inputLine = input.readLine()) != null) {
				if (!inputLine.contains("="))
					continue;

				String pattern = inputLine.split("\\|")[0].trim();
				String counts = inputLine.split("\\|")[1].trim();

				double correct = Integer.parseInt(counts.split(" ")[0].trim());
				double incorrect = Integer.parseInt(counts.split(" ")[1].trim());

				if (pattern.substring(0, 3).equals("T: ")) {
					if (correct * 2 >= incorrect) { // restrict pattern counts
						output.write(pattern + "\n");
						isBadType = false;
					} else {
						isBadType = true;
					}
				} else if (pattern.substring(0, 3).equals("S: ")) {
					if (!isBadType) { // look for neg subtypes if type is positive
						if (correct * 2 < incorrect)
							output.write("N" + pattern + "\n");
					} else { // look for pos subtypes if type is negative
						if (correct * 2 >= incorrect)
							output.write("P" + pattern + "\n");
					}
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