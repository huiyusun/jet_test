package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GetFrequentRelationDocList {

	public static void Separate() {
		BufferedReader input = null, input1 = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			input = new BufferedReader(
					new FileReader("/Users/nuist/documents/NlpResearch/ice-eval/allRelationDoc_frequent_GEN-AFF"));
			input1 = new BufferedReader(new FileReader("/Users/nuist/jetx/data/ACE/2005/allSgmList_all.txt"));
			output = new BufferedWriter(new FileWriter("/Users/nuist/jetx/data/ACE/2005/allSgmList_frequent_GEN-AFF"));

			HashMap<String, String> docList = new HashMap<String, String>();

			while ((inputLine = input1.readLine()) != null) {
				docList.put(inputLine.split("/")[1], inputLine);
			}

			while ((inputLine = input.readLine()) != null) {
				String[] lineArr = inputLine.split("=");
				if (lineArr.length >= 1) {
					String docId = lineArr[1].trim() + ".sgm";

					if (docList.containsKey(docId)) {
						output.write(docList.get(docId) + "\n");
					}
				}
			}

			input.close();
			input1.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Separate();
	}
}