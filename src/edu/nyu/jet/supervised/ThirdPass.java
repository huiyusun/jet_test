package edu.nyu.jet.supervised;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;

public class ThirdPass {

	public static void Separate() {
		BufferedReader input = null, input1 = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			input = new BufferedReader(
					new FileReader("/Users/nuist/documents/NlpResearch/ice-eval/patterns_supervised_train_typeSubtype"));
			input1 = new BufferedReader(new FileReader(
					"/Users/nuist/documents/NlpResearch/Oracle-AL/SupervisedLDP/patterns_supervised_train_subtype_v1"));
			output = new BufferedWriter(
					new FileWriter("/Users/nuist/documents/NlpResearch/ice-eval/patterns_supervised_train_typeSubtype_good"));
			HashSet<String> subtypes = new HashSet<String>();

			while ((inputLine = input1.readLine()) != null) {
				subtypes.add(inputLine.trim());
			}

			while ((inputLine = input.readLine()) != null) {
				if (!inputLine.contains("="))
					continue;

				String pattern = inputLine.split("\\|")[0].trim();
				String counts = inputLine.split("\\|")[1].trim();
				double correct = Integer.parseInt(counts.split(" ")[0].trim());
				double incorrect = Integer.parseInt(counts.split(" ")[1].trim());

				if (pattern.substring(0, 3).equals("S: ")) {
					if (correct * 2 >= incorrect) {
						String p = pattern.replace("S: ", "");
						if (subtypes.contains(p))
							output.write("P" + pattern + "\n");
					}

				}
			}

			input.close();
			input1.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Separate();
	}
}