package edu.nyu.jet.scorer;

import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class ScoreAceTriplesCountPatterns {

	public static String score() throws IOException {
		BufferedWriter writer = new BufferedWriter(
				new FileWriter("/Users/nuist/Documents/NlpResearch/ice-eval/patternsCorrectCount"));
		BufferedWriter writerIncorrect = new BufferedWriter(
				new FileWriter("/Users/nuist/Documents/NlpResearch/ice-eval/patternsIncorrectCount"));

		// read in lists of triples
		String keyTriples = "/Users/nuist/documents/NlpResearch/ice-eval/aceKeyTriples";
		String responseTriples = "/Users/nuist/documents/NlpResearch/ice-eval/aceResponseTriples";

		Set<String> key = new TreeSet(Files.readAllLines(new File(keyTriples).toPath(), StandardCharsets.UTF_8));
		Set<String> responseTemp = new TreeSet(
				Files.readAllLines(new File(responseTriples).toPath(), StandardCharsets.UTF_8));

		HashMap<String, String> responsePattern = new HashMap<String, String>();
		Set<String> response = new TreeSet<String>();

		for (String line : responseTemp) {
			responsePattern.put(line.split("\\|")[1].trim(), line.split("\\|")[0].trim()); // (triples, pattern)
			response.add(line.split("\\|")[1].trim());
		}

		// Resolve triples with more than one names
		Set<String> temp = new TreeSet<String>();
		Iterator<String> itr = response.iterator();

		while (itr.hasNext()) {
			String triple = itr.next();
			if (triple.contains("=")) { // line contains multiple triples
				boolean isRemove = false;
				String[] triples = triple.split("=");

				for (int i = 0; i < triples.length; i++) {
					if (key.contains(triples[i])) {
						temp.add(triples[i]); // could be multiple hits for one triple line
						responsePattern.put(triples[i], responsePattern.get(triple));
						isRemove = true;
					}
				}

				if (isRemove == true) {
					responsePattern.remove(triple);
					itr.remove();
				}
			}
		}

		if (temp.size() > 0) {
			response.addAll(temp);
		}

		// get pattern correct and incorrect count
		HashMap<String, Integer> patternCorrect = new HashMap<String, Integer>();
		HashMap<String, Integer> patternIncorrect = new HashMap<String, Integer>();

		Set<String> correct = new TreeSet<String>(response);
		correct.retainAll(key);
		for (String triple : correct) {
			String pattern = responsePattern.get(triple);

			if (!patternCorrect.containsKey(pattern)) {
				patternCorrect.put(pattern, 1);
			} else {
				patternCorrect.put(pattern, patternCorrect.get(pattern) + 1);
			}
		}

		Set<String> incorrect = new TreeSet<String>(response);
		incorrect.removeAll(key);
		for (String triple : incorrect) {
			String pattern = responsePattern.get(triple);

			if (!patternIncorrect.containsKey(pattern)) {
				patternIncorrect.put(pattern, 1);
			} else {
				patternIncorrect.put(pattern, patternIncorrect.get(pattern) + 1);
			}
		}

		Map<String, Integer> patternCorrectSorted = sortByValue(patternCorrect);
		Map<String, Integer> patternIncorrectSorted = sortByValue(patternIncorrect);

		// get correct count
		int total = 0;
		for (String count : patternCorrectSorted.keySet()) {
			writer.write(count + " | " + patternCorrectSorted.get(count) + " "
					+ (patternIncorrectSorted.get(count) == null ? 0 : patternIncorrectSorted.get(count)) + "\n");
			total += patternCorrectSorted.get(count);
		}
		System.out.println("Total Correct: " + total);

		// get incorrect count
		int totalIncorrect = 0;
		for (String count : patternIncorrectSorted.keySet()) {
			writerIncorrect.write(count + " | " + patternIncorrectSorted.get(count) + " "
					+ (patternCorrectSorted.get(count) == null ? 0 : patternCorrectSorted.get(count)) + "\n");
			totalIncorrect += patternIncorrectSorted.get(count);
		}
		System.out.println("Total Incorrect: " + totalIncorrect);

		// compute recall:
		// of triples found by LDC (key),
		// fraction also found by system (correct = response ^ key)
		float recall = (float) correct.size() / (float) key.size();
		System.out.println("key = " + key.size());
		System.out.println("response = " + response.size());
		System.out.println("correct = " + correct.size());
		System.out.println("RECALL = " + recall);

		// compute precision:
		// of triples found by system (response),
		// fraction classified correct (correct)
		float precision = (float) correct.size() / (float) response.size();
		System.out.println("PRECISION = " + precision);

		// computer f1 score
		float f1 = 2 * (recall * precision) / (recall + precision);
		System.out.println("F1 = " + f1);

		writer.close();
		writerIncorrect.close();

		return recall + " " + precision + " " + f1;
	}

	// sort map by values in descending order
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static void main(String[] args) throws IOException {
		score();
	}
}
