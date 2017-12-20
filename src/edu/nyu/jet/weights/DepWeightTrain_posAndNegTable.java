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

public class DepWeightTrain_posAndNegTable {

	private static List<String> posTable = new ArrayList<String>();

	private static List<String> negTable = new ArrayList<String>();

	private static Map<String, Double> depWeightsPos = new TreeMap<String, Double>();

	private static Map<String, Double> depWeightsNeg = new TreeMap<String, Double>();

	public static void loadPos(String posFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(posFile));
		String line = null;
		while ((line = br.readLine()) != null) {
			if (!line.isEmpty())
				posTable.add(line.split("=")[0].trim());
		}
		br.close();
	}

	public static void loadNeg(String negFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(negFile));
		String line = null;
		while ((line = br.readLine()) != null) {
			if (!line.isEmpty())
				negTable.add(line.split("=")[0].trim());
		}
		br.close();
	}

	public static void trainPos(String file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		// BufferedWriter debugWriter = new BufferedWriter(new FileWriter(file + "_debug"));

		// train dependency weights
		for (int i = 0; i < posTable.size(); i++) {
			for (int j = 0; j < posTable.size(); j++) {
				if (j <= i)
					continue; // prevent comparing the same paths twice

				// initializations
				String iPath = posTable.get(i);
				String jPath = posTable.get(j);

				List<String[]> nodes = getNodes(iPath, jPath); // convert paths into nodes

				if (nodes == null)
					continue;

				String[] iNodes = nodes.get(0);
				String[] jNodes = nodes.get(1);

				boolean isShort = false;
				if (iNodes.length == 1)
					isShort = true;

				// debugWriter.write("i: " + iPath + "\n");
				// debugWriter.write("j: " + jPath + "\n");

				// replace between nodes and see if the resulting path matches an existing path in the pos set
				for (int posn = 0; posn < iNodes.length; posn++) {
					String[] replaceNodes = Arrays.copyOf(iNodes, iNodes.length);

					if (!iNodes[posn].equals(jNodes[posn])) {
						replaceNodes[posn] = jNodes[posn];
						String replaceStr = String.join(",", replaceNodes).replaceAll(",", ":").replaceAll(":END_OF_PATH", "");
						String replacePath = iPath.split("--")[0] + "--" + replaceStr + "--" + iPath.split("--")[2];

						// debugWriter.write("replace: " + replacePath + '\n');

						if (replacePath.equals(jPath)) { // rest of the path matches except for node at posn
							depWeightsPos.put(iNodes[posn] + " -- " + jNodes[posn], 0.0);
							// debugWriter.write("replace1: " + replacePath + "\n");
						}

						List<String> tempTable = new ArrayList<String>(posTable);
						tempTable.remove(jPath);
						if (tempTable.contains(replacePath)) { // rest of pattern set contains the new pattern
							depWeightsPos.put(iNodes[posn] + " -- " + jNodes[posn], 0.0);
							// debugWriter.write("replace2: " + replacePath + "\n");
						}

						if (isShort) {// penalty for path with only one node
							depWeightsPos.put(iNodes[posn] + " -- " + jNodes[posn], 0.5);
							// debugWriter.write("short: " + replacePath + "\n");
						}

						if (negTable.contains(replacePath)) {
							depWeightsPos.put(iNodes[posn] + " -- " + jNodes[posn], 2.0);
							// debugWriter.write("neg: " + replacePath + "\n");
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

	public static void trainNeg(String file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		// BufferedWriter debugWriter = new BufferedWriter(new FileWriter(file + "_debug"));

		// train dependency weights
		for (int i = 0; i < posTable.size(); i++) {
			for (int j = 0; j < negTable.size(); j++) {
				if (j <= i)
					continue; // prevent comparing the same paths twice

				// initializations
				String iPath = posTable.get(i);
				String jPath = negTable.get(j);

				List<String[]> nodes = getNodes(iPath, jPath); // convert paths into nodes

				if (nodes == null)
					continue;

				String[] iNodes = nodes.get(0);
				String[] jNodes = nodes.get(1);

				boolean isShort = false;
				if (iNodes.length == 1)
					isShort = true;

				// debugWriter.write("i: " + iPath + "\n");
				// debugWriter.write("j: " + jPath + "\n");

				// replace between nodes and see if the resulting path matches an existing path in the pos set
				for (int posn = 0; posn < iNodes.length; posn++) {
					String[] replaceNodes = Arrays.copyOf(iNodes, iNodes.length);

					if (!iNodes[posn].equals(jNodes[posn])) {
						replaceNodes[posn] = jNodes[posn];
						String replaceStr = String.join(",", replaceNodes).replaceAll(",", ":").replaceAll(":END_OF_PATH", "");
						String replacePath = iPath.split("--")[0] + "--" + replaceStr + "--" + iPath.split("--")[2];

						// debugWriter.write("replace: " + replacePath + '\n');

						if (replacePath.equals(jPath)) { // rest of the path matches except for node at posn
							depWeightsNeg.put(iNodes[posn] + " -- " + jNodes[posn], 2.0);
							// debugWriter.write("replace1: " + replacePath + "\n");
						}

						List<String> tempTable = new ArrayList<String>(negTable);
						tempTable.remove(jPath);
						if (tempTable.contains(replacePath)) { // rest of pattern set contains the new pattern
							depWeightsNeg.put(iNodes[posn] + " -- " + jNodes[posn], 2.0);
							// debugWriter.write("replace2: " + replacePath + "\n");
						}

						if (isShort) {// penalty for path with only one node
							depWeightsNeg.put(iNodes[posn] + " -- " + jNodes[posn], 1.5);
							// debugWriter.write("short: " + replacePath + "\n");
						}

						if (posTable.contains(replacePath)) {
							depWeightsNeg.put(iNodes[posn] + " -- " + jNodes[posn], 0.5);
							// debugWriter.write("neg: " + replacePath + "\n");
						}
					}
				}
			}
		}

		// output weights to file
		Set<String> duplicates = new TreeSet<String>();

		for (String dep : depWeightsNeg.keySet()) {
			String firstDep = dep.split("--")[0].trim();
			String secondDep = dep.split("--")[1].trim();
			String score = depWeightsNeg.get(dep).toString();

			if (!duplicates.contains(firstDep + secondDep + score) && !duplicates.contains(secondDep + firstDep + score))
				writer.write(dep + " = " + depWeightsNeg.get(dep) + "\n");

			duplicates.add(firstDep + secondDep + score);
		}

		writer.close();
	}

	public static List<String[]> getNodes(String iPath, String jPath) {
		String iArg1 = iPath.split("--")[0];
		String[] iLex = iPath.split("--")[1].split(":");
		String iArg2 = iPath.split("--")[2];
		String jArg1 = jPath.split("--")[0];
		String[] jLex = jPath.split("--")[1].split(":");
		String jArg2 = jPath.split("--")[2];

		// if (iLex.length == 1 || jLex.length == 1)
		// return null; // path must have more than one dependency

		if (iLex.length != jLex.length)
			return null; // two paths must have the same length

		if (!iArg1.equals(jArg1) || !iArg2.equals(jArg2))
			return null; // two paths must have the same argument type

		// separate path into nodes where each node consists of 'dependency:word'
		String[] iNodes = new String[(iLex.length + 1) / 2];
		String[] jNodes = new String[(jLex.length + 1) / 2];

		for (int posn = 0; posn < (iLex.length - 1) / 2; posn++) {
			iNodes[posn] = iLex[2 * posn] + ":" + iLex[2 * posn + 1];
			// System.out.println(iNodes[posn]);
		}
		iNodes[(iLex.length - 1) / 2] = iLex[iLex.length - 1] + ":END_OF_PATH";
		// System.out.println(iNodes[(iLex.length - 1) / 2]);

		for (int posn = 0; posn < (jLex.length - 1) / 2; posn++) {
			jNodes[posn] = jLex[2 * posn] + ":" + jLex[2 * posn + 1];
			// System.out.println(jNodes[posn]);
		}
		jNodes[(jLex.length - 1) / 2] = jLex[jLex.length - 1] + ":END_OF_PATH";
		// System.out.println(jNodes[(jLex.length - 1) / 2]);

		List<String[]> nodes = new ArrayList<String[]>();
		nodes.add(iNodes);
		nodes.add(jNodes);

		return nodes;
	}

	public static void main(String[] args) throws IOException {
		String dir = "/Users/nuist/documents/NlpResearch/ice-eval/";

		// load positive and negative patterns
		loadPos(dir + "patterns_weights_dep.pos");
		loadNeg(dir + "patterns_weights_dep.neg");

		// train dependency weights
		trainPos(dir + "weights_dep_perfect_posTable");
		trainNeg(dir + "weights_dep_perfect_negTable");

		// combine posTable and negTable weights and resolve conflicting scores
		CombineDepWeights.Combine(dir + "weights_dep_perfect_posTable", dir + "weights_dep_perfect_negTable",
				dir + "weights_dep_perfect_combined");
	}
}