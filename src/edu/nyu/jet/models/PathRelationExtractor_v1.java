package edu.nyu.jet.models;

import opennlp.model.Event;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import javax.management.relation.RelationType;

/**
 * Tag relations in a document by using positive and negative dependency path rules created with ICE
 *
 * @author yhe
 */
public class PathRelationExtractor_v1 {

	public void setNegDiscount(double nDiscount) {
		negDiscount = nDiscount;
	}

	public void setKThreshold(double kThresh) {
		kThreshold = kThresh;
	}

	public void setK(int newK) {
		k = newK;
	}

	public static double kThreshold = 0.5;

	public static double negDiscount = 0.9;

	public static int k = 3; // k nearest neighbor

	private PathMatcher pathMatcher = new PathMatcher();

	private static List<MatcherPath> ruleTable;

	private static List<MatcherPath> negTable;

	public List<String> rules;

	public void updateWeights(double replace, double insert, double delete) {
		pathMatcher.updateWeights(replace, insert, delete);
	}

	public void updateLabelMismatchCost(double cost) {
		pathMatcher.updateLabelMismatchCost(cost);
	}

	/**
	 * Load positive patterns from file.
	 * 
	 * @param rulesFile
	 * @param limit
	 * @param load
	 *          which loading method to use: sequential loading, random loading, or individual loading
	 * @return one of the two: the total number of patterns loaded or the individual pattern loaded
	 */
	public static Object loadRules(String rulesFile, int limit, String load) throws IOException {
		ruleTable = new ArrayList<MatcherPath>();
		BufferedReader br = new BufferedReader(new FileReader(rulesFile));
		String line = null;
		int count = 0;

		if (load.toUpperCase().equals("SEQUENTIAL")) { // sequential load
			while ((line = br.readLine()) != null) {
				if (count >= limit)
					break;
				String[] parts = line.split("=");
				MatcherPath path = new MatcherPath(parts[0].trim());
				if (ruleTable.contains(path)) {
					ruleTable.get(ruleTable.indexOf(path)).addRelationType(parts[1].trim());
				} else {
					path.addRelationType(parts[1].trim());
					ruleTable.add(path);
				}
				count++;
			}

			br.close();
			System.out.println("loaded " + count + " positive patterns (sequential)");
			return count;
		} else if (load.toUpperCase().equals("RANDOM")) { // random load
			HashMap<Integer, String> pos = new HashMap<Integer, String>();
			int posCount = 1;
			while ((line = br.readLine()) != null) {
				pos.put(posCount, line);
				posCount++;
			}

			int requiredPos = 1;
			HashSet<String> posRand = new HashSet<String>();
			while (requiredPos <= limit && requiredPos < pos.size()) {
				int random = ThreadLocalRandom.current().nextInt(0, 1500);
				if (pos.containsKey(random)) {
					if (!posRand.contains(pos.get(random)))
						requiredPos++;
					posRand.add(pos.get(random));
				}
			}

			for (String pattern : posRand) {
				String[] parts = pattern.split("=");
				MatcherPath path = new MatcherPath(parts[0].trim());
				if (ruleTable.contains(path)) {
					ruleTable.get(ruleTable.indexOf(path)).addRelationType(parts[1].trim());
				} else {
					path.addRelationType(parts[1].trim());
					ruleTable.add(path);
				}
				count++;
			}

			br.close();
			System.out.println("loaded " + count + " positive patterns (random)");
			return count;
		} else if (load.toUpperCase().contains("INDIVIDUAL")) { // individual load
			boolean isCumu = false;
			if (load.toUpperCase().contains("CUMULATIVE"))
				isCumu = true;

			while ((line = br.readLine()) != null) {
				if (count < limit && isCumu) { // load all patterns up to limit
					String[] parts = line.split("=");
					MatcherPath path = new MatcherPath(parts[0].trim());
					if (ruleTable.contains(path)) {
						ruleTable.get(ruleTable.indexOf(path)).addRelationType(parts[1].trim());
					} else {
						path.addRelationType(parts[1].trim());
						ruleTable.add(path);
					}
				}

				if (count == limit) {
					String[] parts = line.split("=");
					MatcherPath path = new MatcherPath(parts[0].trim());
					if (ruleTable.contains(path)) {
						ruleTable.get(ruleTable.indexOf(path)).addRelationType(parts[1].trim());
					} else {
						path.addRelationType(parts[1].trim());
						ruleTable.add(path);
					}
					br.close();

					if (!isCumu)
						System.out.println("loaded the positive pattern at line " + (count + 1) + " (individual)");
					else
						System.out.println("loaded " + (count + 1) + " positive patterns" + " (individual)");

					int size = ruleTable.size() - 1;
					int size1 = ruleTable.get(size).relationType.size() - 1;
					if (!ruleTable.get(size).arg1Subtype.equals("UNK") && !ruleTable.get(size).arg2Subtype.equals("UNK"))
						return ruleTable.get(size).toStringSubtypes() + " = " + ruleTable.get(size).relationType.get(size1).trim();
					else
						return ruleTable.get(size).toString() + " = " + ruleTable.get(size).relationType.get(size1).trim();
				}

				count++;
			}
		}

		br.close();
		System.out.println("loaded " + count + " positive patterns");
		return null;
	}

	/**
	 * Load negative patterns from file.
	 * 
	 * @param rulesFile
	 * @param limit
	 * @param load
	 *          which loading method to use: sequential loading, random loading, or individual loading
	 * @return one of the two: the total number of patterns loaded or the individual pattern loaded
	 */
	public static Object loadNeg(String negRulesFile, int limit, String load) throws IOException {
		negTable = new ArrayList<MatcherPath>();
		BufferedReader br = new BufferedReader(new FileReader(negRulesFile));
		String line = null;
		int count = 0;

		if (load.toUpperCase().equals("SEQUENTIAL")) { // sequential load
			while ((line = br.readLine()) != null) {
				if (count >= limit)
					break;
				String[] parts = line.split("=");
				MatcherPath path = new MatcherPath(parts[0].trim());
				if (negTable.contains(path)) {
					negTable.get(negTable.indexOf(path)).addRelationType(parts[1].trim());
				} else {
					path.addRelationType(parts[1].trim());
					negTable.add(path);
				}
				count++;
			}

			br.close();
			System.out.println("loaded " + count + " negative patterns (sequential)");
			return count;
		} else if (load.toUpperCase().equals("RANDOM")) { // random load
			HashMap<Integer, String> pos = new HashMap<Integer, String>(); // pos here refers to neg
			int posCount = 1;
			while ((line = br.readLine()) != null) {
				pos.put(posCount, line);
				posCount++;
			}

			int requiredPos = 1;
			HashSet<String> posRand = new HashSet<String>();
			while (requiredPos <= limit && requiredPos < pos.size()) {
				int random = ThreadLocalRandom.current().nextInt(0, 1500);
				if (pos.containsKey(random)) {
					if (!posRand.contains(pos.get(random)))
						requiredPos++;
					posRand.add(pos.get(random));
				}
			}

			for (String pattern : posRand) {
				String[] parts = pattern.split("=");
				MatcherPath path = new MatcherPath(parts[0].trim());
				if (negTable.contains(path)) {
					negTable.get(negTable.indexOf(path)).addRelationType(parts[1].trim());
				} else {
					path.addRelationType(parts[1].trim());
					negTable.add(path);
				}
				count++;
			}

			br.close();
			System.out.println("loaded " + count + " negative patterns (random)");
			return count;
		} else if (load.toUpperCase().equals("INDIVIDUAL")) { // individual load
			while ((line = br.readLine()) != null) {
				if (count == limit) {
					String[] parts = line.split("=");
					MatcherPath path = new MatcherPath(parts[0].trim());
					if (!path.isEmpty()) {
						path.addRelationType(parts[1].trim());
					}
					negTable.add(path);

					br.close();
					System.out.println("loaded the negative pattern at line " + (count + 1) + " (individual)");
					if (!negTable.get(0).arg1Subtype.equals("UNK") && !negTable.get(0).arg2Subtype.equals("UNK"))
						return negTable.get(0).toStringSubtypes() + " = " + negTable.get(0).relationType.get(0).trim();
					else
						return negTable.get(0).toString() + " = " + negTable.get(0).relationType.get(0).trim();
				}
				count++;
			}
		}

		br.close();
		System.out.println("loaded " + count + " negative patterns");
		return null;
	}

	public void loadRules(String rulesFile) throws IOException {
		loadRules(rulesFile, 100000);
	}

	// pos sequential loading
	public static int loadRules(String rulesFile, int limit) throws IOException {
		return (Integer) loadRules(rulesFile, limit, "SEQUENTIAL");
	}

	public void loadNeg(String negRulesFile) throws IOException {
		loadNeg(negRulesFile, 100000);
	}

	// neg sequential loading
	public static int loadNeg(String rulesFile, int limit) throws IOException {
		return (Integer) loadNeg(rulesFile, limit, "SEQUENTIAL");
	}

	public void loadAllRules(String posFile, String negFile, int limit) throws IOException {
		rules = new ArrayList<String>();
		BufferedReader br1 = new BufferedReader(new FileReader(posFile));
		BufferedReader br2 = new BufferedReader(new FileReader(negFile));
		String line = null;
		int count = 0;

		while ((line = br1.readLine()) != null) {
			if (count > limit)
				break;
			if (!line.isEmpty()) {
				String path = line.split("=")[0].trim();
				String arg1 = path.split("--")[0].trim().split(":")[0].trim();
				String dep = path.split("--")[1].trim();
				String arg2 = path.split("--")[2].trim().split(":")[0].trim();
				rules.add(arg1 + "--" + dep + "--" + arg2);
				count++;
			}
		}
		while ((line = br2.readLine()) != null) {
			if (count > limit)
				break;
			if (!line.isEmpty()) {
				String path = line.split("=")[0].trim();
				String arg1 = path.split("--")[0].trim().split(":")[0].trim();
				String dep = path.split("--")[1].trim();
				String arg2 = path.split("--")[2].trim().split(":")[0].trim();
				rules.add(arg1 + "--" + dep + "--" + arg2);
				count++;
			}
		}

		br1.close();
		br2.close();
		System.out.println("Loaded " + count + " type rules for pattern checking");
	}

	public void loadEmbeddings(String embeddingFile) throws IOException {
		WordEmbedding.loadWordEmbedding(embeddingFile);
	}

	/**
	 * Predict the relation type of an Event. The context[] array of the Event should have the format [dependency path,
	 * arg1 type, arg2 type]
	 * 
	 * @param e
	 *          An OpenNLP context[]:label pair
	 * @return
	 */
	public List<String> predict(Event e) {
		String[] context = e.getContext();
		String depPath = context[0];
		String arg1Type = context[1];
		String arg2Type = context[2];
		String fullDepPath = arg1Type + "--" + depPath + "--" + arg2Type;
		MatcherPath matcherPath = new MatcherPath(fullDepPath);
		List<String> relationType = new ArrayList<String>();
		double knnScore = 0;
		double knnNegScore = 0;
		boolean enoughPosPatterns = false;
		boolean enoughNegPatterns = false;

		Map<Double, MatcherPath> posMap = new TreeMap<Double, MatcherPath>();
		Map<Double, MatcherPath> negMap = new TreeMap<Double, MatcherPath>();

		// compare candidate with positive paths
		for (MatcherPath rule : ruleTable) {
			// System.out.println("pos pattern: " + rule);
			// System.out.println("candidate pattern: " + matcherPath);
			double score = pathMatcher.matchPaths(matcherPath, rule); // normalized by the length of the path
			// System.out.println("pos score: " + score);
			if (score <= kThreshold) { // threshold for the k nearest neighbors
				posMap.put(score, rule);
			}
		}

		if (posMap.size() == 0)
			return null;

		int posCount = 0;
		for (Double score : posMap.keySet()) {
			knnScore += score;
			posCount++;
			relationType = posMap.get(score).getRelationType(); // get relation type
			if (posCount >= k) {
				knnScore = knnScore / k; // calculate average of the k smallest scores
				enoughPosPatterns = true;
				break;
			}
		}

		if (!enoughPosPatterns)
			knnScore = knnScore / posCount;

		// compare candidate with negative paths
		for (MatcherPath rule : negTable) {
			// System.out.println("neg pattern: " + rule);
			// System.out.println("candidate pattern: " + matcherPath);
			double score = pathMatcher.matchPaths(matcherPath, rule);
			// System.out.println("neg score: " + score);
			if (score <= kThreshold)
				negMap.put(score, rule);
		}

		int negCount = 0;
		for (Double score : negMap.keySet()) {
			knnNegScore += score;
			negCount++;
			if (negCount >= k) {
				knnNegScore = knnNegScore / k; // calculate average of the k smallest scores
				enoughNegPatterns = true;
				break;
			}
		}

		if (!enoughNegPatterns)
			knnNegScore = knnNegScore / negCount;

		if (negMap.size() == 0)
			knnNegScore = kThreshold;

		// get pos and neg rule
		MatcherPath posRule = posMap.get(posMap.keySet().iterator().next());
		String posRuleStr = null;
		if (!posRule.arg1Subtype.equals("UNK") || !posRule.arg2Subtype.equals("UNK"))
			posRuleStr = posRule.toStringSubtypes();
		else
			posRuleStr = posRule.toString();

		String negRuleStr = null;
		if (negMap.size() > 0) {
			MatcherPath negRule = negMap.get(negMap.keySet().iterator().next());
			if (!negRule.arg1Subtype.equals("UNK") || !negRule.arg2Subtype.equals("UNK"))
				negRuleStr = negRule.toStringSubtypes();
			else
				negRuleStr = negRule.toString();
		}

		if (knnScore <= knnNegScore * negDiscount) {
			System.err.println("[ACCEPT] Pos Score:" + knnScore);
			System.err.println("[ACCEPT] Neg Score:" + knnNegScore * negDiscount);
			System.err.println("[ACCEPT] Current:" + matcherPath.toStringSubtypes());
			System.err.println("[ACCEPT] Closest Pos Rule:" + posRuleStr);
			System.err.println("[ACCEPT] Predicted:" + relationType.toString());

			return relationType;
		}
		if (knnScore > knnNegScore * negDiscount) {
			System.err.println("[REJECT] Pos Score:" + knnScore);
			System.err.println("[REJECT] Neg Score:" + knnNegScore * negDiscount);
			System.err.println("[REJECT] Current:" + matcherPath.toStringSubtypes());
			System.err.println("[REJECT] Closest Neg Rule:" + negRuleStr);
			System.err.println("[REJECT] Predicted:" + relationType.toString());
		}
		return null;
	}
}
