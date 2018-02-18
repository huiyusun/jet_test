package edu.nyu.jet.supervised;

// create all possible subtype patterns for patterns between main argument types
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.nyu.jet.models.PathMatcher;

public class CombineTypeSubtype {

	private static HashMap<String, String> subtypeType = new HashMap<String, String>();

	public static void Separate() {
		BufferedWriter output = null;
		String dir = "/Users/nuist/documents/NlpResearch/ice-eval/";
		String file = dir + "patterns_supervised_train_selected";
		String file1 = dir + "patterns_supervised_train_selected_subtype";

		try {
			ArrayList<String> types = new ArrayList<String>();
			Map<String, Set<String>> subtypes = new TreeMap<String, Set<String>>();
			entityTypeAndSubtypeMap(dir + "aceEntityTypeSubtype");
			BufferedReader br = new BufferedReader(new FileReader(file));
			BufferedReader br1 = new BufferedReader(new FileReader(file1));
			String line = null;

			while ((line = br.readLine()) != null)
				types.add(line);
			while ((line = br1.readLine()) != null) {
				String path = line.split(" = ")[0].trim();
				String typeArg1 = subtypeType.get(path.split("--")[0].trim());
				String typeArg2 = subtypeType.get(path.split("--")[2].trim());
				String lex = path.split("--")[1].trim();
				String type = line.split(" = ")[1].trim().split("\\|")[0].trim();
				String typePath = typeArg1 + "--" + lex + "--" + typeArg2 + " = " + type;
				if (subtypes.get(typePath) == null)
					subtypes.put(typePath, new HashSet<String>());
				subtypes.get(typePath).add(line);
			}
			br.close();
			br1.close();

			output = new BufferedWriter(new FileWriter(dir + "patterns_supervised_train_typeSubtype"));
			int count = 1;

			for (String type : types) {
				output.write(count + "\n");
				output.write("T: " + type + "\n");
				String typePath = type.split("\\|")[0].trim();
				count++;

				if (subtypes.get(typePath) != null) {
					for (String subtype : subtypes.get(typePath)) {
						output.write("S: " + subtype + "\n");
					}
				}
			}

			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void entityTypeAndSubtypeMap(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;

		while ((line = br.readLine()) != null) {
			String type = line.split(":")[0].trim();
			String[] subtypes = line.split(":")[1].trim().split(",");
			for (int i = 0; i < subtypes.length; i++)
				subtypeType.put(subtypes[i].trim(), type);
		}

		br.close();
	}

	public static void main(String[] args) {
		Separate();
	}
}