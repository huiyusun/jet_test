package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;

public class OutputWordEmbeddingsExtVec {

	public static void Clean() {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			input = new BufferedReader(
					new FileReader("/Users/nuist/documents/NlpResearch/WordEmbeddings/wiki_extvec/wiki_extvec"));

			output = new BufferedWriter(
					new FileWriter("/Users/nuist/documents/NlpResearch/WordEmbeddings/wiki_extvec/wiki_extvec_short"));

			int i = 0;

			while ((inputLine = input.readLine()) != null) {
				if (!inputLine.isEmpty()) {
					if (inputLine.contains("nsubj_inv_rules")) {
						System.out.println("found");
						output.write(inputLine + "\n");
						i++;
						break;
					}
				}

				if (i >= 10000)
					break;
			}

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