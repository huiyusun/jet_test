package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class SeparateSentenceKeys {

	public static void Separate(String inputFile) {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			input = new BufferedReader(new FileReader(inputFile));

			output = new BufferedWriter(
					new FileWriter("/Users/nuist/documents/NlpResearch/ice-eval/aceSentencesKey_Geographical"));

			while ((inputLine = input.readLine()) != null) {
				if (inputLine.contains(":Geographical:")) {
					output.write(inputLine + "\n");
					output.write("\n");
				}
			}

			input.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Separate("/Users/nuist/documents/NlpResearch/ice-eval/aceSentencesKey_all_subtype");
	}
}