// -*- tab-width: 4 -*-
//Title:        JET
//Version:      1.81
//Copyright:    Copyright (c) 2015
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Tool

package edu.nyu.jet.aceJet;

import java.util.*;
import java.io.*;

import edu.nyu.jet.models.WordEmbedding;
import edu.nyu.jet.parser.SyntacticRelationSet;
import edu.nyu.jet.tipster.*;
import edu.nyu.jet.zoner.SentenceSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a relation tagger based on dependency paths and argument types, as produced by Jet ICE.
 */

public class DepPathRelationTaggerWordEmbedding {

	final static Logger logger = LoggerFactory.getLogger(DepPathRelationTaggerWordEmbedding.class);

	static Document doc;
	static AceDocument aceDoc;
	static String currentDoc;

	static int WINDOW = 5;

	// model: a map from AnchoredPath strings to relation types
	static Map<String, List<String>> model = null;

	static List<String> posModel = null; // positive LDPs
	static List<String> negModel = null; // negative LDPs

	static String normalOutcome = null; // relation type of positive LDPs without inverse args
	static Set<String> normalArgs = null;

	/**
	 * relation 'decoder': identifies the relations in document 'doc' (from file name 'currentDoc') and adds them as
	 * AceRelations to AceDocument 'aceDoc'.
	 */

	public static void findRelations(String currentDoc, Document d, AceDocument ad) {
		doc = d;
		RelationTagger.doc = d;
		doc.relations.addInverses();
		aceDoc = ad;
		RelationTagger.docName = currentDoc;
		SentenceSet sentences = new SentenceSet(doc);
		RelationTagger.relationList = new ArrayList<AceRelation>();
		// RelationTagger.findEntityMentions (aceDoc);
		AceMention[] ray = aceDoc.allMentionsList.toArray(new AceMention[0]);
		Arrays.sort(ray);
		for (int i = 0; i < ray.length - 1; i++) {
			for (int j = 1; j < WINDOW && i + j < ray.length; j++) {
				AceMention m1 = ray[i];
				AceMention m2 = ray[i + j];
				// if two mentions co-refer, they can't be in a relation
				// if (!canBeRelated(m1, m2)) continue;
				// if two mentions are not in the same sentence, they can't be in a relation
				if (!sentences.inSameSentence(m1.getJetHead().start(), m2.getJetHead().start()))
					continue;

				// System.out.println(doc.relations);

				predictRelation(m1, m2, doc.relations);
			}
		}
		// combine relation mentions into relations
		RelationTagger.relationCoref(aceDoc);
		RelationTagger.removeRedundantMentions(aceDoc);
	}

	/**
	 * load the model used by the relation tagger. Each line consists of an AnchoredPath [a lexicalized dependency path
	 * with information on the endpoint types], an equals sign, and a relation type.
	 */

	static void loadModel(String modelFile) throws IOException {
		System.out.println("loading dep path relational model from file " + modelFile);
		model = new TreeMap<String, List<String>>();
		BufferedReader reader = new BufferedReader(new FileReader(modelFile)); // for model + posModel

		String line;
		int n = 0;
		int lineNo = 0;
		while ((line = reader.readLine()) != null) {
			lineNo++;
			if (line.startsWith("#"))
				continue;
			String[] fields = line.split("=");
			if (fields.length < 2) {
				loadError(lineNo, line, "missing =");
				continue;
			}
			if (fields.length > 2) {
				loadError(lineNo, line, "extra =");
				continue;
			}
			String pattern = fields[0].trim();
			String outcome = fields[1].trim();
			if (!AnchoredPath.valid(pattern)) {
				loadError(lineNo, line, "invalid path");
				continue;
			}
			if (model.get(pattern) == null)
				model.put(pattern, new ArrayList<String>());
			model.get(pattern).add(outcome);
			n++;
		}
		System.out.println("Loaded " + n + " dependency paths.");
	}

	// load positive and negative patterns for nearest neighbor matching
	static void loadPosAndNegModel(String posModelFile, String negModelFile, String embeddingFile) throws IOException {
		WordEmbedding.loadWordEmbedding(embeddingFile);

		posModel = new ArrayList<String>();
		negModel = new ArrayList<String>();

		normalArgs = new TreeSet<String>();

		BufferedReader posReader = new BufferedReader(new FileReader(posModelFile)); // for posModel
		BufferedReader negReader = new BufferedReader(new FileReader(negModelFile)); // for negModel

		String line, negLine;
		int n = 0, m = 0;
		int lineNo = 0, negLineNo = 0;

		while ((line = posReader.readLine()) != null) {
			lineNo++;
			if (line.startsWith("#"))
				continue;
			String[] fields = line.split("=");
			if (fields.length < 2) {
				loadError(lineNo, line, "missing =");
				continue;
			}
			if (fields.length > 2) {
				loadError(lineNo, line, "extra =");
				continue;
			}

			String pattern = fields[0].trim();
			String outcome = fields[1].trim();

			if (!AnchoredPath.valid(pattern)) {
				loadError(lineNo, line, "invalid path");
				continue;
			}

			posModel.add(pattern);
			n++;

			if (!outcome.contains("-1")) { // used to check if a similarity matched pattern has normal args
				normalArgs.add(pattern.split("--")[0] + " " + pattern.split("--")[2]);
				normalOutcome = outcome; // relation type of positive patterns
			}
		}

		System.out.println("Normal Args: " + normalArgs);

		while ((negLine = negReader.readLine()) != null) {
			negLineNo++;
			if (negLine.startsWith("#"))
				continue;
			String[] fields = negLine.split("=");
			if (fields.length < 2) {
				loadError(negLineNo, negLine, "missing =");
				continue;
			}
			if (fields.length > 2) {
				loadError(negLineNo, negLine, "extra =");
				continue;
			}

			String pattern = fields[0].trim();

			if (!AnchoredPath.valid(pattern)) {
				loadError(negLineNo, negLine, "invalid path");
				continue;
			}

			negModel.add(pattern);
			m++;
		}

		System.out.println("Loaded " + n + " posigve paths" + " and " + m + " negtative paths.");
	}

	private static void loadError(int lineNo, String line, String message) {
		System.out.println(" *** Invalid dep path (" + message + ") on line " + lineNo + ":");
		System.out.println("        " + line);
	}

	/**
	 * use dependency paths to determine whether the pair of mentions bears some ACE relation; if so, add the relation to
	 * relationList. If the path appears with multiple relation types, add each one to the list.
	 */

	private static void predictRelation(AceMention m1, AceMention m2, SyntacticRelationSet relations) {
		// compute path
		int h1 = m1.getJetHead().start();
		int h2 = m2.getJetHead().start();
		String path = EventSyntacticPattern.buildSyntacticPath(h1, h2, relations);

		// logger.info(path);

		if (path == null)
			return;
		path = AnchoredPath.reduceConjunction(path);

		if (path == null)
			return;
		path = AnchoredPath.lemmatizePath(path); // telling -> tell, does -> do, watched -> watch, etc.

		// build pattern = path + arg types
		String pattern = m1.getType() + "--" + path + "--" + m2.getType();
		// look up path in model
		List<String> outcomes = model.get(pattern);

		// if candidate pattern does not have a exact match in ACE document
		// if (outcomes == null && posArgsSet.contains(m1.getType() + m2.getType())) {
		if (outcomes == null) {
			if (checkPositiveSimilarity(pattern)) {
				outcomes = new ArrayList<String>();
				// determine if a pattern has inverse args
				outcomes.add(normalArgs.contains(m1.getType() + " " + m2.getType()) ? normalOutcome : normalOutcome + "-1");
				// System.out.println(outcomes.get(0));
			} else {
				return;
			}
		}

		// if (!RelationTagger.blockingTest(m1, m2)) return;
		// if (!RelationTagger.blockingTest(m2, m1)) return;
		for (String outcome : outcomes) {
			boolean inv = outcome.endsWith("-1");
			outcome = outcome.replace("-1", "");
			String[] typeSubtype = outcome.split(":", 2);
			String type = typeSubtype[0];
			String subtype;
			if (typeSubtype.length == 1) {
				subtype = "";
			} else {
				subtype = typeSubtype[1];
			}

			if (inv) {
				AceRelationMention mention = new AceRelationMention("", m2, m1, doc);
				System.out.println("Inverse Found " + outcome + " relation " + mention.text); // <<<
				AceRelation relation = new AceRelation("", type, subtype, "", m2.getParent(), m1.getParent());
				relation.addMention(mention);
				RelationTagger.relationList.add(relation);
			} else {
				AceRelationMention mention = new AceRelationMention("", m1, m2, doc);
				System.out.println("Found " + outcome + " relation " + mention.text); // <<<
				AceRelation relation = new AceRelation("", type, subtype, "", m1.getParent(), m2.getParent());
				relation.addMention(mention);
				RelationTagger.relationList.add(relation);
			}
		}
	}

	// compare the similarity of a candidate pattern to the set of positive and negative patterns
	private static boolean checkPositiveSimilarity(String candidatePattern) {
		double[] candidatePathEmbedding = null;
		double[] posPathEmbedding = null;
		double[] negPathEmbedding = null;

		Set<String> posArgsSet = new TreeSet<String>(); // store argument pairs of positive patterns

		if (!WordEmbedding.isLoaded()) {
			return false;
		}

		String args = candidatePattern.split("--")[0] + " " + candidatePattern.split("--")[2];

		// get embedding of candidate path
		if (candidatePattern != null) {
			String path = candidatePattern.split("--")[1];
			String[] lexInPath = path.split(":");
			int length = lexInPath.length;

			if (length > 1) { // if the path contain at least one word
				String[] wordsInPath = new String[(int) Math.floor(length / 2)]; // No. of words in path = Math.floor(length/2)
				for (int i = 1; i < length; i = i + 2) {
					wordsInPath[(int) Math.floor(i / 2)] = lexInPath[i]; // get words in path
				}

				// System.out.println(wordsInPath[0]);
				candidatePathEmbedding = WordEmbedding.embed(wordsInPath);
			}
		}

		// get embedding of positive paths
		if (posModel != null) {
			for (String pattern : posModel) {
				String posArgs = pattern.split("--")[0] + " " + pattern.split("--")[2];
				posArgsSet.add(posArgs);

				if (!posArgs.equals(args)) {
					continue; // argument types don't match
				}

				String path = pattern.split("--")[1];
				String[] lexInPath = path.split(":");
				int length = lexInPath.length;

				if (length > 1) {
					String[] wordsInPath = new String[(int) Math.floor(length / 2)];
					for (int i = 1; i < length; i = i + 2) {
						wordsInPath[(int) Math.floor(i / 2)] = lexInPath[i]; // get words in path
					}

					double[] v = WordEmbedding.embed(wordsInPath); // get embedding
					if (v != null) {
						if (posPathEmbedding == null) {
							posPathEmbedding = v;
						} else {
							for (int i = 0; i < v.length; i++) {
								posPathEmbedding[i] += v[i]; // add embedding scores onto old scores
							}
						}
					}
				}
			}
		}

		// get embedding of negative paths
		if (negModel != null) {
			for (String pattern : negModel) {
				String negArgs = pattern.split("--")[0] + " " + pattern.split("--")[2];

				if (!negArgs.equals(args)) {
					continue; // argument types don't match
				}

				String path = pattern.split("--")[1];
				String[] lexInPath = path.split(":");
				int length = lexInPath.length;

				if (length > 1) {
					String[] wordsInPath = new String[(int) Math.floor(length / 2)];
					for (int i = 1; i < length; i = i + 2) {
						wordsInPath[(int) Math.floor(i / 2)] = lexInPath[i]; // get words in path
					}

					double[] v = WordEmbedding.embed(wordsInPath); // get embedding
					if (v != null) {
						if (negPathEmbedding == null) {
							negPathEmbedding = v;
						} else {
							for (int i = 0; i < v.length; i++) {
								negPathEmbedding[i] += v[i]; // add embedding scores onto old scores
							}
						}
					}
				}
			}
		}

		double posScore = WordEmbedding.similarity(posPathEmbedding, candidatePathEmbedding);
		double negScore = WordEmbedding.similarity(negPathEmbedding, candidatePathEmbedding);

		if (posArgsSet.contains(args)) { // if argument pairs of candidate occurs in positive argument pairs set
			// System.out.println("Embedding scores: " + candidatePattern + "=" + posScore + " " + negScore);
			if (posScore > negScore && posScore > 0.5) {
				return true;
			} else {
				return false;
			}
		}

		return false;
	}

}