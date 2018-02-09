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

public class APFtoTriples {

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

	// makeTriplesMain: write triple separated by '=' if an argument has multiple names. This is later resolved by
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

			StringBuilder tripleSB = new StringBuilder();
			Set<String> argNamesSet = new TreeSet<String>();

			// form triples over all combination of names between arg1 and arg2
			for (AceEntityName n1 : arg1Names) {
				for (AceEntityName n2 : arg2Names) {
					String name1 = n1.text.replaceAll("\\s+", " ");
					String name2 = n2.text.replaceAll("\\s+", " ");

					if (argNamesSet.contains(name1 + name2)) // don't write the same names
						continue;

					argNamesSet.add(name1 + name2);

					tripleSB.append(name1 + ":" + r.type + ":" + name2 + "=");
				}
			}

			String tripleStr = tripleSB.deleteCharAt(tripleSB.length() - 1).toString(); // delete the last redundant '='
			triples.add(tripleStr);
		}

		return triples;
	}

	// makeTriplesPatternCount: write triple separated by '=' if an argument has multiple names. This is later resolved by
	// the Scorer: ScoreAceTriples.java
	public static List<String> makeTriplesPatternsCount(Document doc, AceDocument aceDoc) {
		List<String> triples = new ArrayList<String>();
		for (AceRelation r : aceDoc.relations) {
			List<AceEntityName> arg1Names = ((AceEntity) r.arg1).names;
			List<AceEntityName> arg2Names = ((AceEntity) r.arg2).names;

			int size1 = arg1Names.size();
			int size2 = arg2Names.size();

			if (size1 == 0 || size2 == 0)
				continue;

			StringBuilder tripleSB = new StringBuilder();
			Set<String> argNamesSet = new TreeSet<String>();

			// String pattern = r.patterns.get(0); // type pattern
			String subtypePattern = r.subtypePatterns.get(0);// subtype pattern

			tripleSB.append(subtypePattern + " | ");

			// form triples over all combination of names between arg1 and arg2
			for (AceEntityName n1 : arg1Names) {
				for (AceEntityName n2 : arg2Names) {
					String name1 = n1.text.replaceAll("\\s+", " ");
					String name2 = n2.text.replaceAll("\\s+", " ");

					if (argNamesSet.contains(name1 + name2)) // don't write the same names
						continue;

					argNamesSet.add(name1 + name2);

					tripleSB.append(name1 + ":" + r.type + ":" + name2 + "=");
				}
			}

			String tripleStr = tripleSB.deleteCharAt(tripleSB.length() - 1).toString(); // delete the last redundant '='
			triples.add(tripleStr);
		}
		return triples;
	}

	// returns the longest name of an entity argument
	public static String getLongest(List<AceEntityName> names) {
		AceEntityName longest = names.get(0);

		for (AceEntityName element : names) {
			if (element.text.replaceAll("\\s+", " ").length() > longest.text.replaceAll("\\s+", " ").length()) {
				longest = element;
			}
		}

		return longest.text.replaceAll("\\s+", " ");
	}

	/**
	 * Returns the name of entity 'e', or <CODE>null</CODE> if it has no name. Not in use anymore.
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
}
