package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;

public class CheckWhetherDuplicateExists {

	public static void Clean() {
		BufferedReader input = null;
		String inputLine;
		boolean duplicate = false;
		int count = 0;

		try {
			input = new BufferedReader(new FileReader(
					"/Users/nuist/documents/NlpResearch/Oracle-AL/NearestNeighborMatch_LDPs/PHYS/patterns_v0.pos"));

			HashSet<String> relationsSet = new HashSet<String>();

			while ((inputLine = input.readLine()) != null) {
				if (!inputLine.isEmpty()) {
					if (relationsSet.contains(inputLine)) {
						System.out.println(inputLine);
						duplicate = true;
						count++;
					}

					relationsSet.add(inputLine);
				}
			}

			if (duplicate) {
				System.out.println("File contains " + count + " Duplicates");
			} else {
				System.out.println("File does not contain Duplicates");
			}

			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Clean();
	}
}