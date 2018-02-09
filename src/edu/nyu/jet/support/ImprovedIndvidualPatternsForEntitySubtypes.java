package edu.nyu.jet.support;

// separate individual patterns for entity subtypes where each main type pattern is followed by its subtypes patterns 
// and their performances
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.TreeSet;

public class ImprovedIndvidualPatternsForEntitySubtypes {

	public static void Separate() {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			input = new BufferedReader(
					new FileReader("/Users/nuist/documents/NlpResearch/ice-eval/results_subtypes_individual_neg_separated"));
			output = new BufferedWriter(
					new FileWriter("/Users/nuist/documents/NlpResearch/ice-eval/results_subtypes_individual_neg_improved"));

			HashSet<String> types = new HashSet<String>();
			TreeSet<String> subtypes = new TreeSet<String>();

			// all subtype patterns that perform better
			// while ((inputLine = input.readLine()) != null) {
			// if (inputLine.split(":")[0].equals("S"))
			// if (inputLine.split("=")[1].trim().equals("YES")) {
			// String path = inputLine.split("=")[0].replace("S: ", "").trim();
			// subtypes.add(path + " = " + "ORG-AFF");
			// }
			// }
			//
			// for (String subtype : subtypes)
			// output.write(subtype + "\n");

			// only include subtype patterns that perform better than its main types
			double typeScore = 1.0;
			double subtypeScore = 0.0;
			while ((inputLine = input.readLine()) != null) {
				if (inputLine.split(":")[0].equals("T")) {
					typeScore = Double.parseDouble(inputLine.split("=")[3].split("F:")[1].trim());
					if (inputLine.split("=")[2].trim().equals("NO")) {
						String path = inputLine.split("=")[0].replace("T: ", "").trim();
						String relationType = inputLine.split("=")[1].trim();

				if (inputLine.split("=")[2].trim().equals("NO"))
							subtypes.add(path + " = " + relationType);
					}
					// System.out.println("type" + typeScore);
				}

				if (inputLine.split(":")[0].equals("S")) {
					subtypeScore = Double.parseDouble(inputLine.split("=")[3].split("F:")[1].trim());
					// System.out.println("subtype" + subtypeScore);

				if (inputLine.split("=")[2].trim().equals("NO")) {
						String path = inputLine.split("=")[0].replace("S: ", "").trim();
						String pathType = path.split("--")[0].trim().split(":")[0] + "--" + path.split("--")[1].trim() + "--"
								+ path.split("--")[2].trim().split(":")[0];
						String relationType = inputLine.split("=")[1].trim();

						subtypes.add(path + " = " + relationType);
					}
				}
			}

			for (String subtype : subtypes)
				output.write(subtype + "\n");

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