package edu.nyu.jet.models;

import opennlp.model.Event;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Tag relations in a document by using positive and negative dependency path rules created with ICE
 *
 * @author yhe
 */
public class PathRelationExtractor {

	public void setNegDiscount(double nDiscount) {
		negDiscount = nDiscount;
	}

	public void setMinThreshold(double minThresh) {
		minThreshold = minThresh;
	}

	public void setK(int newK) {
		k = newK;
	}

	public static double minThreshold = 0.5;

	public static double negDiscount = 0.9;

	public static int k = 3; // k nearest neighbor

	private PathMatcher pathMatcher = new PathMatcher();

	private List<MatcherPath> ruleTable = new ArrayList<MatcherPath>();

	private List<MatcherPath> negTable = new ArrayList<MatcherPath>();

	public void updateWeights(double replace, double insert, double delete) {
		pathMatcher.updateWeights(replace, insert, delete);
	}

	public void updateLabelMismatchCost(double cost) {
		pathMatcher.updateLabelMismatchCost(cost);
	}

	public void loadRules(String rulesFile) throws IOException {
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
			ruleTable.add(path);
			count++;
		}

		br.close();
		System.out.println("loaded " + count + " positive patterns");
	}

	public void loadNeg(String negRulesFile) throws IOException {
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
	public String predict(Event e) {
		String[] context = e.getContext();
		String depPath = context[0];
		String arg1Type = context[1];
		String arg2Type = context[2];
		String fullDepPath = arg1Type + "--" + depPath + "--" + arg2Type;
		MatcherPath matcherPath = new MatcherPath(fullDepPath);
		String relationType = null;
		double knnScore = 0;
		double knnNegScore = 0;
		boolean enoughPosPatterns = false;
		boolean enoughNegPatterns = false;

		Map<Double, String> posMap = new TreeMap<Double, String>();
		Map<Double, String> negMap = new TreeMap<Double, String>();

		// compare candidate with positive paths
		for (MatcherPath rule : ruleTable) {
			// System.out.println("pos pattern: " + rule);
			// System.out.println("candidate pattern: " + matcherPath);

			double score = pathMatcher.matchPaths(matcherPath, rule) / rule.length(); // normalized by the length of the path
			posMap.put(score, rule.getRelationType());
		}

		int posCount = 0;
		for (Double score : posMap.keySet()) {
			knnScore += score;
			relationType = posMap.get(score); // get relation type
			posCount++;
			if (posCount >= k) {
				knnScore = knnScore / k; // calculate average of the k smallest scores
				enoughPosPatterns = true;
				break;
			}
		}

		if (!enoughPosPatterns)
			knnScore = knnScore / posCount;

		// compare candidate with negative paths
		if (knnScore < minThreshold) {
			for (MatcherPath rule : negTable) {
				double score = pathMatcher.matchPaths(matcherPath, rule) / rule.length();
				negMap.put(score, rule.getRelationType());
			}
		} else {
			return null;
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

		if (knnScore < minThreshold && knnScore < knnNegScore * negDiscount) {
			System.err.println("[ACCEPT] Pos Score:" + knnScore);
			System.err.println("[ACCEPT] Neg Score:" + knnNegScore * negDiscount);
			System.err.println("[ACCEPT] Current:" + matcherPath);
			System.err.println("[ACCEPT] Actual:" + e.getOutcome() + "\tPredicted:" + relationType);

			return relationType;
		}
		if (knnScore > knnNegScore * negDiscount) {
			System.err.println("[REJECT] Pos Score:" + knnScore);
			System.err.println("[REJECT] Neg Score:" + knnNegScore * negDiscount);
			System.err.println("[REJECT] Current:" + matcherPath);
			System.err.println("[REJECT] Actual:" + e.getOutcome() + "\tPredicted:" + relationType);
		}
		return null;
	}
}
