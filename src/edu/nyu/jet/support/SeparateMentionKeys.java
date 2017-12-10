package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class SeparateMentionKeys {

	public static void Separate(String inputFile) {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			input = new BufferedReader(new FileReader(inputFile));

			output = new BufferedWriter(new FileWriter(
					"/Users/nuist/documents/NlpResearch/ice-eval/aceMentionsKey_Near"));

			while ((inputLine = input.readLine()) != null) {
				if (inputLine.contains(":Near:")) {
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
		Separate("/Users/nuist/documents/NlpResearch/ice-eval/aceMentionsKey_all_subtype");
	}
}