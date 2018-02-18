package edu.nyu.jet.supervised;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class FirstPass {

	public static void Separate() {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			input = new BufferedReader(
					new FileReader("/Users/nuist/documents/NlpResearch/ice-eval/patterns_supervised_train"));

			output = new BufferedWriter(
					new FileWriter("/Users/nuist/documents/NlpResearch/ice-eval/patterns_supervised_train_selected"));

			while ((inputLine = input.readLine()) != null) {
				String pattern = inputLine.split("\\|")[0].trim();
				String counts = inputLine.split("\\|")[1].trim();

				double correct = Integer.parseInt(counts.split(" ")[0].trim());
				double incorrect = Integer.parseInt(counts.split(" ")[1].trim());

				if (correct * 2 >= incorrect) { // restrict pattern counts
					// output.write(inputLine + "\n");
					output.write(pattern + "\n");
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