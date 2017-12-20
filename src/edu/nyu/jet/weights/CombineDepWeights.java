package edu.nyu.jet.weights;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class CombineDepWeights {

	public static void Combine(String file1, String file2, String combinedFile) {
		BufferedReader input = null, input1 = null;
		BufferedWriter output = null;
		String line;

		try {
			input = new BufferedReader(new FileReader(file1));
			input1 = new BufferedReader(new FileReader(file2));
			output = new BufferedWriter(new FileWriter(combinedFile));

			HashMap<String, String> depWeights = new HashMap<String, String>();

			int count = 0;

			while ((line = input.readLine()) != null) {
				if (!line.isEmpty()) {
					String dep = line.split("=")[0].trim();
					String score = line.split("=")[1].trim();
					depWeights.put(dep, score);
				}
			}

			while ((line = input1.readLine()) != null) {
				if (!line.isEmpty()) {
					String dep = line.split("=")[0].trim();
					String score = line.split("=")[1].trim();

					if (depWeights.containsKey(dep)) {
						if (!depWeights.get(dep).equals(score)) { // remove cases where same dependency but different scores
							depWeights.remove(dep);
							count++;
							// System.out.println(dep);
						}
					} else {
						depWeights.put(dep, score);
					}
				}
			}

			for (String dep : depWeights.keySet()) {
				output.write(dep + " = " + depWeights.get(dep) + "\n");
			}

			System.out.println("Total conflicting scores: " + count);

			input.close();
			input1.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
	}
}