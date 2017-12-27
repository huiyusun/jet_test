package edu.nyu.jet.weights;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class CombineDepWeights {

	public static void Combine(String posFile, String negFile, String mixFile, String combinedFile) {
		BufferedWriter output = null;

		try {
			output = new BufferedWriter(new FileWriter(combinedFile));
			Set<String> setPos = new TreeSet(Files.readAllLines(new File(posFile).toPath(), StandardCharsets.UTF_8));
			Set<String> setNeg = new TreeSet(Files.readAllLines(new File(negFile).toPath(), StandardCharsets.UTF_8));
			Set<String> setMix = new TreeSet(Files.readAllLines(new File(mixFile).toPath(), StandardCharsets.UTF_8));

			Set<String> combined = new TreeSet<String>();
			Set<String> removed = new TreeSet<String>();
			Map<String, Map<String, Integer>> depWeights = new HashMap<String, Map<String, Integer>>();

			combined.addAll(setPos);
			combined.addAll(setNeg);
			combined.addAll(setMix);

			for (String line : combined) {
				String deps = line.split("=")[0].trim();
				String dep1 = deps.split("--")[0].trim();
				String dep2 = deps.split("--")[1].trim();
				String weight = line.split("=")[1].trim();
				int count = Integer.parseInt(line.split("=")[2].trim());
				Map<String, Integer> values = new HashMap<String, Integer>();

				if (!depWeights.containsKey(dep1 + " -- " + dep2) && !depWeights.containsKey(dep2 + " -- " + dep1)) {
					values.put(weight, count);
					depWeights.put(dep1 + " -- " + dep2, values);
				} else if (depWeights.containsKey(dep1 + " -- " + dep2)) {
					values = depWeights.get(dep1 + " -- " + dep2);

					if (values.keySet().iterator().next().equals(weight)) { // same dependency and same weights: add
						Map<String, Integer> newValues = new HashMap<String, Integer>();
						int newCount = values.get(weight) + count;
						newValues.put(weight, newCount);
						depWeights.put(dep1 + " -- " + dep2, newValues);
					} else { // same dependency but different weights: remove
						removed.add(dep1 + " -- " + dep2);
						System.out.println("removed: " + dep1 + " -- " + dep2);
					}
				} else if (depWeights.containsKey(dep2 + " -- " + dep1)) {
					values = depWeights.get(dep2 + " -- " + dep1);

					if (values.keySet().iterator().next().equals(weight)) {
						Map<String, Integer> newValues = new HashMap<String, Integer>();
						int newCount = values.get(weight) + count;
						newValues.put(weight, newCount);
						depWeights.put(dep2 + " -- " + dep1, newValues);
					} else {
						removed.add(dep2 + " -- " + dep1);
						System.out.println("removed: " + dep2 + " -- " + dep1);
					}
				}
			}

			// remove conflicting dependencies
			depWeights.keySet().removeAll(removed);

			// sort by values then output to file
			Map<String, Integer> temp = new HashMap<String, Integer>();
			for (String dep : depWeights.keySet()) {
				Map<String, Integer> values = depWeights.get(dep);
				temp.put(dep + " = " + values.keySet().iterator().next(), values.values().iterator().next());
			}

			Map<String, Integer> sorted = sortByValue(temp);
			for (String dep : sorted.keySet())
				output.write(dep + " = " + sorted.get(dep) + "\n");

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