package edu.nyu.jet.aceJet;

import java.util.*;
import java.util.function.Predicate;
import java.io.*;

import edu.nyu.jet.tipster.*;

/**
 * Converts information about ACE relations in an APF file into a list of triples. <br>
 * If one of the APF documents contains a relation of type R between entities E1 and E2, where both E1 and E2 have names
 * and the first names associated with E1 and E2 are N1 and N2, writes a line of the form <br>
 * N1:R:N2.
 */

public class APFtoTriples_selectiveNames {

	/**
	 * converts ACE relations in an APF file into triples. Takes an array of 6 arguments:
	 * <ul>
	 * <li>the directory containing the source files
	 * <li>the directory containing the APF files
	 * <li>a file containing a list (one per line) of the document (file) names
	 * <li>the file extension of the source files
	 * <li>the file extension of the APF files
	 * <li>the output file which will contain the triples
	 * </ul>
	 * If the same relation between the same individuals appears several times across the input collection, multiple
	 * triples will be generated.
	 */

	public static void main(String[] args) throws IOException {
		if (args.length != 6) {
			System.err
					.println("APFtoTriples requires 6 arguments: sourceDir apfDir docList sourceFileExt apfFileExt outFile");
			System.exit(1);
		}
		String sourceDir = args[0];
		String apfDir = args[1];
		String docList = args[2];
		String sourceFileExt = args[3];
		String apfFileExt = args[4];
		String outFile = args[5];
		List<String> allTriples = new ArrayList<String>();
		int docCount = 0;
		String line;
		BufferedReader docIdReader = new BufferedReader(new FileReader(docList));
		while ((line = docIdReader.readLine()) != null) {
			String sourceFile = sourceDir + line + "." + sourceFileExt;
			String apfFile = apfDir + line + "." + apfFileExt;
			ExternalDocument doc = new ExternalDocument("sgml", sourceFile);
			doc.setAllTags(true);
			doc.open();
			AceDocument aceDoc = new AceDocument(sourceFile, apfFile);
			allTriples.addAll(makeTriples(doc, aceDoc));
			docCount++;
			// if (docCount % 100 == 0)
			System.out.println(docCount + " documents " + allTriples.size() + " triples");
		}
		PrintWriter pw = new PrintWriter(new FileWriter(outFile));
		for (String trip : allTriples) {
			pw.println(trip);
		}
		pw.close();
	}

	/**
	 * Converts the ACE relations in one document into triples.
	 * 
	 * @param doc
	 *          the source file of the document
	 * @param aceDoc
	 *          the APF file of the document
	 * @return a list of relation triples
	 */

	// makeTriples
	public static List<String> makeTriples1(Document doc, AceDocument aceDoc) {
		List<String> triples = new ArrayList<String>();
		for (AceRelation r : aceDoc.relations) {
			AceEventArgumentValue arg1 = r.arg1;
			String arg1Name = nameOfEntity(arg1);
			AceEventArgumentValue arg2 = r.arg2;
			String arg2Name = nameOfEntity(arg2);
			if (arg1Name == null || arg2Name == null)
				continue;

			String triple = arg1Name + ":" + r.type + ":" + arg2Name;
			triples.add(triple);
		}
		return triples;
	}

	// makeTriplesPerfect: write triple separated by '=' if an argument has multiple names. This is later resolved by
	// the Scorer: ScoreAceTriples.java
	public static List<String> makeTriples(Document doc, AceDocument aceDoc) {
		List<String> triples = new ArrayList<String>();
		for (AceRelation r : aceDoc.relations) {
			List<AceEntityName> arg1Names = ((AceEntity) r.arg1).names;
			List<AceEntityName> arg2Names = ((AceEntity) r.arg2).names;

			int size1 = arg1Names.size();
			int size2 = arg2Names.size();

			if (size1 == 0 || size2 == 0)
				continue;

			if (size1 >= 3 && size2 >= 3) {
				String tripleLongest = getLongest(arg1Names) + ":" + r.type + ":" + getLongest(arg2Names);

				String triple1 = arg1Names.get(0).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(0).text.replaceAll("\\s+", " ");
				String triple2 = arg1Names.get(0).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(size2 - 2).text.replaceAll("\\s+", " ");
				String triple3 = arg1Names.get(0).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(size2 - 1).text.replaceAll("\\s+", " ");

				String triple4 = arg1Names.get(size1 - 2).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(0).text.replaceAll("\\s+", " ");
				String triple5 = arg1Names.get(size1 - 2).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(size2 - 2).text.replaceAll("\\s+", " ");
				String triple6 = arg1Names.get(size1 - 2).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(size2 - 1).text.replaceAll("\\s+", " ");

				String triple7 = arg1Names.get(size1 - 1).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(0).text.replaceAll("\\s+", " ");
				String triple8 = arg1Names.get(size1 - 1).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(size2 - 2).text.replaceAll("\\s+", " ");
				String triple9 = arg1Names.get(size1 - 1).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(size2 - 1).text.replaceAll("\\s+", " ");

				triples.add(tripleLongest + "=" + triple1 + "=" + triple2 + "=" + triple3 + "=" + triple4 + "=" + triple5 + "="
						+ triple6 + "=" + triple7 + "=" + triple8 + "=" + triple9);
			} else if (size1 >= 3 && size2 >= 2) {
				String tripleLongest = getLongest(arg1Names) + ":" + r.type + ":" + getLongest(arg2Names);

				String triple1 = arg1Names.get(0).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(0).text.replaceAll("\\s+", " ");
				String triple2 = arg1Names.get(0).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(size2 - 1).text.replaceAll("\\s+", " ");

				String triple3 = arg1Names.get(size1 - 2).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(0).text.replaceAll("\\s+", " ");
				String triple4 = arg1Names.get(size1 - 2).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(size2 - 1).text.replaceAll("\\s+", " ");

				String triple5 = arg1Names.get(size1 - 1).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(size2 - 1).text.replaceAll("\\s+", " ");
				String triple6 = arg1Names.get(size1 - 1).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(size2 - 1).text.replaceAll("\\s+", " ");

				triples.add(tripleLongest + "=" + triple1 + "=" + triple2 + "=" + triple3 + "=" + triple4 + "=" + triple5 + "="
						+ triple6);
			} else if (size1 >= 2 && size2 >= 3) {
				String tripleLongest = getLongest(arg1Names) + ":" + r.type + ":" + getLongest(arg2Names);

				String triple1 = arg1Names.get(0).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(0).text.replaceAll("\\s+", " ");
				String triple2 = arg1Names.get(0).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(size2 - 2).text.replaceAll("\\s+", " ");
				String triple3 = arg1Names.get(0).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(size2 - 1).text.replaceAll("\\s+", " ");

				String triple4 = arg1Names.get(size1 - 1).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(0).text.replaceAll("\\s+", " ");
				String triple5 = arg1Names.get(size1 - 1).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(size2 - 2).text.replaceAll("\\s+", " ");
				String triple6 = arg1Names.get(size1 - 1).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(size2 - 1).text.replaceAll("\\s+", " ");

				triples.add(tripleLongest + "=" + triple1 + "=" + triple2 + "=" + triple3 + "=" + triple4 + "=" + triple5 + "="
						+ triple6);
			} else if (size1 >= 2 && size2 >= 2) {
				String tripleLongest = getLongest(arg1Names) + ":" + r.type + ":" + getLongest(arg2Names);

				String triple1 = arg1Names.get(0).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(0).text.replaceAll("\\s+", " ");
				String triple2 = arg1Names.get(0).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(size2 - 1).text.replaceAll("\\s+", " ");
				String triple3 = arg1Names.get(size1 - 1).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(0).text.replaceAll("\\s+", " ");
				String triple4 = arg1Names.get(size1 - 1).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(size2 - 1).text.replaceAll("\\s+", " ");

				triples.add(tripleLongest + "=" + triple1 + "=" + triple2 + "=" + triple3 + "=" + triple4);
			} else if (size1 >= 2) {
				String tripleLongest = getLongest(arg1Names) + ":" + r.type + ":" + getLongest(arg2Names);

				String triple1 = arg1Names.get(0).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(0).text.replaceAll("\\s+", " ");
				String triple2 = arg1Names.get(size1 - 1).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(0).text.replaceAll("\\s+", " ");

				triples.add(tripleLongest + "=" + triple1 + "=" + triple2);
			} else if (size2 >= 2) {
				String tripleLongest = getLongest(arg1Names) + ":" + r.type + ":" + getLongest(arg2Names);

				String triple1 = arg1Names.get(0).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(0).text.replaceAll("\\s+", " ");
				String triple2 = arg1Names.get(0).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(size2 - 1).text.replaceAll("\\s+", " ");

				triples.add(tripleLongest + "=" + triple1 + "=" + triple2);
			} else { // both size1 = 1 and size2 = 1
				String triple1 = arg1Names.get(0).text.replaceAll("\\s+", " ") + ":" + r.type + ":"
						+ arg2Names.get(0).text.replaceAll("\\s+", " ");

				triples.add(triple1);
			}
		}
		return triples;
	}

	// makeTriplesTests
	public static List<String> makeTriplesTests(Document doc, AceDocument aceDoc) {
		List<String> triples = new ArrayList<String>();
		int count = 0;

		for (AceRelation r : aceDoc.relations) {
			AceEventArgumentValue arg1 = r.arg1;
			String arg1Name = nameOfEntity(arg1);
			AceEventArgumentValue arg2 = r.arg2;
			String arg2Name = nameOfEntity(arg2);
			if (arg1Name == null || arg2Name == null)
				continue;

			String triple = ((AceEntity) arg1).names.get(0).text + ":" + r.type + ":" + r.arg2.id;
			triples.add(triple);

			// // get aceMentionsKey
			// if (r.mentions.size() >= 1) {
			// String triple = arg1Name + "(" + ((AceEntity) arg1).type + ")" + ":" + r.subtype + ":" + arg2Name + "("
			// + ((AceEntity) arg2).type + ")" + " = " + r.mentions.get(0).text;
			// triples.add(triple);
			// }

			// Step 1 to get aceSentencesKey. Step 2 is to run FindSentence.java in ice_support
			// String triple = arg1Name + "(" + ((AceEntity) arg1).type + ")" + ":" + r.type + ":" + arg2Name + "("
			// + ((AceEntity) arg2).type + ")" + " = " + aceDoc.docID;
			// triples.add(triple);

			// get allRelationDoc_frequent
			// if (r.type.equals("ORG-AFF")) {
			// String triple = r.type + " = " + aceDoc.docID;
			// count++;
			// if (count >= 7) { // get frequent doc id
			// triples.add(triple);
			// return triples;
			// }
			// }

			// // get aceMentionsDocID
			// String triple = arg1Name + ":" + r.type + ":" + arg2Name + " = " + r.id;
			// triples.add(triple);
		}

		return triples;
	}

	/**
	 * Returns the name of entity 'e', or <CODE>null</CODE> if it has no name.
	 */

	public static String nameOfEntity(AceEventArgumentValue e) {
		if (e instanceof AceTimex)
			return ((AceTimex) e).mentions.get(0).text;
		List<AceEntityName> names = ((AceEntity) e).names;
		if (names != null && names.size() > 0)
			return names.get(0).text.replaceAll("\\s+", " ");
		else
			return null;
	}

	public static String getLongest(List<AceEntityName> names) {
		AceEntityName longest = names.get(0);

		for (AceEntityName element : names) {
			if (element.text.replaceAll("\\s+", " ").length() > longest.text.replaceAll("\\s+", " ").length()) {
				longest = element;
			}
		}

		return longest.text.replaceAll("\\s+", " ");
	}
}
