package AceJet;

import java.util.*;
import java.io.*;

import Jet.Tipster.*;

/**
 *  Converts information about ACE relations in an APF file into a list
 *  of triples.
 *  <br>
 *  If one of the APF documents contains a relation of type R between
 *  entities E1 and E2, where both E1 and E2 have names and the first
 *  names associated with E1 and E2 are N1 and N2, writes a line of the form
 *  <br>
 *  N1:R:N2.
 */

public class ApfToTriples {

        /**
         *  converts ACE relations in an APF file into triples.
         *  Takes an array of 5 arguments:  <ul>
         *  <li>  the directory containing the source and APF fies
         *  <li>  a file containing a list (one per line) of the document (file) names
         *  <li>  the file extension of the source files
         *  <li>  the file extension of the APF files
         *  <li>  the output file which will contain the triples
         *  </ul>
         *  If the same relation between the same individuals appears several times
         *  across the input collection, multiple triples will be generated.
         */

	public static void main (String[] args) throws IOException {
                if (args.length != 5) {
                    System.err.println
                        ("ApfToTriples requires 5 arguments: inDir docList sourceFileExt apfFileExt outFile");
                    System.exit(1);
                }
		String inDir = args[0];
		String docList = args[1];
		String sourceFileExt = args[2];
		String apfFileExt = args[3];
		String outFile = args[4];
		List<String> allTriples = new ArrayList<String>();
		String line;
		BufferedReader docIdReader = new BufferedReader (new FileReader (docList));
		while ((line = docIdReader.readLine()) != null) {
			String sourceFile = inDir + line + "." + sourceFileExt;
			String apfFile = inDir + line + "." + apfFileExt;
			ExternalDocument doc = new ExternalDocument("sgml", sourceFile);
        	        doc.setAllTags(true);
                	doc.open();
                	AceDocument aceDoc = new AceDocument(sourceFile, apfFile);
			allTriples.addAll(makeTriples(doc, aceDoc));
		}
		for (String trip : allTriples) {
			System.out.println (trip);
		}
	}

        /**
         *  Converts the ACE relations in one document into triples.
         *  @param  doc       the source file of the document
         *  @param  aceDoc    the APF file of the document
         *  @return           a list of relation triples
         */

	public static List<String> makeTriples (Document doc, AceDocument aceDoc) {
		List<String> triples = new ArrayList<String>();
		for (AceRelation r : aceDoc.relations) {
			AceEntity arg1 = r.arg1;
			String arg1Name = nameOfEntity(arg1);
			AceEntity arg2 = r.arg2;
			String arg2Name = nameOfEntity(arg2);
			if (arg1Name == null ||  arg2Name == null)
				continue;
			String triple = arg1Name + ":" +
					r.type + ":" +
					arg2Name;
			triples.add(triple);
		}
		return triples;
	}
	
        /**
         *  Returns the name of entity 'e', or <CODE>null</CODE> if it has no name.
         */

	public static String nameOfEntity (AceEntity e) {
		List<AceEntityName> names = e.names;
		if (names != null && names.size() > 0)
			return names.get(0).text.replaceAll("\\s+", " ");
		else
			return null;
	}
}		
