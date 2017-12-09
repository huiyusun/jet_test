// -*- tab-width: 4 -*-
//Title:        JET
//Version:      1.81
//Copyright:    Copyright (c) 2015
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Tool

package AceJet;

import java.util.*;
import java.io.*;
import Jet.*;
import Jet.Parser.SynFun;
import Jet.Parser.SyntacticRelation;
import Jet.Parser.SyntacticRelationSet;
import Jet.Parser.DepParser;
import Jet.Refres.Resolve;
import Jet.Pat.Pat;
import Jet.Tipster.*;
import Jet.Zoner.SentenceSet;
import Jet.Lex.Stemmer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  A relation tagger based on dependency paths and argument types.  This
 *  class accepts more general patterns than are currently produced by ICE:
 *  a node in the path may match a specific word, an entity class, or
 *  any word.
 */

public class DepPathRelationTagger {

    final static Logger logger = LoggerFactory.getLogger(DepPathRelationTagger.class);

    static Document doc;
    static AceDocument aceDoc;
    static String currentDoc;
    static Stemmer stemmer = Stemmer.getDefaultStemmer();

    // model:  a map from AnchoredPath strings to relation types
    static Map<String, String> model = null;

    /**
     *  relation 'decoder':  identifies the relations in document 'd' 
     *  (from file name 'currentDoc') and adds them
     *  as AceRelations to AceDocument 'ad'.
     */

    public static void findRelations (String currentDoc, Document d, AceDocument ad) {
	doc = d;
	RelationTagger.doc = d;
	doc.relations.addInverses();
	aceDoc = ad;
	RelationTagger.docName = currentDoc;
	RelationTagger.sentences = new SentenceSet(doc);
	RelationTagger.relationList = new ArrayList<AceRelation> ();
	RelationTagger.findEntityMentions (aceDoc);
	// -- set up map posn --> entity mention
	entityMentionAt = new HashMap<Integer, AceEntityMention>();
	for (AceEntityMention m : RelationTagger.mentionSet) {
	    int h = m.getJetHead().start();
	    entityMentionAt.put(h, m);
	}
	for (String pattern : model.keySet()) {
	    String outcome = model.get(pattern);
	    SyntacticRelationSet srs = doc.relations;
	    predictRelation(pattern.split(":"), outcome, srs);
	}
	// combine relation mentions into relations
	RelationTagger.relationCoref (aceDoc);
	RelationTagger.removeRedundantMentions (aceDoc);
    }

    /**
     *  load the model used by the relation tagger.  Each line consists of an
     *  AnchoredPath [a lexicalized dependency path with information on the
     *  endpoint types], a tab, and a relation type.
     */

    static void loadModel (String modelFile) throws IOException {
	model = new TreeMap<String, String>();
	BufferedReader reader = new BufferedReader (new FileReader (modelFile));
	String line;
	int n = 0;
	while ((line = reader.readLine()) != null) {
	    String[] fields = line.split("\t");
	    if (fields.length !=  2) {
		System.out.println("Error in dependency path file, line " + n);
		System.out.println("   Missing/extra tab: " + line);
		continue;
	    }
	    String pattern = fields[0];
	    String outcome = fields[1];
	    model.put (pattern, outcome);
	    n++;
	}
	System.out.println ("Loaded relation model with " + n + " relation paths.");
    }

    public static void loadModel () {
	String relationDepPathFile = JetTest.getConfigFile("Ace.RelationDepPaths.fileName");
	if (relationDepPathFile != null) {
	    try {
		DepPathRelationTagger.loadModel (relationDepPathFile);
	    } catch (IOException e) {
		System.out.println ("Error loading dependency paths: " + e);
	    }
	}
    }

    static Set<Integer> nodesMatched;
    static SyntacticRelationSet relations;
    static AceEntityMention m1;
    static AceEntityMention m2;
    static Map<Integer, AceEntityMention> entityMentionAt;
    static int wildPosn;
    static String wildWord;
    static String wildType;

    /**
     *  Check a document for instances of the relation 
     */

    public static void predictRelation (String[] pattern, String outcome, 
	    SyntacticRelationSet relations) {
	// System.out.println("pattern = ");
	// for (String p : pattern) System.out.print(p + ":");
	// System.out.println();
	wildPosn = -1;
	nodesMatched = new HashSet<Integer>();
	String startNodeType = pattern[0];
	for (SyntacticRelation r : relations) {
	   String stem = stemmer.getStem(r.sourceWord, r.sourcePos);
	    if(typeMatch(r.sourcePosn, stem, startNodeType)) {
		m1 = entityMentionAt.get(r.sourcePosn);
		matchPath(pattern, 1, r.sourcePosn, outcome, relations);
	    }
	}
    }

    /**
     *  Tests whether a lexicalized depend. path matches some path
     *  through the dependency tree.  If the match is successful,
     *  calls 'recordRelation'.
     *
     *  @param  pattern    the lexical depend. path, consisting of
     *                     alternating nodes and dependency relations
     *  @param  i          the current position within the LDP
     *  @param  node       the ofset in the document of the most
     *                     recently matched token
     *  @param  outcome    the relation type
     *  @param  relations  the depedency relations for the sentence
     */

   public static void matchPath (String[] pattern, int i, int node,
	   String outcome, SyntacticRelationSet relations) {
       nodesMatched.add(node);
       if (i == pattern.length) {
	   if (wildPosn >= 0) 
	       buildEntity (wildPosn, wildWord, wildType);
	   m2 = entityMentionAt.get(node);
	   recordRelation (outcome, relations);
	   return;
       }
       String edgeType = pattern[i];
       String nodeType = pattern[i+1];
       for (SyntacticRelation r : relations.getRelations(node, edgeType)) {
	   String stem = stemmer.getStem(r.targetWord, r.targetPos);
	   if (!nodesMatched.contains(r.targetPosn) &&
		   typeMatch(r.targetPosn, stem, nodeType))
	       matchPath (pattern, i + 2, r.targetPosn, outcome, relations);
       }
       nodesMatched.remove(node);
   }

   /**
    *  Tests a single token in the dependency tree.
    *
    *  @param  posn    offset of token within document
    *  @param  word    the token
    *  @param  type    specification of token within lexical depend. path
    *
    *  @return true if token matches specification.
    */

    public static boolean typeMatch (int posn, String word, String type) {
	if (type.equals("*"))
	   return true;
        if (type.equals(word))
	   return true;
	if (type.startsWith("?")) {
	    type = type.substring(1);
	    AceEntityMention m = entityMentionAt.get(posn);
	    if (m == null) {
		wildPosn = posn;
		wildWord = word;
		wildType = type;
		return true;
	    } else {
		return m.entity.type.equalsIgnoreCase(type);
	    }
	}
	if (allCaps(type)) {
	    AceEntityMention m = entityMentionAt.get(posn);
	    if (m == null)
		return false;
	    return m.entity.type.equalsIgnoreCase(type);
	}
	return false;
    }

    static boolean allCaps (String s) {
	return s.equals(s.toUpperCase());
    }

    /**
     *  use dependency paths to determine whether the pair of mentions bears some
     *  ACE relation;  if so, add the relation to relationList.
     */

    private static void recordRelation (String outcome, SyntacticRelationSet relations) {
	String[] typeSubtype = outcome.split(":", 2);
	String type = typeSubtype[0];
	String subtype;
	if (typeSubtype.length == 1) {
	    subtype = "";
	} else {
	    subtype = typeSubtype[1];
	}
	if (subtype.endsWith("-1")) {
	    subtype = subtype.replace("-1","");
	    AceRelationMention mention = new AceRelationMention("", m2, m1, doc);
	    AceRelation relation = new AceRelation("", type, subtype, "", m2.entity, m1.entity);
	    relation.addMention(mention);
	    RelationTagger.relationList.add(relation);
	} else {
	    AceRelationMention mention = new AceRelationMention("", m1, m2, doc);
System.out.println ("Found " + outcome + " relation " + mention.text.replaceAll("\n", " "));  //<<<
            AceRelation relation = new AceRelation("", type, subtype, "", m1.entity, m2.entity);
	    relation.addMention(mention);
	    RelationTagger.relationList.add(relation);
	}
    }

    /**
     *  Creates a new entity when one is implied by a pattern but has
     *  not been recognized in the text.
     */

    static boolean buildEntity (int posn, String word, String type) {
	// no existing mention
	if (entityMentionAt.get(posn) != null)
	    return false;
	int headEnd = posn + word.length();
	Span headSpan = new Span(posn, headEnd);
	int mentionStart = firstDescendentOf(posn);
	int mentionEnd = tokenEnd(doc, lastDescendentOf(posn));
	Span mentionSpan = new Span(mentionStart, mentionEnd);
	int aceEntityNo = 999;
	String docId = "?";
	String entityID = docId + "-" + aceEntityNo;
	AceEntity aceEntity = new AceEntity (entityID, type, "", false);
	String mentionID = entityID + "-1";
	String mentionType = "NAME";
	AceEntityMention m =
	    new AceEntityMention (mentionID, mentionType, mentionSpan, headSpan, doc.text());
	aceEntity.addMention(m);
	// to handle disease names, use span of entire mention
	aceEntity.addName(new AceEntityName(mentionSpan, doc.text()));
	aceDoc.addEntity(aceEntity);
	entityMentionAt.put(posn, m);
	return true;
    }

    /**
     *  Returns the leftmost descendent of node 'posn' in the
     *  dependency tree.
     */

    static int firstDescendentOf (int posn) {
	int childPosn = 99999999;
	SyntacticRelationSet children = doc.relations.getRelationsFrom(posn);
	for (SyntacticRelation child : children) {
	    if (child.type.endsWith("-1"))
		continue;
	    if (child.targetPosn < childPosn)
		childPosn = child.targetPosn;
	}
	if (childPosn < posn)
	    return firstDescendentOf(childPosn);
	else
	    return posn;
    }

    /**
     *  Returns the rightmost descendent of node 'posn' in the
     *  dependency tree.
     */

    static int lastDescendentOf (int posn) {
	int childPosn = -1;
	SyntacticRelationSet children = doc.relations.getRelationsFrom(posn);
	for (SyntacticRelation child : children) {
	    if (child.type.endsWith("-1"))
		continue;
	    if (child.targetPosn > childPosn)
		childPosn = child.targetPosn;
	}
	if (childPosn > posn)
	    return lastDescendentOf(childPosn);
	else
	    return posn;
    }

    /**
     *  If 'posn' is the offset of the start of a token, return the
     *  offset of the end pf the token.
     */

    static int tokenEnd (Document doc, int posn) {
	Annotation a = doc.tokenAt(posn);
	if (a != null)
	    return a.end();
	else
	    return posn;
    }
}
