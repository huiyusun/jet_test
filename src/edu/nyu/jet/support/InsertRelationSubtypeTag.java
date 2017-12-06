package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class InsertRelationSubtypeTag {

	public static void Separate(String inputFile) {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			input = new BufferedReader(new FileReader(inputFile));

			output = new BufferedWriter(new FileWriter("/Users/nuist/documents/NlpResearch/ice-eval/aceResponseTriples1"));

			while ((inputLine = input.readLine()) != null) {
				String arg1 = inputLine.split("::")[0];
				String arg2 = inputLine.split("::")[1];

				output.write(arg1 + ":Citizen-Resident-Religion-Ethnicity:" + arg2 + "\n");
			}

			input.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Separate("/Users/nuist/documents/NlpResearch/ice-eval/aceResponseTriples");
	}
}