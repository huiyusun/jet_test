package edu.nyu.jet.models;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * PathMatcher is a Edit-Distance-based matcher that produces an alignment and an alignment score between two
 * MatcherPaths using the generalized Levenshtein algorithm. It can optionally use word embeddings to compute the
 * substitution cost, if embeddings is set.
 */
public class PathMatcher_depWeights {

	private TObjectDoubleHashMap weights = new TObjectDoubleHashMap();
	private TObjectDoubleHashMap labelWeights = new TObjectDoubleHashMap();
	private TObjectDoubleHashMap wordWeights = new TObjectDoubleHashMap();

	private Map<String, double[]> embeddings = null;

	public static double labelMismatchCost = 2.5;

	private static HashMap<String, Double> depWeights = new HashMap<String, Double>();

	public PathMatcher_depWeights() {
		setWeights();
	}

	public void setWeights() {
		weights.put("replace", 1.0);
		weights.put("insert", 1.0);
		weights.put("delete", 1.0);
	}

	public void setEmbeddings(Map<String, double[]> embeddings) {
		this.embeddings = embeddings;
	}

	public void updateLabelMismatchCost(double cost) {
		labelMismatchCost = cost;
	}

	public void updateWeights(double replace, double insert, double delete) {
		weights.put("replace", replace);
		weights.put("insert", insert);
		weights.put("delete", delete);
	}

	public static void loadDepWeights(String file) throws IOException {
		String line = null;
		int count = 0;

		BufferedReader br = new BufferedReader(new FileReader(file));

		while ((line = br.readLine()) != null) {
			if (!line.isEmpty()) {
				String dep = line.split("=")[0].trim();
				Double score = Double.parseDouble(line.split("=")[1].trim());

				depWeights.put(dep, score);
				count++;
			}
		}

		br.close();
		System.out.println("loaded " + count + " dependency weights");
	}

	public double matchPaths(String path1, String path2) {
		MatcherPath matcherPath1 = new MatcherPath(path1);
		MatcherPath matcherPath2 = new MatcherPath(path2);

		return matchPaths(matcherPath1, matcherPath2);
	}

	public double matchPaths(MatcherPath matcherPath1, MatcherPath matcherPath2) {
		int len1 = matcherPath1.nodes.size();
		int len2 = matcherPath2.nodes.size();

		if (len1 == 1 && len2 == 1) {
			if (!matcherPath1.arg1Type.equals(matcherPath2.arg1Type) || !matcherPath1.arg2Type.equals(matcherPath2.arg2Type))
				return 1;

			if (matcherPath1.nodes.get(0).label.equals(matcherPath2.nodes.get(0).label))
				return 0;

			double depCost = 1;
			String node1 = matcherPath1.nodes.get(0).label + ":" + matcherPath1.nodes.get(0).token;
			String node2 = matcherPath2.nodes.get(0).label + ":" + matcherPath2.nodes.get(0).token;

			if (depWeights.containsKey(node1 + " -- " + node2)) {
				depCost = depWeights.get(node1 + " -- " + node2);
			} else if (depWeights.containsKey(node2 + " -- " + node1)) {
				depCost = depWeights.get(node2 + " -- " + node1);
			}

			return depCost;
		}

		// iterate to find min
		double[][] dp = new double[len1 + 1][len2 + 1];

		for (int i = 0; i <= len1; i++) {
			dp[i][0] = i;
		}

		for (int j = 0; j <= len2; j++) {
			dp[0][j] = j;
		}

		// iterate though, and check last char
		for (int i = 0; i < len1; i++) {
			MatcherNode c1 = matcherPath1.nodes.get(i);
			for (int j = 0; j < len2; j++) {
				MatcherNode c2 = matcherPath2.nodes.get(j);

				// if last two chars equal
				if (c1.equals(c2)) {
					// update dp value for +1 length
					dp[i + 1][j + 1] = dp[i][j];
				} else {
					double depCost = 1;

					String node1 = c1.label + ":" + c1.token;
					String node2 = c2.label + ":" + c2.token;

					if (depWeights.containsKey(node1 + " -- " + node2)) {
						depCost = depWeights.get(node1 + " -- " + node2);
					} else if (depWeights.containsKey(node2 + " -- " + node1)) {
						depCost = depWeights.get(node2 + " -- " + node1);
					}

					double replace = dp[i][j] + depCost;
					dp[i + 1][j + 1] = replace; // only allow replace operations

					// System.out.println("nodes: " + node1 + " " + node2);
					// System.out.println("replace dp[i][j]: " + replace + " " + dp[i + 1][j + 1]);
				}
			}
		}

		return matcherPath1.arg1Type.equals(matcherPath2.arg1Type) && matcherPath1.arg2Type.equals(matcherPath2.arg2Type)
				? dp[len1][len2] : Math.max(matcherPath1.length(), matcherPath2.length());
	}

}
