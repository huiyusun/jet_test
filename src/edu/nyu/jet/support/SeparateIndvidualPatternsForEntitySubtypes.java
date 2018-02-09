package edu.nyu.jet.support;

// separate individual patterns for entity subtypes where each main type pattern is followed by its subtypes patterns 
// and their performances
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;

public class SeparateIndvidualPatternsForEntitySubtypes {

	public static void Separate() {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			input = new BufferedReader(
					new FileReader("/Users/nuist/documents/NlpResearch/ice-eval/results_subtypes_individual_both"));
			output = new BufferedWriter(
					new FileWriter("/Users/nuist/documents/NlpResearch/ice-eval/results_subtypes_individual_both_separated"));

			HashSet<String> types = new HashSet<String>();
			HashSet<String> subtypes = new HashSet<String>();

			while ((inputLine = input.readLine()) != null) {
				if (inputLine.split(":")[0].equals("T"))
					types.add(inputLine.replace("T: ", ""));
				else if (inputLine.split(":")[0].equals("S"))
					subtypes.add(inputLine.replace("S: ", ""));
			}

			for (String type : types) {
				output.write("T: " + type + "\n");
				for (String subtype : subtypes) {
					String path = subtype.split("=")[0].trim();
					String mainPath = path.split("--")[0].split(":")[0] + "--" + path.split("--")[1] + "--"
							+ path.split("--")[2].split(":")[0];
					if (type.contains(mainPath))
						output.write("S: " + subtype + "\n");
				}
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