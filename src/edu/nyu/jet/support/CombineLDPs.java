package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;

public class CombineLDPs {

	public static void Combine() {
		BufferedReader input = null, input1 = null;
		BufferedWriter output = null;
		String line;

		try {
			input = new BufferedReader(new FileReader("/Users/nuist/Documents/NlpResearch/AL-oracle/relationOracleLDP_ltw"));
			input1 = new BufferedReader(new FileReader("/Users/nuist/Documents/NlpResearch/AL-oracle/relationOracleLDP_all"));

			output = new BufferedWriter(new FileWriter("/Users/nuist/Documents/NlpResearch/AL-oracle/relationOracleLDP"));

			HashSet<String> ldps = new HashSet<String>();

			while ((line = input.readLine()) != null) {
				ldps.add(line);
			}

			while ((line = input1.readLine()) != null) {
				ldps.add(line);
			}

			for (String ldp : ldps) {
				output.write(ldp + "\n");
			}

			input.close();
			input1.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Combine();
	}
}