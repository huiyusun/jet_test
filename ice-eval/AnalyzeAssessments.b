// Analyze Assessments for KBP Slot Filling
// author:  Ralph Grishman
// August 2015

package KBPJet;

import java.util.*;
import java.io.*;
import Jet.JetTest;
import Jet.Control;
import Jet.Tipster.*;
import Jet.Time.TimeMain;
import Jet.Refres.Resolve;
import Jet.Zoner.*;
import Jet.Scorer.SGMLProcessor;
import Jet.Parser.SyntacticRelation;
import Jet.Parser.SyntacticRelationSet;
import AceJet.Ace;
import AceJet.EventSyntacticPattern;

public class AnalyzeAssessments {

	static int assessmentCount = 0;
	static int trialCount = 0;
	static int successCount = 0;

	// file containing list of full paths of every document in corpus
	public static String fileListPath = 
		"/misc/proteus108/xiangli/Data/InitialData/FileLists/ColdStart/cs2013-2014FileList";

	// map from document id to file path
	static Map<String, String> id2DocPath = new HashMap<String, String>();

	// name of temporary file holding response of NYU system
	static String responseFile = "responseTemp";

	static StringBuilder docText = new StringBuilder();

	// file with hard-made patterns
	// static String patternFile = "../cs2015/syntax/out-patterns";
	static String patternFile = "patterns";

	// set of fills alrady reported
	static Set<String> fillsReported = new HashSet<String>();

	// switches
	// collect negative (as well as positive) examples
	static boolean collectNegatives = false;
	// collect only one example of each fill
	static boolean uniqueFills = true;
	// collect full sentences
	static boolean collectSentences = true;
	// collect paths
	static boolean collectPaths = false;
	// test NYU system
	static boolean systemTest = true;
	// maximum number of assessments to process
	static int max = 4000;

	/**
 	 *  read in a series of KBP slot-filling assessments and convert training data
 	 */

	public static void main (String[] args) 
			throws IOException, InterruptedException, NumberFormatException {

		if (args.length != 2) {
			System.err.println ("Review requires 2 arguments:");
			System.err.println ("     queries assessments");
			System.exit(0);
		}

		String queriesFile = args[0];
		String assessmentFile = args[1];
Fuzzy.initializePathMatcher(null);
		
		loadIdFileListMap();
		if (collectPaths)
			JetTest.initializeFromConfig ("props");
		if (systemTest)
			KBP.initialize("props", patternFile);
		List<SFQuery> queries = new SFQueryList(queriesFile).queries;

		BufferedReader reader = new BufferedReader(new InputStreamReader(
			new FileInputStream(assessmentFile), "UTF-8"));
		String assessment;
		while ((assessment = reader.readLine()) != null) {
			assessment = assessment.trim();
			String[] field = assessment.split("\t");
			String queryAndSlot = field[1];
			String docAndOffsetList = field[2];
			String fill = field[3];
			String fillOffset = field[4];
 			boolean positive = field[5].equals("C") && field[6].equals("C");
			if (!positive && !collectNegatives) continue;
			if (uniqueFills) 
				if(fillsReported.contains(fillOffset))
					continue;
				else
					fillsReported.add(fillOffset);
			String just = retrieveList(docAndOffsetList);
			just = just.replace('\n', ' ');

			field = queryAndSlot.split(":", 2);
			String queryId = field[0];
			String slot = field[1];
			int queryNo = Integer.parseInt(queryId.substring(9, 12));
			SFQuery query = queries.get(queryNo - 1);

			assessmentCount++;
			String path = "";
			if (collectPaths) {
				path = findPath(just, query, fill);
			}
			System.out.println ("=> " + assessmentCount + ": " + (positive ? "+" : "-") 
				+ queryAndSlot + "|" + query.name + "|" 
				+ fill + "|" + path + "|" + just);
			if (systemTest && positive) {
				trialCount++;
				boolean success = test (query, just, slot, fill);
				if (success) successCount++;
			}
			if (assessmentCount >= max) break;
		}
		System.out.println (successCount + "/" + trialCount + " examples successful");
	}

	/**
 	 *  Use NYU system to fill slots for query name 'query.name' from text 'just'.
 	 *  Return true if response includes value 'fill' for slot 'slot'.
 	 */

	static boolean test (SFQuery query, String just, String slot, String fill) throws IOException {
		KBP.entityID = query.id;
		KBP.entityName = query.name;
		KBP.entityType = query.enttype.toLowerCase().intern();;
		just = query.name  + " . " + just;
		SGMLProcessor.allTags = true;
		Document miniDoc = SGMLProcessor.sgmlToDoc(just, "x");
		KBP.responseWriter = new PrintWriter (new FileWriter (responseFile));
		KBP.processDocument (miniDoc, 1);
		KBP.responseWriter.close();
		BufferedReader responseReader = new BufferedReader(new FileReader(responseFile));
		String response;
		boolean success = false;
		while ((response = responseReader.readLine()) != null) {
			response = response.replace("org:top_members/employees", "org:top_members_employees");
			response = response.replace("per:employee_of", "per:employee_or_member_of");
			System.out.println("response:  " + response);
			String[] responseField = response.split(" \\| ");
			String responseSlot = responseField[3];
			String responseFill = responseField[6];
			if (responseSlot.equals(slot) && responseFill.equals(fill)) {
				success = true;
			}
		}
		responseReader.close();
		System.out.println (success);
		System.out.println ();
		return success;
	}

	static void loadIdFileListMap() throws IOException {
		System.out.print("\n\n Loading paths for corpus ... ");
		BufferedReader reader = new BufferedReader(new FileReader(fileListPath));
		
		String path = "", id = "";
		while ((path = reader.readLine()) != null) {
			path = path.trim();
			if (path.isEmpty()) continue;
			if (path.lastIndexOf("/") >= 0) {
				id = path.substring(path.lastIndexOf("/")+1);
				if (true) { //!CS2.format2013) {
					if (id.lastIndexOf(".") < 0) {
						System.out.println("***Error in CS1.loadIdFileListMap, invalid document id");
						System.exit(1);
					}
					id = id.substring(0, id.lastIndexOf("."));
				}
			}
			else
				id = path;
			
			if (id == null || id2DocPath.containsKey(id)) {
				System.out.println("***Error in CS1.loadIdFileListMap, duplicate or null document id: " + id);
				System.exit(1);
			}
			
			id2DocPath.put(id, path);
		}
		reader.close();
		System.out.println("Done!");
	}

	/**
 	 *  Given a comma-separated list of document ids and offsets, returns the
 	 *  concatenated text.
 	 */

	static String retrieveList (String docAndOffsetList) throws IOException, NumberFormatException {
		String[] field = docAndOffsetList.split(",");
		String justs = "";
		for (String docAndOffset : field) {
			if (justs != "")
				justs += "...";
			justs += retrieve(docAndOffset);
		}
		return justs;
	}

	/**
 	 *  Given a document id and offsets, in the KBP format id : start - end,
 	 *  returns the specified text (or enclosing sentences, if collectSentences is set)
 	 */

	static String retrieve (String docAndOffset) throws IOException, NumberFormatException {
		String[] field = docAndOffset.split(":", 2);
		String doc = field[0];
		String path = id2DocPath.get(doc);
		path = "/misc/proteus108/xiangli/Data/InitialData/KBPCorpus/2013/" + path;
		String offsets = field[1];
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line;
		docText.setLength(0);
		while ((line = reader.readLine()) != null) {
			docText.append(line + "\n");
		}
		reader.close();
		String[] offsetField = offsets.split("-", 2);
		int start = Integer.parseInt(offsetField[0]);
		int end = Integer.parseInt(offsetField[1]);
		if (!collectSentences) {
			String just = docText.substring(start, end + 1);
			return just;
		} else {
			Span justSpan = new Span(start, end + 1);
			String sent = enclosingSentence(docText.toString(), justSpan);
			return sent;
		}
	}
		
	/**
 	 *  Given the text of a document and a span within the document,
 	 *  returns the sentence enclosing that span.
 	 */

	static String enclosingSentence (String text, Span justSpan) {
		Document doc = new Document(text);
		SentenceSplitter.split (doc, doc.fullSpan());
		Vector<Annotation> sentences = doc.annotationsOfType("sentence");
		if (sentences == null)
			return "";
		for (Annotation sentence : sentences) {
			Span sentSpan = sentence.span();
			if (justSpan.within(sentSpan)) {
				String sentenceText = doc.text(sentSpan);
				return sentenceText;
			}
		}
		return "";
	}

	/**
 	 *  find dependency path from query name to fill
 	 */

	static String findPath (String just, SFQuery query, String fill) throws IOException {
		SGMLProcessor.allTags = true;
		Document miniDoc = SGMLProcessor.sgmlToDoc(just, "x");
		processDocument (miniDoc, 0);
		Integer queryPosn = findNode(query.name, miniDoc.relations);
		if (queryPosn == null) return null;
		Integer fillPosn = findNode(fill, miniDoc.relations);
		if (fillPosn == null) return null;
		String path = 
			EventSyntacticPattern.buildSyntacticPath(queryPosn, fillPosn, miniDoc.relations);
		return path;
	}

	static void processDocument (Document doc, int docCount) throws IOException {
		Vector<Annotation> textAnns = doc.annotationsOfType("TEXT");
		Annotation text;
		if (textAnns == null || textAnns.size() == 0) {
			System.err.println ("No TEXT annotations, processing entire document.");
			text = new Annotation("TEXT", doc.fullSpan(), null);
			doc.addAnnotation(text);
		} else {
			text = textAnns.get(0);
		}
		Resolve.ACE = true;
		Resolve.trace = true;
		Control.processDocument (doc, null, false, docCount);
		doc.relations.addInverses(); //updated June 21, 2014
		Ace.tagReciprocalRelations(doc);
		TimeMain.processDocument(doc);
	}

	static Integer findNode (String argument, SyntacticRelationSet relations) {
		argument = argument.replace(' ', '_');
		for (int j=0; j<relations.size(); j++) {
			SyntacticRelation r = (SyntacticRelation) relations.get(j);
			String word = r.targetWord;
			if (argument.equals(word))
				return r.targetPosn;
		}
		return null;
	}
}
