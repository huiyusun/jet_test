package edu.nyu.jet.scorer;

import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class ScoreAndGetCorrectPatterns {

	public static void score() throws IOException {
		BufferedWriter writer = new BufferedWriter(
				new FileWriter("/Users/nuist/Documents/NlpResearch/ice-eval/acePatterns_all"));

		// read in lists of triples
		String keyTriples = "/Users/nuist/documents/NlpResearch/ice-eval/aceKeyTriples_all";
		String responseTriples = "/Users/nuist/documents/NlpResearch/ice-eval/aceResponseTriples";

		Set<String> key = new TreeSet(Files.readAllLines(new File(keyTriples).toPath(), StandardCharsets.UTF_8));
		Set<String> responseTemp = new TreeSet(
				Files.readAllLines(new File(responseTriples).toPath(), StandardCharsets.UTF_8));

		HashMap<String, String> keyMap = new HashMap<String, String>();
		HashMap<String, String> responsePattern = new HashMap<String, String>();
		HashMap<String, List<String>> responseCorrect = new HashMap<String, List<String>>();
		Set<String> response = new TreeSet<String>();

		for (String line : key) {
			keyMap.put(line.split(":")[0] + ":" + line.split(":")[2], line.split(":")[1]); // (arg1:arg2, relationType)
		}
		for (String line : responseTemp) {
			responsePattern.put(line.split("\\|")[1].trim(), line.split("\\|")[0].trim()); // (triples, pattern)
			response.add(line.split("\\|")[1].trim());
		}

		for (String triple : response) {
			if (!triple.contains("=")) {
				String args = triple.split(":")[0] + ":" + triple.split(":")[2];
				String inverseArgs = triple.split(":")[2] + ":" + triple.split(":")[0];

				if (keyMap.containsKey(args)) {
					String tri = triple.split(":")[0] + ":" + keyMap.get(args) + ":" + triple.split(":")[2];
					if (!responseCorrect.containsKey(tri)) {
						List<String> patterns = new ArrayList<String>();
						patterns.add(responsePattern.get(triple) + " = " + keyMap.get(args));
						responseCorrect.put(tri, patterns);
					} else {
						List<String> patterns = responseCorrect.get(tri);
						patterns.add(responsePattern.get(triple) + " = " + keyMap.get(args));
						responseCorrect.put(tri, patterns);
					}
				} else if (keyMap.containsKey(inverseArgs)) {
					String tri = triple.split(":")[2] + ":" + keyMap.get(inverseArgs) + ":" + triple.split(":")[0];
					if (!responseCorrect.containsKey(tri)) {
						List<String> patterns = new ArrayList<String>();
						patterns.add(responsePattern.get(triple) + " = " + keyMap.get(inverseArgs));
						responseCorrect.put(tri, patterns);
					} else {
						List<String> patterns = responseCorrect.get(tri);
						patterns.add(responsePattern.get(triple) + " = " + keyMap.get(inverseArgs));
						responseCorrect.put(tri, patterns);
					}
				}
			} else if (triple.contains("=")) { // line contains multiple triples
				String[] triples = triple.split("=");
				for (int i = 0; i < triples.length; i++) {
					String args = triples[i].split(":")[0] + ":" + triples[i].split(":")[2];
					String inverseArgs = triples[i].split(":")[2] + ":" + triples[i].split(":")[0];

					if (keyMap.containsKey(args)) {
						String tri = triples[i].split(":")[0] + ":" + keyMap.get(args) + ":" + triples[i].split(":")[2];
						if (!responseCorrect.containsKey(tri)) {
							List<String> patterns = new ArrayList<String>();
							patterns.add(responsePattern.get(triple) + " = " + keyMap.get(args));
							responseCorrect.put(tri, patterns);
						} else {
							List<String> patterns = responseCorrect.get(tri);
							patterns.add(responsePattern.get(triple) + " = " + keyMap.get(args));
							responseCorrect.put(tri, patterns);
						}
					} else if (keyMap.containsKey(inverseArgs)) {
						String tri = triples[i].split(":")[2] + ":" + keyMap.get(inverseArgs) + ":" + triples[i].split(":")[0];
						if (!responseCorrect.containsKey(tri)) {
							List<String> patterns = new ArrayList<String>();
							patterns.add(responsePattern.get(triple) + " = " + keyMap.get(inverseArgs));
							responseCorrect.put(tri, patterns);
						} else {
							List<String> patterns = responseCorrect.get(tri);
							patterns.add(responsePattern.get(triple) + " = " + keyMap.get(inverseArgs));
							responseCorrect.put(tri, patterns);
						}
					}
				}
			}
		}

		List<String> dupes = new ArrayList<String>();
		for (String line : responseCorrect.keySet()) {
			List<String> patterns = responseCorrect.get(line);
			int shortest = 999;
			String shortestPattern = null;
			for (String pattern : patterns) {
				int len = pattern.length();
				if (len < shortest) {
					shortest = len;
					shortestPattern = pattern;
				}
			}
			if (!dupes.contains(shortestPattern))
				writer.write(shortestPattern + "\n");
			dupes.add(shortestPattern);
			// writer.write(patterns.size() + " " + shortestPattern + " " + line + "\n");
		}

		writer.close();
	}

	public static void main(String[] args) throws IOException {
		score();
	}
}
