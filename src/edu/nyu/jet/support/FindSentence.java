package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.process.DocumentPreprocessor;

/*
 * find sentence in ACE for a relation argument pair using Stanford CoreNLP tokenizer. 
 * Only include entity pairs appearing in the same sentence. Refer to aceMentionsKey for full list of mentions.
 */

public class FindSentence {

	private static final Pattern END_OF_SENTENCE = Pattern.compile("(?<=[.!?])\\s+"); // or "[?!.]($|\\s)"
	private static final Pattern END_OF_PARAGRAPH = Pattern.compile("r\n\n"); // or "r\n\n"

	public static void Clean(String inputFile) {
		BufferedReader input = null;
		BufferedWriter output = null, output1 = null;
		String inputLine;
		String trainingDirPath = "/Users/nuist/jetx/data/ACE/2005/training/";

		try {
			input = new BufferedReader(new FileReader(inputFile));
			output = new BufferedWriter(new FileWriter("/Users/nuist/documents/NlpResearch/ice-eval/aceSentencesKey"));
			output1 = new BufferedWriter(
					new FileWriter("/Users/nuist/documents/NlpResearch/ice-eval/aceSentencesKey_noMentions"));

			HashSet<String> triplesSet = new HashSet<String>();

			while ((inputLine = input.readLine()) != null) {
				triplesSet.add(inputLine); // read in lines from aceTriplesKey file
			}

			int count = 0;

			for (String line : triplesSet) {
				String[] lineArr = line.split("=");
				String triple = lineArr[0].trim();
				String arg1 = triple.split(":")[0].split("\\(")[0].trim();
				String arg2 = triple.split(":")[2].split("\\(")[0].trim();
				;

				String fileName = lineArr[1].trim();
				String filePath = "";

				if (new File(trainingDirPath + "bc/" + fileName + ".sgm").exists()) {
					filePath = trainingDirPath + "bc/" + fileName + ".sgm";
				} else if (new File(trainingDirPath + "bn/" + fileName + ".sgm").exists()) {
					filePath = trainingDirPath + "bn/" + fileName + ".sgm";
				} else if (new File(trainingDirPath + "cts/" + fileName + ".sgm").exists()) {
					filePath = trainingDirPath + "cts/" + fileName + ".sgm";
				} else if (new File(trainingDirPath + "nw/" + fileName + ".sgm").exists()) {
					filePath = trainingDirPath + "nw/" + fileName + ".sgm";
				} else if (new File(trainingDirPath + "un/" + fileName + ".sgm").exists()) {
					filePath = trainingDirPath + "un/" + fileName + ".sgm";
				} else if (new File(trainingDirPath + "wl/" + fileName + ".sgm").exists()) {
					filePath = trainingDirPath + "wl/" + fileName + ".sgm";
				} else {
					System.out.println(fileName + " doesn't exist");
				}

				Reader reader = new FileReader(new File(filePath));
				String sentence = getSentence(reader, arg1, arg2); // get sentence in training file containing both args

				if (sentence != null) {
					output.write(triple + " = " + sentence + "\n");
					output.write("\n" + "\n");
				} else {
					count++;
					output1.write(line + "\n");

					// String paragraph = getParagraph(content, arg1, arg2); // get paragraph containing both args

					// if (paragraph != null) {
					// output.write(arguments + " = " + paragraph + "\n");
					// output.write("\n" + "\n");
					// } else {
					System.out.println(triple);
					// }
				}

				reader.close(); // close scanner after each loop
			}

			System.out.println("Count = " + count);
			input.close();
			output.close();
			output1.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// find sentence using Stanford CoreNLP tokenizer
	public static String getSentence(Reader reader, String arg1, String arg2) {
		DocumentPreprocessor dp = new DocumentPreprocessor(reader);

		for (List<HasWord> sentence : dp) {
			// SentenceUtils not Sentence
			String sentenceString = SentenceUtils.listToString(sentence);
			if (sentenceString.contains(arg1) && sentenceString.contains(arg2)) {
				return sentenceString;
			}
		}

		return null;
	}

	// find sentence containing both arguments using regex
	public static String getSentence1(String text, String arg1, String arg2) {
		for (String sentence : END_OF_SENTENCE.split(text)) {
			if (sentence.contains(arg1) && sentence.contains(arg2)) {
				return sentence;
			}
		}
		return null;
	}

	// find paragraph containing both arguments
	public static String getParagraph(String text, String arg1, String arg2) {
		for (String paragraph : END_OF_PARAGRAPH.split(text)) {
			if (paragraph.contains(arg1) && paragraph.contains(arg2)) {
				return paragraph;
			}
		}
		return null;
	}

	public static void main(String[] args) {
		// "/Users/nuist/documents/NlpResearch/AL-patterns/art/itr1"
		// "/Users/nuist/documents/workspaceNLP/jet_master/data/ldpRelationModel"
		Clean("/Users/nuist/documents/NlpResearch/ice-eval/aceTriplesKey");
	}
}