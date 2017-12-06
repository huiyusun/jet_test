package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class ReverseAceTriples {

	public static void Separate(String inputFile) {
		BufferedReader input = null;
		BufferedWriter output = null;
		String inputLine;

		try {
			input = new BufferedReader(new FileReader(inputFile));

			output = new BufferedWriter(
					new FileWriter("/Users/nuist/documents/NlpResearch/ice-eval/aceResponseTriples_Inverse"));

			while ((inputLine = input.readLine()) != null) {
				String[] lineArr = inputLine.split(":");
				output.write(lineArr[2] + ":" + lineArr[1] + ":" + lineArr[0] + "\n");
			}

			input.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Separate("/Users/nuist/documents/NlpResearch/ice-eval/aceResponseTriples");
	}
}