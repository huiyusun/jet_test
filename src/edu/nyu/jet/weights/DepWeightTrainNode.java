package edu.nyu.jet.weights;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class DepWeightTrainNode {
	private static List<MatcherPath> posTable;
	private static List<MatcherPath> negTable;

	public static int loadPos(String rulesFile, int limit) throws IOException {
		posTable = new ArrayList<MatcherPath>();
		BufferedReader br = new BufferedReader(new FileReader(rulesFile));
		String line = null;
		int count = 0;
		while ((line = br.readLine()) != null) {
			if (count >= limit)
				break;
			String[] parts = line.split("=");
			MatcherPath path = new MatcherPath(parts[0].trim());
			if (!path.isEmpty()) {
				path.setRelationType(parts[1].trim());
			}
			posTable.add(path);
			count++;
		}

		br.close();
		System.out.println("loaded " + count + " positive patterns");

		return count;
	}

	public static int loadNeg(String negRulesFile, int limit) throws IOException {
		negTable = new ArrayList<MatcherPath>();
		BufferedReader br = new BufferedReader(new FileReader(negRulesFile));
		String line = null;
		int count = 0;
		while ((line = br.readLine()) != null) {
			if (count >= limit)
				break;
			String[] parts = line.split("=");
			MatcherPath path = new MatcherPath(parts[0].trim());
			if (!path.isEmpty()) {
				path.setRelationType(parts[1].trim());
			}
			negTable.add(path);
			count++;
		}

		br.close();
		System.out.println("loaded " + count + " negative patterns");

		return count;
	}

	public static void train(String file, List<MatcherPath> table1, List<MatcherPath> table2, List<MatcherPath> negTable)
			throws IOException {
		Map<String, Integer> depWeightsPos = new TreeMap<String, Integer>();
		Map<String, Integer> depWeightsNeg = new TreeMap<String, Integer>();

		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		List<String> negTableStr = new ArrayList<String>();
		if (negTable != null) {
			for (MatcherPath path : negTable) {
				negTableStr.add(path.toString());
			}
		}
		// train dependency weights
		for (int i = 0; i < table1.size(); i++) {
			for (int j = 0; j < table2.size(); j++) {
				if (negTable != null) {
					if (j <= i)
						continue; // prevent comparing the same paths twice
				}

				// initializations
				MatcherPath iPath = table1.get(i);
				MatcherPath jPath = table2.get(j);

				if (iPath.nodes.size() == 1 || jPath.nodes.size() == 1)
					continue; // path must have more than one dependency

				if (iPath.nodes.size() != jPath.nodes.size())
					continue; // two paths must have the same length

				if (!iPath.arg1Type.equals(jPath.arg1Type) || !iPath.arg2Type.equals(jPath.arg2Type))
					continue; // two paths must have the same argument type

				// System.out.println("i: " + iPath.toString());
				// System.out.println("j: " + jPath.toString());

				// replace between nodes and see if the resulting path matches an existing path in the pos set
				for (int posn = 0; posn < iPath.length(); posn++) {
					MatcherNode iNode = iPath.nodes.get(posn);
					MatcherNode jNode = jPath.nodes.get(posn);

					MatcherPath replacePath = new MatcherPath(iPath.toString());

					if (!iNode.equals(jNode)) {
						replacePath.setNode(jNode, posn);
						String iDep = iNode.label + ":" + iNode.token;
						String jDep = jNode.label + ":" + jNode.token;

						if (replacePath.toString().equals(jPath.toString())) { // rest of path matches except for node at posn
							if (!depWeightsPos.containsKey(iDep + " -- " + jDep)
									&& !depWeightsPos.containsKey(jDep + " -- " + iDep)) {
								depWeightsPos.put(iDep + " -- " + jDep, 1);
							} else if (depWeightsPos.containsKey(iDep + " -- " + jDep)) {
								depWeightsPos.put(iDep + " -- " + jDep, depWeightsPos.get(iDep + " -- " + jDep) + 1);
							} else if (depWeightsPos.containsKey(jDep + " -- " + iDep)) {
								depWeightsPos.put(jDep + " -- " + iDep, depWeightsPos.get(jDep + " -- " + iDep) + 1);
							}
							// System.out.println("replace1: " + replacePath.toString());
						}

						if (negTable != null) {
							if (negTableStr.contains(replacePath.toString())) {
								if (!depWeightsNeg.containsKey(iDep + " -- " + jDep)
										&& !depWeightsNeg.containsKey(jDep + " -- " + iDep)) {
									depWeightsNeg.put(iDep + " -- " + jDep, 1);
								} else if (depWeightsNeg.containsKey(iDep + " -- " + jDep)) {
									depWeightsNeg.put(iDep + " -- " + jDep, depWeightsNeg.get(iDep + " -- " + jDep) + 1);
								} else if (depWeightsNeg.containsKey(jDep + " -- " + iDep)) {
									depWeightsNeg.put(jDep + " -- " + iDep, depWeightsNeg.get(jDep + " -- " + iDep) + 1);
								} // System.out.println("replace3: " + replacePath.toString());
							}
						}
					}
				}
			}
		}

		// output weights to file
		Map<String, Integer> sorted1 = sortByValue(depWeightsPos);
		Map<String, Integer> sorted2 = sortByValue(depWeightsNeg);
		Set<String> remove = new HashSet<String>();

		for (String dep : sorted1.keySet()) { // resolve conflicting deps
			String arg1 = dep.split("--")[0].trim();
			String arg2 = dep.split("--")[1].trim();
			if (sorted2.containsKey(arg1 + " -- " + arg2) || sorted2.containsKey(arg2 + " -- " + arg1)) {
				remove.add(arg1 + " -- " + arg2);
				remove.add(arg2 + " -- " + arg1);
				System.out.println("conflicting deps: " + arg1 + " -- " + arg2);
			}
		}

		sorted1.keySet().removeAll(remove);
		sorted2.keySet().removeAll(remove);

		double weight = 0.0;
		if (negTable == null)
			weight = 2.0;

		for (String dep : sorted1.keySet()) {
			writer.write(dep + " = " + weight + " = " + sorted1.get(dep) + "\n");
		}
		for (String dep : sorted2.keySet()) {
			writer.write(dep + " = " + 2.0 + " = " + sorted2.get(dep) + "\n");
		}

		writer.close();
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
		String dir = "/Users/nuist/documents/NlpResearch/ice-eval/";

		// load positive and negative patterns
		loadPos(dir + "patterns_node.pos", 100000);
		loadNeg(dir + "patterns_node.neg", 100000);

		// train dependency weights
		String v = "node_v1"; // e.g. v1, perfect, real, etc.
		// pick one: (pos, pos, neg), (neg, neg, pos), (pos, neg, null)
		train(dir + "weights_" + v + "_negTable", negTable, negTable, posTable);

		// combine weights trained and resolve conflicting scores
		CombineDepWeights.Combine(dir + "weights_" + v + "_posTable", dir + "weights_" + v + "_negTable",
				dir + "weights_" + v + "_mixTable", dir + "weights_" + v + "_combined");
	}
}