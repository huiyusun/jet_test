package edu.nyu.jet.support;

// create all possible subtype patterns for patterns between main argument types
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class CombineSubtypeAndTypePatterns {

	public static void Separate() {
		BufferedWriter output = null;
		String dir = "/Users/nuist/documents/NlpResearch/ice-eval/";
		String typeFile = dir + "patterns_subtypes_changed_art.pos";
		String subtypeFile = dir + "results_subtypes_individual_subtypes_art";

		try {
			Set<String> types = new TreeSet(Files.readAllLines(new File(typeFile).toPath(), StandardCharsets.UTF_8));
			Set<String> subtypes = new TreeSet(Files.readAllLines(new File(subtypeFile).toPath(), StandardCharsets.UTF_8));
			output = new BufferedWriter(new FileWriter(dir + "results_subtypes_individual_art_typeSubtype"));
			Set<String> removed = new TreeSet<String>();

			for (String type : types) {
				if (type.contains("[BEST]") || type.contains("= SAME = "))
					continue;
				output.write("T: " + type + "\n");
				String typeRelation = type.split("=")[1].trim();
				for (String subtype : subtypes) {
					if (subtype.contains("= NO =") || subtype.contains("= YES =")) {
						String path = subtype.split("=")[0].trim();
						String mainPath = path.split("--")[0].split(":")[0] + "--" + path.split("--")[1] + "--"
								+ path.split("--")[2].split(":")[0];
						String subtypeRelation = subtype.split("=")[1].trim();
						if (type.contains(mainPath) && subtypeRelation.equals(typeRelation)) {
							output.write("S: " + subtype + "\n");
							removed.add(subtype);
						}
					}
				}
			}

			subtypes.removeAll(removed);
			for (String subtype : subtypes) {
				if (subtype.contains("= YES =") || subtype.contains("= NO ="))
					output.write("Extra: " + subtype + "\n");
			}

			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Separate();
	}
}