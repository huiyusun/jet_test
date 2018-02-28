package edu.nyu.jet.supervised;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

import edu.nyu.jet.models.PathRelationExtractor;

public class ChooseRelationType {
	static Set<String> person = new HashSet<String>();
	static {
		person.add("PERSON");
		person.add("Group");
		person.add("Indeterminate");
		person.add("Individual");
	}

	public static void Separate() {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			input = new BufferedReader(
					new FileReader("/Users/nuist/documents/NlpResearch/ice-eval/patterns_supervised_train"));

			output = new BufferedWriter(
					new FileWriter("/Users/nuist/documents/NlpResearch/ice-eval/patterns_supervised_train_selected"));

			while ((inputLine = input.readLine()) != null) {
				String pattern = inputLine.split(" ")[0].trim();
				if (pattern.contains("="))
					continue;
				String arg1 = pattern.split("--")[0].trim();
				String arg2 = pattern.split("--")[2].trim();
				boolean isPersoc = false;
				if (person.contains(arg1) && person.contains(arg2))
					isPersoc = true;
				String[] relations = inputLine.split(" ");
				String best = "EMPTY", bestInv = "EMPTY";
				double score = 0, scoreInv = 0;

				for (int i = 1; i < relations.length; i++) { // start at second element
					String relation = relations[i].trim().split("=")[0].trim();
					double cur = Integer.parseInt(relations[i].trim().split("=")[1].trim());
					if (relation.contains("OTHER"))
						cur = cur * 0.5;
					if (!relation.contains("-1")) { // normal relation outcome
						if (cur > score) {
							score = cur;
							best = relation;
						} else if (Math.abs(cur - score) <= 0.000001) {
							HashSet<String> temp = new HashSet<String>();
							temp.add(best);
							temp.add(relation);
							temp = PathRelationExtractor.singleRelationType(temp);
							best = temp.iterator().next();
						}
					} else {
						if (cur > scoreInv) {
							scoreInv = cur;
							bestInv = relation;
						} else if (Math.abs(cur - scoreInv) <= 0.000001) {
							HashSet<String> temp = new HashSet<String>();
							temp.add(bestInv);
							temp.add(relation);
							temp = PathRelationExtractor.singleRelationType(temp);
							bestInv = temp.iterator().next();
						}
					}
				}

				if (isPersoc) {
					if (best.contains("OTHER"))
						best = best.replace("-1", "");
					if (bestInv.contains("OTHER"))
						bestInv = bestInv.replace("-1", "");
				}

				if (best.equals("EMPTY") && bestInv.equals("EMPTY")) {
					continue;
				} else if (best.equals("EMPTY")) {
					best = bestInv;
					output.write(pattern + " = " + best + "\n");
					continue;
				} else if (bestInv.equals("EMPTY")) {
					output.write(pattern + " = " + best + "\n");
					continue;
				}

				if (!best.contains("OTHER") && !bestInv.contains("OTHER")) {
					if (scoreInv > score)
						best = bestInv;
					output.write(pattern + " = " + best + "\n");
				} else if (!best.equals("OTHER")) {
					output.write(pattern + " = " + best + "\n");
				} else if (!bestInv.equals("OTHER")) {
					best = bestInv;
					output.write(pattern + " = " + best + "\n");
				} else {
					output.write(pattern + " = " + best + "\n");
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