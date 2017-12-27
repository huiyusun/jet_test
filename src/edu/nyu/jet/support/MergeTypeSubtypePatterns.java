package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.TreeSet;

public class MergeTypeSubtypePatterns {

	public static void Clean() {
		BufferedReader input = null;
		BufferedWriter output = null;
		String line;

		try {
			input = new BufferedReader(
					new FileReader("/Users/nuist/documents/NlpResearch/ice-eval/patterns_perfect_subtypes.neg"));
			output = new BufferedWriter(new FileWriter("/Users/nuist/documents/NlpResearch/ice-eval/patterns_perfect.neg"));

			TreeSet<String> set = new TreeSet<String>();

			while ((line = input.readLine()) != null) {
				String path = line.split("=")[0].trim();
				String type = line.split("=")[1].trim();
				String arg1 = path.split("--")[0].trim();
				String deps = path.split("--")[1].trim();
				String arg2 = path.split("--")[2].trim();

				String arg1Type = arg1.split(":")[0];
				String arg2Type = arg2.split(":")[0];

				String newPattern = arg1Type + "--" + deps + "--" + arg2Type + " = " + type;
				set.add(newPattern);
			}

			for (String pattern : set)
				output.write(pattern + "\n");

			input.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// "/Users/nuist/documents/NlpResearch/AL-patterns/art/itr1"
		// "/Users/nuist/documents/workspaceNLP/jet_master/data/ldpRelationModel"
		Clean();
	}
}