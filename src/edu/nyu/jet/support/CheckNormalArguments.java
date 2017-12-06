package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;

public class CheckNormalArguments {

	public static void Clean() {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			input = new BufferedReader(new FileReader("/Users/nuist/documents/workspaceNLP/jet_master/data/relationOracle"));

			output = new BufferedWriter(
					new FileWriter("/Users/nuist/documents/workspaceNLP/jet_master/data/relationOracle_wrongArgs"));

			HashSet<String> relationsSetOrg = new HashSet<String>(); // change which relationsSet below when writing to output
			relationsSetOrg.add("PERSON ORGANIZATION");
			relationsSetOrg.add("PERSON GPE");
			relationsSetOrg.add("ORGANIZATION ORGANIZATION");
			// relationsSet.add("ORGANIZATION GPE");
			relationsSetOrg.add("GPE GPE");
			relationsSetOrg.add("GPE ORGANIZATION");

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
			// relationsSetPart.add("LOCATION FACILITY");
			relationsSetPart.add("LOCATION LOCATION");
			relationsSetPart.add("LOCATION GPE");
			// relationsSetPart.add("GPE FACILITY");
			// relationsSetPart.add("GPE LOCATION");
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
			// relationsSetPhys.add("LOCATION FACILITY");
			relationsSetPhys.add("LOCATION GPE");
			relationsSetPhys.add("LOCATION LOCATION");
			// relationsSetPhys.add("GPE FACILITY");
			// relationsSetPhys.add("GPE LOCATION");
			relationsSetPhys.add("GPE GPE");

			HashSet<String> relationsSetPersoc = new HashSet<String>();
			relationsSetPersoc.add("PERSON PERSON");

			while ((inputLine = input.readLine()) != null) {
				if (!inputLine.isEmpty() && inputLine.contains("= EMPLOYMENT")) { // use "= ART" for agent-artifact
					String relation = inputLine.split("=")[1].trim();
					String[] repr = inputLine.split("=")[0].split(" ");
					String arg1 = null, arg2 = null;
					boolean arg1Matched = false, arg2Matched = false;
					int i = 0;

					// get first and second argument from a repr pattern
					while ((!arg1Matched || !arg2Matched) && i < repr.length) {
						if (repr[i].equals(repr[i].toUpperCase()) && repr[i].matches("[A-Z]+")) { // e.g. ORGANIZATION
							if (!arg1Matched) {
								arg1 = repr[i].trim();
								arg1Matched = true;
							} else if (!arg2Matched) {
								arg2 = repr[i].trim();
								arg2Matched = true;
							}
						}
						i++;
					}

					String args = arg1 + " " + arg2;

					if (!relation.contains(("-1")) && !relationsSetOrg.contains(args)) { // incorrect argument order
						output.write(inputLine + "\n");
					}

					if (relation.contains(("-1")) && relationsSetOrg.contains(args)) { // incorrect argument order
						output.write(inputLine + "\n");
					}
				}
			}

			input.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// "/Users/nuist/documents/NlpResearch/AL-patterns/art/itr1"
		// "/Users/nuist/documents/workspaceNLP/jet_master/data/ldpRelationModel"
		Clean();
	}
}