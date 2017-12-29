// -*- tab-width: 4 -*-
//Title:        JET
//Version:      1.81
//Copyright:    Copyright (c) 2015
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Tool

package edu.nyu.jet.aceJet;

import java.util.*;
import java.io.*;

import edu.nyu.jet.aceJet.Ace.Name;
import edu.nyu.jet.models.WordEmbedding;
import edu.nyu.jet.parser.SyntacticRelationSet;
import edu.nyu.jet.tipster.*;
import edu.nyu.jet.zoner.SentenceSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a relation tagger based on dependency paths and argument types, as produced by Jet ICE.
 */

public class DepPathRelationTaggerTestLimit {

	final static Logger logger = LoggerFactory.getLogger(DepPathRelationTagger.class);

	static Document doc;
	static AceDocument aceDoc;
	static String currentDoc;

	static int WINDOW = 5;

	// model: a map from AnchoredPath strings to relation types
	static Map<String, List<String>> model = null;

	static String normalOutcome = null; // relation type of positive LDPs without inverse args

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

				// System.out.println("AceMention: " + m1.getType() + " " + m2.getType());

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

			if (!outcome.contains("-1")) { // used to check if a similarity matched pattern has normal args
				normalOutcome = outcome; // relation type of positive patterns
			}
		}
		System.out.println("Loaded " + n + " dependency paths.");
	}

	// load positive and negative patterns for nearest neighbor matching
	static void loadPosAndNegModel(String posModelFile, String negModelFile, String embeddingFile) throws IOException {
	}

	private static void loadError(int lineNo, String line, String message) {
		System.out.println(" *** Invalid dep path (" + message + ") on line " + lineNo + ":");
		System.out.println("        " + line);
	}

	public enum EntityType {
		PERSON, ORGANIZATION, GPE, LOCATION, FACILITY, WEAPON, VEHICLE
	}

	public static boolean contains(String test) {
		for (EntityType c : EntityType.values()) {
			if (c.name().equals(test)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * use dependency paths to determine whether the pair of mentions bears some ACE relation; if so, add the relation to
	 * relationList. If the path appears with multiple relation types, add each one to the list.
	 */

	private static void predictRelation(AceMention m1, AceMention m2, SyntacticRelationSet relations) {
		List<String> outcomes = new ArrayList<String>();

		if (contains(m1.getType()) && contains(m2.getType())) {
			outcomes.add(normalOutcome);
			outcomes.add(normalOutcome + "-1");
			// System.out.println(outcomes.get(0));
		} else {
			return;
		}

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
}