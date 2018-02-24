package edu.nyu.jet.supervised;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.nyu.jet.models.PathRelationExtractor;

// combines type and subtype LDPs produced by supervised training.
public class PipelineProcessingTypeSubtype {
	private static HashMap<String, String> subtypeType = new HashMap<String, String>();

	// combine all type and subtype LDPs into a single file
	public static void combineTypesAndSubtypes() {
		BufferedWriter output = null;
		String dir = "/Users/nuist/documents/NlpResearch/Oracle-AL/SupervisedLDP/";
		String file = dir + "patterns_supervised_train_type_v1";
		String file1 = dir + "patterns_supervised_train_subtype_v1";

		try {
			ArrayList<String> types = new ArrayList<String>();
			Map<String, Set<String>> subtypes = new TreeMap<String, Set<String>>();
			entityTypeAndSubtypeMap("/Users/nuist/documents/NlpResearch/ice-eval/aceEntityTypeSubtype");
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
				// String type = line.split(" = ")[1].trim().split("\\|")[0].trim();
				String typePath = typeArg1 + "--" + lex + "--" + typeArg2;
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
				String typePath = type.split("=")[0].trim();
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

	// select the subtype LDPs to be added to the type ones
	public static void addSubtypes() {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			String dir = "/Users/nuist/documents/NlpResearch/Oracle-AL/SupervisedLDP/";
			input = new BufferedReader(new FileReader(dir + "patterns_supervised_train_typeSubtype"));
			output = new BufferedWriter(new FileWriter(dir + "patterns_supervised_train_typeSubtype_v1"));
			String curType = "";

			while ((inputLine = input.readLine()) != null) {
				if (!inputLine.contains("="))
					continue;
				String path = inputLine.split("=")[0].trim();
				String type = inputLine.split("=")[1].trim();
				if (path.substring(0, 3).equals("T: ")) {
					output.write(inputLine + "\n");
					curType = type;
				} else if (path.substring(0, 3).equals("S: ")) {
					if (!type.equals(curType))
						output.write(inputLine + "\n");
				}
			}

			input.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		combineTypesAndSubtypes();
		addSubtypes();
	}
}