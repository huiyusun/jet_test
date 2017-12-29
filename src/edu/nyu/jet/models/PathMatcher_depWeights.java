package edu.nyu.jet.models;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

	private static HashMap<String, Double> depWeights;

	private static HashMap<String, List<String>> typeSubtype = new HashMap<String, List<String>>();

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

	/**
	 * Load dependency replacement weights from file.
	 * 
	 * @param file
	 *          the file to load weights from
	 * @param limit
	 *          the max number of weights to be loaded
	 * @param freqLimit
	 *          max frequency cutoff point
	 * @param exactFreq
	 *          whether to use exact frequency or frequency cutoff
	 * @return the number of weights loaded
	 */
	public static int loadDepWeights(String file, int limit, int freqLimit, boolean exactFreq) throws IOException {
		depWeights = new HashMap<String, Double>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		int count = 0, frequency = 1;

		while ((line = br.readLine()) != null) {
			if (!line.isEmpty()) {
				if (count >= limit)
					break;
				String dep = line.split("=")[0].trim();
				double score = Double.parseDouble(line.split("=")[1].trim());
				if (line.split("=").length == 3) // if frequency is used
					frequency = Integer.parseInt(line.split("=")[2].trim());
				if (exactFreq) {
					if (frequency == freqLimit) { // exact frequency
						depWeights.put(dep, score);
						count++;
					}
				} else {
					if (frequency <= freqLimit) { // cutoff frequency
						depWeights.put(dep, score);
						count++;
					}
				}
			}
		}

		br.close();
		System.out.println("loaded " + count + " dependency weights");

		return count;
	}

	public static int loadDepWeights(String file, int start, int end) throws IOException {
		depWeights = new HashMap<String, Double>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		int count = 0;

		while ((line = br.readLine()) != null) {
			if (!line.isEmpty()) {
				count++;
				if (count <= start)
					continue;
				if (count > end)
					break;
				String dep = line.split("=")[0].trim();
				double score = Double.parseDouble(line.split("=")[1].trim());
				// int frequency = Integer.parseInt(line.split("=")[2].trim());
				// if (frequency >= 5 || count < 15) { // cutoff frequency and limit
				depWeights.put(dep, score);
				// }
			}
		}

		br.close();
		System.out.println("loaded " + (count - start - 1) + " dependency weights");

		return count;
	}

	public static void loadDepWeights(String file) throws IOException {
		loadDepWeights(file, 100000, 10000, false);
	}

	public static void entityTypeAndSubtypeMap(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;

		while ((line = br.readLine()) != null) {
			String type = line.split(":")[0].trim();
			String[] subtypes = line.split(":")[1].trim().split(",");
			List<String> subtypesList = new ArrayList<String>();
			for (int i = 0; i < subtypes.length; i++)
				subtypesList.add(subtypes[i].trim());

			typeSubtype.put(type, subtypesList);
		}

		br.close();
	}

	public double matchPaths(String path1, String path2) {
		MatcherPath matcherPath1 = new MatcherPath(path1);
		MatcherPath matcherPath2 = new MatcherPath(path2);

		return matchPaths(matcherPath1, matcherPath2);
	}

	// argument entity subtype matching
	private double matchSubtypes(String type1, String subtype1, String type2, String subtype2) {
		if (!subtype2.equals("UNK")) { // rule requires subtype match
			if (subtype1.equals(subtype2))
				return -1;
			else if (typeSubtype.get(type1).contains(subtype2))
				return 1;
			else
				return 2;
		} else { // rule requires type match
			if (type1.equals(type2))
				return 0;
			else
				return 2;
		}
	}

	// node matching using pre-trained node replacement weights
	private double matchNode(MatcherNode c1, MatcherNode c2) {
		double depCost = 1;

		String node1 = c1.label + ":" + c1.token;
		String node2 = c2.label + ":" + c2.token;

		if (depWeights.containsKey(node1 + " -- " + node2)) {
			depCost = depWeights.get(node1 + " -- " + node2);
		} else if (depWeights.containsKey(node2 + " -- " + node1)) {
			depCost = depWeights.get(node2 + " -- " + node1);
		}

		return depCost;
	}

	// dependency matching using pre-trained dependency replacement weights
	private double matchDependency(MatcherNode c1, MatcherNode c2) {
		double depCost = 1;

		String dep1 = c1.label;
		String dep2 = c2.label;

		if (depWeights.containsKey(dep1 + " -- " + dep2)) {
			depCost = depWeights.get(dep1 + " -- " + dep2);
		} else if (depWeights.containsKey(dep2 + " -- " + dep1)) {
			depCost = depWeights.get(dep2 + " -- " + dep1);
		}

		return depCost;
	}

	public double matchPaths(MatcherPath matcherPath1, MatcherPath matcherPath2) {
		// match subtype costs between arg1 pairs and arg2 pairs
		// double subtypeCostArg1 = matchSubtypes(matcherPath1.arg1Type, matcherPath1.arg1Subtype, matcherPath2.arg1Type,
		// matcherPath2.arg1Subtype);
		// double subtypeCostArg2 = matchSubtypes(matcherPath1.arg2Type, matcherPath1.arg2Subtype, matcherPath2.arg2Type,
		// matcherPath2.arg2Subtype);
		//
		// return subtypeCostArg1 + subtypeCostArg2;

		int len1 = matcherPath1.nodes.size();
		int len2 = matcherPath2.nodes.size();

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
					double depCost = matchDependency(c1, c2); // get dependency weights
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
