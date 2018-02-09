package edu.nyu.jet.support;

// create all possible subtype patterns for patterns between main argument types
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CreateSubtypePatterns {
	static HashMap<String, Set<String>> entityTypeSubtype = new HashMap<String, Set<String>>();

	public static void Separate() {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		addTypeSubtypes(); // add all subtypes of each main entity argument type

		try {
			input = new BufferedReader(
					new FileReader("/Users/nuist/documents/NlpResearch/ice-eval/patterns_subtypes_changed_art.pos"));
			output = new BufferedWriter(
					new FileWriter("/Users/nuist/documents/NlpResearch/ice-eval/patterns_subtypes_subtypes_art.pos"));

			while ((inputLine = input.readLine()) != null) {
				// output.write(inputLine + "\n");

				String ldp = inputLine.split("=")[0].trim();
				String arg1 = ldp.split("--")[0].trim();
				String arg2 = ldp.split("--")[2].trim();
				String path = ldp.split("--")[1].trim();
				String relation = inputLine.split("=")[1].trim();

				for (String subtype1 : entityTypeSubtype.get(arg1)) {
					for (String subtype2 : entityTypeSubtype.get(arg2)) {
						output.write(arg1 + ":" + subtype1 + "--" + path + "--" + arg2 + ":" + subtype2 + " = " + relation + "\n");
					}
				}
			}

			input.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addTypeSubtypes() {
		HashSet<String> per = new HashSet<String>();
		HashSet<String> org = new HashSet<String>();
		HashSet<String> gpe = new HashSet<String>();
		HashSet<String> loc = new HashSet<String>();
		HashSet<String> fac = new HashSet<String>();
		HashSet<String> veh = new HashSet<String>();
		HashSet<String> wea = new HashSet<String>();

		per.add("Individual");
		per.add("Group");
		// person.add("Indeterminate");

		org.add("Commercial");
		// org.add("Educational");
		// org.add("Entertainment");
		org.add("Government");
		// org.add("Media");
		org.add("Medical-Science");
		org.add("Non-Governmental");
		// org.add("Religious");
		org.add("Sports");

		// gpe.add("Continent");
		// gpe.add("County-or-District");
		// gpe.add("GPE-Cluster");
		gpe.add("Nation");
		// gpe.add("Population-Center");
		// gpe.add("Special");
		// gpe.add("State-or-Province");

		// loc.add("Address");
		// loc.add("Boundary");
		// loc.add("Celestial");
		// loc.add("Land-Region-Natural");
		loc.add("Region-General");
		// loc.add("Region-International");
		loc.add("Water-Body");

		fac.add("Airport");
		fac.add("Building-Grounds");
		// fac.add("Path");
		// fac.add("Plant");
		// fac.add("Subarea-Facility");

		veh.add("Air");
		veh.add("Land");
		// veh.add("Subarea-Vehicle");
		// veh.add("Underspecified");
		veh.add("Water");

		// wea.add("Biological");
		// wea.add("Blunt");
		// wea.add("Chemical");
		// wea.add("Exploding");
		// wea.add("Nuclear");
		wea.add("Projectile");
		// wea.add("Sharp");
		// wea.add("Shootingl");
		// wea.add("Underspecified");

		entityTypeSubtype.put("PERSON", per);
		entityTypeSubtype.put("ORGANIZATION", org);
		entityTypeSubtype.put("GPE", gpe);
		entityTypeSubtype.put("LOCATION", loc);
		entityTypeSubtype.put("FACILITY", fac);
		entityTypeSubtype.put("VEH", veh);
		entityTypeSubtype.put("WEA", wea);
	}

	public static void main(String[] args) {
		Separate();
	}
}