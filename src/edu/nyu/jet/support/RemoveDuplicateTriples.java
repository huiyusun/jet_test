package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;

public class RemoveDuplicateTriples {

	public static void Clean(String inputFile) {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			input = new BufferedReader(new FileReader(inputFile));

			output = new BufferedWriter(
					new FileWriter("/Users/nuist/documents/NlpResearch/ice-eval/aceResponseTriples_distinct"));

			HashSet<String> relationsSet = new HashSet<String>();

			while ((inputLine = input.readLine()) != null) {
				if (!inputLine.isEmpty()) {
					if (!relationsSet.contains(inputLine)) {
						output.write(inputLine + "\n");
					}

					relationsSet.add(inputLine);
				}
			}

			input.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Clean("/Users/nuist/documents/NlpResearch/ice-eval/aceResponseTriples");
	}
}