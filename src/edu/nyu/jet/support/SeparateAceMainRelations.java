package edu.nyu.jet.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class SeparateAceMainRelations {

  public static void Separate(String inputFile) {
    BufferedReader input = null;
    BufferedWriter output = null, output1 = null, output2 = null, output3 = null, output4 = null, output5 = null;
    String inputLine;

    try {
      input = new BufferedReader(new FileReader(inputFile));

      output = new BufferedWriter(new FileWriter(
          "/Users/nuist/documents/ThesisSummer2016/ice-eval/aceKeyTriples_orgaff"));
      output1 = new BufferedWriter(new FileWriter(
          "/Users/nuist/documents/ThesisSummer2016/ice-eval/aceKeyTriples_phys"));
      output2 = new BufferedWriter(new FileWriter(
          "/Users/nuist/documents/ThesisSummer2016/ice-eval/aceKeyTriples_partwhole"));
      output3 = new BufferedWriter(new FileWriter(
          "/Users/nuist/documents/ThesisSummer2016/ice-eval/aceKeyTriples_genaff"));
      output4 = new BufferedWriter(new FileWriter(
          "/Users/nuist/documents/ThesisSummer2016/ice-eval/aceKeyTriples_persoc"));
      output5 = new BufferedWriter(new FileWriter(
          "/Users/nuist/documents/ThesisSummer2016/ice-eval/aceKeyTriples_art"));

      while ((inputLine = input.readLine()) != null) {
        if (inputLine.contains(":ORG-AFF:")) {
          output.write(inputLine + "\n");
        } else if (inputLine.contains(":PHYS:")) {
          output1.write(inputLine + "\n");
        } else if (inputLine.contains(":PART-WHOLE:")) {
          output2.write(inputLine + "\n");
        } else if (inputLine.contains(":GEN-AFF:")) {
          output3.write(inputLine + "\n");
        } else if (inputLine.contains(":PER-SOC:")) {
          output4.write(inputLine + "\n");
        } else if (inputLine.contains(":ART:")) {
          output5.write(inputLine + "\n");
        }
      }

      input.close();
      output.close();
      output1.close();
      output2.close();
      output3.close();
      output4.close();
      output5.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    Separate("/Users/nuist/documents/ThesisSummer2016/ice-eval/aceKeyTriples");
  }
}