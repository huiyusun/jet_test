package edu.nyu.jet.weights;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class AssignWeightByFrequency {

	public static void AssignWeights(String combinedFile, String newWeightFile) {
		BufferedWriter output = null;

		try {
			output = new BufferedWriter(new FileWriter(newWeightFile));
			Set<String> combined = new TreeSet(Files.readAllLines(new File(combinedFile).toPath(), StandardCharsets.UTF_8));
			Map<String, Double> weights = new TreeMap<String, Double>();
			int count = 0;

			// assing new weights based on the frequency of occurence
			for (String line : combined) {
				String deps = line.split("=")[0].trim();
				String weight = line.split("=")[1].trim();
				int freq = Integer.parseInt(line.split("=")[2].trim());
				double newWeight = Double.parseDouble(weight);
				if (freq == 1) { // limit number of deps with frequency 1
					count++;
					if (count >= 30)
						continue;
				}

				// v1: 0.0-0.9, 1.1-2.0 & 1.1-2.0, 0.0-0.9
				if (weight.equals("0.0")) {
					if (freq <= 10) {
						newWeight = (freq - 1) / 10.0;
					} else if (freq > 10 && freq <= 100) {
						newWeight = 1 + (freq - 1) / 100.0;
					} else {
						newWeight = 2.0;
					}
				} else if (weight.equals("2.0")) {
					if (freq <= 10) {
						newWeight = 2 - (freq - 1) / 10.0;
					} else if (freq > 10 && freq <= 100) {
						newWeight = 1 - (freq - 1) / 100.0;
					} else {
						newWeight = 0.0;
					}
				}

				// v2: -1, 0, 0.5 & 3, 2, 1.5
				// if (weight.equals("0.0")) {
				// if (freq <= 5) {
				// newWeight = -1.0;
				// } else if (freq <= 10) {
				// newWeight = 0.0;
				// } else {
				// newWeight = 0.5;
				// }
				// } else if (weight.equals("2.0")) {
				// if (freq <= 5) {
				// newWeight = 3.0;
				// } else if (freq <= 10) {
				// newWeight = 2.0;
				// } else {
				// newWeight = 1.5;
				// }
				// }

				// v3: modified on v1
				// if (weight.equals("0.0")) {
				// if (freq < 20) {
				// newWeight = freq / 20.0; // between 0.0 and 1.0
				// } else {
				// newWeight = 2.0;
				// }
				// } else if (weight.equals("2.0")) {
				// if (freq < 20) {
				// newWeight = 2 - freq / 20.0; // between 1.0 and 2.0
				// } else {
				// newWeight = 0.0;
				// }
				// }

				weights.put(deps, newWeight);
			}

			// sort by values then output
			Map<String, Double> sorted = sortByValue(weights);
			for (String line : sorted.keySet())
				output.write(line + " = " + String.format("%.2f", sorted.get(line)) + "\n");

			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	public static void main(String[] args) {
	}
}