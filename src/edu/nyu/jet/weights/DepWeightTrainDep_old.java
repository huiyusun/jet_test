package edu.nyu.jet.weights;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class DepWeightTrainDep_old {

	private static List<MatcherPath> posTable = new ArrayList<MatcherPath>();
	private static List<MatcherPath> negTable = new ArrayList<MatcherPath>();

	private static List<String> posTableStr = new ArrayList<String>();
	private static List<String> negTableStr = new ArrayList<String>();

	private static Map<String, Double> depWeightsPos = new TreeMap<String, Double>();
	private static Map<String, Double> depWeightsNeg = new TreeMap<String, Double>();

	public static void loadPos(String rulesFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(rulesFile));
		String line = null;
		int count = 0;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split("=");
			MatcherPath path = new MatcherPath(parts[0].trim());
			if (parts[0].contains("EMPTY")) {
				continue;
			}
			if (!path.isEmpty()) {
				path.setRelationType(parts[1].trim());
			}
			posTable.add(path);
			count++;
		}

		br.close();
		System.out.println("loaded " + count + " positive patterns");
	}

	public static void loadNeg(String negRulesFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(negRulesFile));
		String line = null;
		int count = 0;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split("=");
			MatcherPath path = new MatcherPath(parts[0].trim());
			if (parts[0].contains("EMPTY")) {
				continue;
			}
			if (!path.isEmpty()) {
				path.setRelationType(parts[1].trim());
			}
			negTable.add(path);
			count++;
		}

		br.close();
		System.out.println("loaded " + count + " negative patterns");
	}

	public static void trainPos(String file) throws IOException, CloneNotSupportedException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		tableToString(); // convert from MatcherPath to string for comparision

		// train dependency weights
		for (int i = 0; i < posTable.size(); i++) {
			for (int j = 0; j < posTable.size(); j++) {
				if (j <= i)
					continue; // prevent comparing the same paths twice

				// initializations
				MatcherPath iPath = posTable.get(i);
				MatcherPath jPath = posTable.get(j);

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
					String iDep = iPath.nodes.get(posn).label;
					String jDep = jPath.nodes.get(posn).label;

					MatcherPath replacePath = new MatcherPath(iPath.toString());

					if (!iDep.equals(jDep)) {
						replacePath.setLabel(jDep, posn);

						// System.out.println("replace: " + replacePath.toString());

						if (replacePath.toString().equals(jPath.toString())) { // rest of path matches except for node at posn
							depWeightsPos.put(iDep + " -- " + jDep, 0.0);
							// System.out.println("replace1: " + replacePath.toString());
						}

						List<String> tempTable = new ArrayList<String>(posTableStr);
						tempTable.remove(jPath.toString());
						if (tempTable.contains(replacePath.toString())) { // rest of pattern set contains the new pattern
							depWeightsPos.put(iDep + " -- " + jDep, 0.0);
							// System.out.println("replace2: " + replacePath.toString());
						}

						if (negTableStr.contains(replacePath.toString())) {
							depWeightsPos.put(iDep + " -- " + jDep, 2.0);
							// System.out.println("replace3: " + replacePath.toString());
						}
					}
				}
			}
		}

		// output weights to file
		Set<String> duplicates = new TreeSet<String>();

		for (String dep : depWeightsPos.keySet()) {
			String firstDep = dep.split("--")[0].trim();
			String secondDep = dep.split("--")[1].trim();
			String score = depWeightsPos.get(dep).toString();

			if (!duplicates.contains(firstDep + secondDep + score) && !duplicates.contains(secondDep + firstDep + score))
				writer.write(dep + " = " + depWeightsPos.get(dep) + "\n");

			duplicates.add(firstDep + secondDep + score);
		}

		writer.close();
	}

	public static void tableToString() {
		for (MatcherPath path : posTable) {
			posTableStr.add(path.toString());
		}

		for (MatcherPath path : negTable) {
			negTableStr.add(path.toString());
		}
	}

	public static void main(String[] args) throws IOException, CloneNotSupportedException {
		String dir = "/Users/nuist/documents/NlpResearch/ice-eval/";

		// load positive and negative patterns
		loadPos(dir + "patterns_weights_dep.pos");
		loadNeg(dir + "patterns_weights_dep.neg");

		// train dependency weights
		String v = "dep_v1"; // e.g. v1, perfect, real, etc.

		trainPos(dir + "weights_" + v + "_posTable");
		// trainNeg(dir + "weights_" + v + "_negTable");

		// combine posTable and negTable weights and resolve conflicting scores
		// CombineDepWeights.Combine(dir + "weights_" + v + "_posTable", dir + "weights_" + v + "_negTable",
		// dir + "weights_" + v + "_combined");
	}
}