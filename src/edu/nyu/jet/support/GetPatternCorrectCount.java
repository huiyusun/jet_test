package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

// get patterns with correct argument types for a relation type from PatternsCorrectCount
public class GetPatternCorrectCount {
	public static void Separate() {
		BufferedReader input = null;
		BufferedWriter output = null;
		String line;

		// normal argument types for each relation (not inverse)
		HashSet<String> relationsSetOrg = new HashSet<String>(); // change which relationsSet below when writing to output
		relationsSetOrg.add("PERSON ORGANIZATION");
		relationsSetOrg.add("PERSON GPE");
		relationsSetOrg.add("ORGANIZATION ORGANIZATION");
		relationsSetOrg.add("ORGANIZATION GPE");
		relationsSetOrg.add("GPE ORGANIZATION");
		relationsSetOrg.add("GPE GPE");

		HashSet<String> relationsSetGen = new HashSet<String>();
		relationsSetGen.add("PERSON PERSON");
		relationsSetGen.add("PERSON LOCATION");
		relationsSetGen.add("PERSON GPE");
		relationsSetGen.add("PERSON ORGANIZATION");
		relationsSetGen.add("ORGANIZATION LOCATION");
		relationsSetGen.add("ORGANIZATION GPE");

		HashSet<String> relationsSetPart = new HashSet<String>();
		relationsSetPart.add("FACILITY FACILITY");
		relationsSetPart.add("FACILITY LOCATION");
		relationsSetPart.add("FACILITY GPE");
		relationsSetPart.add("LOCATION FACILITY");
		relationsSetPart.add("LOCATION LOCATION");
		relationsSetPart.add("LOCATION GPE");
		relationsSetPart.add("GPE FACILITY");
		relationsSetPart.add("GPE LOCATION");
		relationsSetPart.add("GPE GPE");
		relationsSetPart.add("ORGANIZATION ORGANIZATION");
		relationsSetPart.add("ORGANIZATION GPE");
		relationsSetPart.add("VEHICLE VEHICLE");
		relationsSetPart.add("WEAPON WEAPON");

		HashSet<String> relationsSetPhys = new HashSet<String>();
		relationsSetPhys.add("PERSON FACILITY");
		relationsSetPhys.add("PERSON LOCATION");
		relationsSetPhys.add("PERSON GPE");
		relationsSetPhys.add("FACILITY FACILITY");
		relationsSetPhys.add("FACILITY GPE");
		relationsSetPhys.add("FACILITY LOCATION");
		relationsSetPhys.add("LOCATION FACILITY");
		relationsSetPhys.add("LOCATION GPE");
		relationsSetPhys.add("LOCATION LOCATION");
		relationsSetPhys.add("GPE FACILITY");
		relationsSetPhys.add("GPE LOCATION");
		relationsSetPhys.add("GPE GPE");

		HashSet<String> relationsSetPersoc = new HashSet<String>();
		relationsSetPersoc.add("PERSON PERSON");

		try {
			input = new BufferedReader(new FileReader("/Users/nuist/documents/NlpResearch/ice-eval/patternsCorrectCount"));
			output = new BufferedWriter(
					new FileWriter("/Users/nuist/documents/NlpResearch/ice-eval/patternsCorrectCount_ORG-AFF_perfect_v1"));

			while ((line = input.readLine()) != null) {
				String pattern = line.split("=")[0].trim();
				String arg1 = pattern.split("--")[0].trim();
				String arg2 = pattern.split("--")[2].trim();
				String type = line.split("=")[1].trim();

				if (arg1.contains(":") && arg2.contains(":")) {
					arg1 = arg1.split(":")[0].trim();
					arg2 = arg2.split(":")[0].trim();
				}

				if (type.contains("-1")) {
					if (relationsSetOrg.contains(arg2 + " " + arg1)) // change which set here Org, Gen, Part, Phys, Persoc
						output.write(line + "\n");
				} else {
					if (relationsSetOrg.contains(arg1 + " " + arg2)) // and here
						output.write(line + "\n");
				}
			}

			input.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Separate();
	}
}