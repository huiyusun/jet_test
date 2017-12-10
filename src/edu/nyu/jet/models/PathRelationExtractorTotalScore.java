package edu.nyu.jet.models;

import opennlp.model.Event;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tag relations in a document by using positive and negative dependency path rules created with ICE
 *
 * @author yhe
 */
public class PathRelationExtractorTotalScore {

	public void setNegDiscount(double nDiscount) {
		negDiscount = nDiscount;
	}

	public void setMinThreshold(double minThresh) {
		minThreshold = minThresh;
	}

	public static double minThreshold = 0.5;

	public static double negDiscount = 0.8;

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
		}

		br.close();
	}

	public void loadNeg(String negRulesFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(negRulesFile));
		String line = null;
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
		}

		br.close();
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

		MatcherPath posRule = null;
		double totalPosScore = 0;
		double totalNegScore = 0;
		int posCount = 0, negCount = 0;

		// System.out.println("candidate path: " + fullDepPath);

		for (MatcherPath rule : ruleTable) { // positive paths
			if (rule.toString().replace(rule.getRelationType(), "").equals(matcherPath.toString())) { // exact match
				// System.out.println("found exact match");
				return rule.getRelationType();
			}

			if (rule.arg1Type.equals(matcherPath.arg1Type) && rule.arg2Type.equals(matcherPath.arg2Type)) {
				double score = pathMatcher.matchPaths(matcherPath, rule) / rule.length();
				totalPosScore += score;
				posCount++;
				posRule = rule;
			}
		}

		for (MatcherPath rule : negTable) { // negative paths
			if (rule.arg1Type.equals(matcherPath.arg1Type) && rule.arg2Type.equals(matcherPath.arg2Type)) {
				double score = pathMatcher.matchPaths(matcherPath, rule) / rule.length();
				totalNegScore += score;
				negCount++;
			}
		}

		// calculate average scores
		double posScore = posCount == 0 ? 0 : totalPosScore / posCount;
		double negScore = negCount == 0 ? 0 : totalNegScore / negCount;

		if (posRule != null && posScore <= negScore * negDiscount) {
			System.err.println("[ACCEPT] Pos Score:" + posScore);
			System.err.println("[ACCEPT] Neg Score:" + negScore * negDiscount);
			System.err.println("[ACCEPT] Current:" + matcherPath);
			System.err.println("[ACCEPT] Actual:" + e.getOutcome() + "\tPredicted:" + posRule.getRelationType());

			return posRule.getRelationType();
		}

		if (posRule != null && posScore > negScore * negDiscount) {
			System.err.println("[REJECT] Pos Score:" + posScore);
			System.err.println("[REJECT] Neg Score:" + posScore * negDiscount);
			System.err.println("[REJECT] Current:" + matcherPath);
			System.err.println("[REJECT] Actual:" + e.getOutcome() + "\tPredicted:" + posRule.getRelationType());
		}

		return null;
	}
}
