package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class ChangeRelationTypes {

  public static void Clean(String inputFile) {
    BufferedReader input = null;
    BufferedWriter output = null;
    String inputLine, relation;

    try {
      input = new BufferedReader(new FileReader(inputFile));

      output = new BufferedWriter(new FileWriter(
          "/Users/nuist/documents/NlpResearch/ice-eval/patterns"));

      String newRelation = "PART-WHOLE";

      while ((inputLine = input.readLine()) != null) {
        if (!inputLine.isEmpty()) {
          String[] lineArr = inputLine.split("\t");
          relation = lineArr[1];

          if (relation.contains("-1")) {
            output.write(lineArr[0] + "\t" + newRelation + "-1" + "\n"); // add -1
          } else {
            output.write(lineArr[0] + "\t" + newRelation + "\n"); // change to the new relation
          }
        }
      }

      input.close();
      output.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    Clean("/Users/nuist/documents/ThesisSummer2016/ice-eval/pattern/patterns_all");
  }
}