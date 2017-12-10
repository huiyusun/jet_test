package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;

public class GetYesOrNoRelationOracle {

	public static void Combine() {
		BufferedReader input = null;
		BufferedWriter output = null;
		String line;

		try {
			input = new BufferedReader(
					new FileReader("/Users/nuist/Documents/NlpResearch/Oracle-AL/ClosestMatch/ORG-AFF/relationOracle.all"));

			output = new BufferedWriter(
					new FileWriter("/Users/nuist/Documents/NlpResearch/Oracle-AL/ClosestMatch/ORG-AFF/relationOracle.pos"));

			while ((line = input.readLine()) != null) {
				if (line.contains(": YES")) {
					output.write(line + "\n");
				}
			}

			input.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Combine();
	}
}