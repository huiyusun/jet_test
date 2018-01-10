package edu.nyu.jet.scorer;

import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class ScoreAndGetIncorrectPatterns {

	public static void score() throws IOException {
		BufferedWriter writer = new BufferedWriter(
				new FileWriter("/Users/nuist/Documents/NlpResearch/ice-eval/acePatternsIncorrect_all"));

		// read in lists of triples
		String keyTriples = "/Users/nuist/documents/NlpResearch/ice-eval/aceKeyTriples";
		String responseTriples = "/Users/nuist/documents/NlpResearch/ice-eval/aceResponseTriples";

		Set<String> key = new TreeSet(Files.readAllLines(new File(keyTriples).toPath(), StandardCharsets.UTF_8));
		Set<String> responseTemp = new TreeSet(
				Files.readAllLines(new File(responseTriples).toPath(), StandardCharsets.UTF_8));

		HashMap<String, String> keyMap = new HashMap<String, String>();
		HashMap<String, String> responsePattern = new HashMap<String, String>();
		Set<String> responseIncorrect = new TreeSet<String>();
		Set<String> response = new TreeSet<String>();

		for (String line : key) {
			keyMap.put(line.split(":")[0] + ":" + line.split(":")[2], line.split(":")[1]); // (arg1:arg2, relationType)
		}
		for (String line : responseTemp) {
			responsePattern.put(line.split("\\|")[1].trim(), line.split("\\|")[0].trim()); // (triples, pattern)
			response.add(line.split("\\|")[1].trim());
		}

		for (String triple : response) {
			if (triple.contains("::"))
				continue;

			if (!triple.contains("=")) {
				String args = triple.split(":")[0] + ":" + triple.split(":")[2];
				String inverseArgs = triple.split(":")[2] + ":" + triple.split(":")[0];

				if (!keyMap.containsKey(args) && !keyMap.containsKey(inverseArgs)) {
					responseIncorrect.add(responsePattern.get(triple) + " = ORG-AFF");
				}
			} else if (triple.contains("=")) { // line contains multiple triples
				String[] triples = triple.split("=");
				boolean isIncorrect = true;
				for (int i = 0; i < triples.length; i++) {
					String args = triples[i].split(":")[0] + ":" + triples[i].split(":")[2];
					String inverseArgs = triples[i].split(":")[2] + ":" + triples[i].split(":")[0];

					if (keyMap.containsKey(args) || keyMap.containsKey(inverseArgs)) {
						isIncorrect = false;
					}
				}
				if (isIncorrect)
					responseIncorrect.add(responsePattern.get(triple) + " = ORG-AFF");
			}
		}

		List<String> dupes = new ArrayList<String>();
		for (String line : responseIncorrect) {
			if (!dupes.contains(line))
				writer.write(line + "\n");
			dupes.add(line);
			// writer.write(patterns.size() + " " + shortestPattern + " " + line + "\n");
		}

		writer.close();
	}

	public static void main(String[] args) throws IOException {
		score();
	}
}
