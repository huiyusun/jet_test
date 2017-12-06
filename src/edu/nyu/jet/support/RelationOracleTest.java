package edu.nyu.jet.support;// -*- tab-width: 4 -*-

import java.util.*;
import java.io.*;

public class RelationOracleTest {

	static String jetHome = "/Users/nuist/documents/workspaceNLP/jet_master";

	static Set<String> knownRepr = new HashSet<String>();
	static Set<String> knownLDP = new HashSet<String>();

	// In case previous choices have been altered
	public static void alterChoice() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(jetHome + "/data/relationOracleTest")));

			// use linked hash map to keep original order
			LinkedHashMap<String, String> alteredRepr = new LinkedHashMap<String, String>();

			String line;

			while ((line = br.readLine()) != null) {
				String repr = line.split(":")[0].trim();
				String choice = line.split(":")[1].trim();
				
				if (alteredRepr.containsKey(repr)) {
					alteredRepr.remove(repr);
				}
				
				alteredRepr.put(repr, choice);
			}

			br.close();
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(jetHome + "/data/relationOracleTest")));

			for (String key : alteredRepr.keySet()) {
				bw.write(key + " : " + alteredRepr.get(key) + "\n");
			}

			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		alterChoice();
	}
}
