package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class DependencyCount {

	public static void Clean() {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			// "/Users/nuist/documents/NlpResearch/Oracle-AL/NearestNeighborMatch_LDPs/STUDENT-ALUM/patterns_v0.neg"
			input = new BufferedReader(new FileReader(
					"/Users/nuist/documents/NlpResearch/Oracle-AL/relationOracleLDP.neg"));
			output = new BufferedWriter(new FileWriter(
					"/Users/nuist/documents/NlpResearch/Oracle-AL/NearestNeighborMatch_LDPs/dependencyCount_all", true));

			HashMap<String, Integer> dependency = new HashMap<String, Integer>();

			while ((inputLine = input.readLine()) != null) {
				if (!inputLine.isEmpty()) {
					String path = inputLine.split("=")[0].trim().split("--")[1].trim();
					String[] lexInPath = path.split(":");
					int length = lexInPath.length;

					for (int i = 0; i < length; i = i + 2) {
						if (dependency.containsKey(lexInPath[i])) {
							int newCount = dependency.get(lexInPath[i]) + 1;
							dependency.put(lexInPath[i], newCount);
						} else {
							dependency.put(lexInPath[i], 1);
						}
					}
				}
			}

			output.write(
					"ALL : relationOracleLDP.neg = " + entriesSortedByValues(dependency).size() + " dependencies" + "\n");
			output.write(entriesSortedByValues(dependency) + "\n");
			output.write("\n");

			input.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
		SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
				int res = e1.getValue().compareTo(e2.getValue());
				return res != 0 ? res : 1;
			}
		});
		sortedEntries.addAll(map.entrySet());
		return sortedEntries;
	}

	public static void main(String[] args) {
		// "/Users/nuist/documents/NlpResearch/AL-patterns/art/itr1"
		// "/Users/nuist/documents/workspaceNLP/jet_master/data/ldpRelationModel"
		Clean();
	}
}