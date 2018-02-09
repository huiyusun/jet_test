package edu.nyu.jet.support;

// create all possible subtype patterns for patterns between main argument types
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;

public class GetTypePatterns {

	public static void Separate() {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			input = new BufferedReader(new FileReader(
					"/Users/nuist/documents/NlpResearch/ice-eval/results_subtypes_individual_art"));
			output = new BufferedWriter(new FileWriter(
					"/Users/nuist/documents/NlpResearch/ice-eval/results_subtypes_individual_art_changed"));

			while ((inputLine = input.readLine()) != null) {
				if (!inputLine.contains("= SAME =")) // YES or NO indicates that the pattern affects the score
					output.write(inputLine + "\n");
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