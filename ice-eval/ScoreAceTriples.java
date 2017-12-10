import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class ScoreAceTriples {

    public static void main(String[] args) throws IOException {
        // read in lists of triples
        Set<String> key = new TreeSet(Files.readAllLines(new File("aceKeyTriples").toPath(), StandardCharsets.UTF_8));
        Set<String> response = new TreeSet(
                Files.readAllLines(new File("aceResponseTriples").toPath(), StandardCharsets.UTF_8));

        // Resolve triples with more than one names
        Set<String> temp = new TreeSet<String>();
        Iterator<String> itr = response.iterator();

        while (itr.hasNext()) {
            String triple = itr.next();
            if (triple.contains("=")) { // line contains multiple triples
                boolean isRemove = false;
                String[] triples = triple.split("=");

                for (int i = 0; i < triples.length; i++) {
                    if (key.contains(triples[i])) {
                        temp.add(triples[i]); // could be multiple hits for one triple line
                        isRemove = true;
                    }
                }

                if (isRemove == true) {
                    itr.remove();
                }
            }
        }

        if (temp.size() > 0) {
            response.addAll(temp);
        }

        // compute recall:
        // of triples found by LDC (key),
        // fraction also found by system (correct = response ^ key)
        Set<String> correct = new TreeSet<String>(response);
        correct.retainAll(key);
        float recall = (float) correct.size() / (float) key.size();
        System.out.println("key = " + key.size());
        System.out.println("response = " + response.size());
        System.out.println("correct = " + correct.size());
        System.out.println("RECALL = " + recall);

        // compute precision:
        // of triples found by system (response),
        // fraction classified correct (correct)
        float precision = (float) correct.size() / (float) response.size();
        System.out.println("PRECISION = " + precision);

        // computer f1 score
        float f1 = 2 * (recall * precision) / (recall + precision);
        System.out.println("F1 = " + f1);
    }
}
