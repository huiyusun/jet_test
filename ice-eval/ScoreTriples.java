import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class ScoreTriples {

    public static void main (String[] args) throws IOException{

        //
        //  read in lists of triples
        //
        Set<String> key = new TreeSet(
                Files.readAllLines(new File("keyTriples").toPath(), 
                StandardCharsets.UTF_8));
        Set<String> response = new TreeSet(
                Files.readAllLines(new File("responseTriples").toPath(), 
                StandardCharsets.UTF_8));
        List<String> assessments = 
                Files.readAllLines(new File("assessments").toPath(), 
                StandardCharsets.UTF_8);
        //
        //  divide assessments
        //
        Set<String> positive = new TreeSet<String>();
        Set<String> negative = new TreeSet<String>();
        for (String s : assessments) {
            int paren = s.indexOf("(");
            if (paren >= 0)
                s = s.substring(0, paren).trim();
            if (s.startsWith("+ "))
                positive.add(s.substring(2));
            else
                negative.add(s.substring(2));
        }
        //
        //  compute recall:
        //      of triples found by LDC (key),
        //      fraction also found by system (correct = response ^ key)
        //
        Set<String> correct = new TreeSet<String>(response);
        correct.retainAll(key);
        float recall = (float) correct.size() / (float) key.size();
        System.out.println("key = " + key.size());
        System.out.println("response = " + response.size());
        System.out.println("correct = " + correct.size());
        System.out.println("RECALL = " + recall);
        //
        //  compute precision:
        //      of triples found by system (response),
        //      fraction classified correct (correct + positive)
        correct.addAll(positive);
        float precision = (float) correct.size() / (float) response.size();
        System.out.println("correct = " + correct.size());
        System.out.println("PRECISION = " + precision);
        //
        //  list unclassified triples
        //
        Set<String> unknown = new TreeSet<String>(response);
        unknown.removeAll(key);
        unknown.removeAll(positive);
        unknown.removeAll(negative);
        for (String s : unknown)
            System.out.println(s);
    }

}
