package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class ChangeLDPFormat {

	public static void Clean(String inputFile) {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine, relation;

		try {
			input = new BufferedReader(new FileReader(inputFile));

			output = new BufferedWriter(
					new FileWriter("/Users/nuist/documents/NlpResearch/AL-patterns/phys/patterns_phys"));

			while ((inputLine = input.readLine()) != null) {
				if (!inputLine.isEmpty()) {
					String[] lineArr = inputLine.split("\t");
					output.write(lineArr[0] + " = " + lineArr[1] + "\n");
				}
			}

			input.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Clean("/Users/nuist/documents/NlpResearch/AL-patterns/phys/patterns_phys");
	}
}