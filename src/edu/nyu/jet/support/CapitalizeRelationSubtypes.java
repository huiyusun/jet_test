package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class CapitalizeRelationSubtypes {

	public static void Separate() {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			input = new BufferedReader(
					new FileReader("/Users/nuist/documents/NlpResearch/ice-eval/aceKeyTriples_all_Subtype"));

			output = new BufferedWriter(new FileWriter(
					"/Users/nuist/documents/NlpResearch/ice-eval/aceKeyTriples_all_Subtype_caps"));

			while ((inputLine = input.readLine()) != null) {
				String[] lineArr = inputLine.split(":");

				output.write(lineArr[0].trim() + ":" + lineArr[1].trim().toUpperCase() + ":" + lineArr[2].trim() + "\n");
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