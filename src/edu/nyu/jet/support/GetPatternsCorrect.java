package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class GetPatternsCorrect {

	public static void Separate() {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			input = new BufferedReader(
					new FileReader("/Users/nuist/documents/NlpResearch/ice-eval/patternsCorrectCount_ORG-AFF_perfect_v1"));

			output = new BufferedWriter(
					new FileWriter("/Users/nuist/documents/NlpResearch/ice-eval/patternsCorrect_ORG-AFF_perfect_v1"));

			while ((inputLine = input.readLine()) != null) {
				String pattern = inputLine.split("\\|")[0].trim();
				String counts = inputLine.split("\\|")[1].trim();

				double correct = Integer.parseInt(counts.split(" ")[0].trim());
				double incorrect = Integer.parseInt(counts.split(" ")[1].trim());

				if (correct >= incorrect * 0.5) { // restrict pattern counts
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