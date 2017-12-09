import java.io.*;
import java.util.*;

// if p:RESIDE:c and c:REGIONOF:d then p:RESIDE:d

public class Infer {

    static List<String[]> triples = new ArrayList<String[]>();
    static Map<String, String> regionMap = new HashMap<String, String>();

    public static void main (String[] args) throws IOException {
        String inFileName = args[0];
        String outFileName =args[1];
        BufferedReader reader = new BufferedReader(new FileReader (inFileName));
        PrintWriter writer = new PrintWriter(new FileWriter (outFileName));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] field = line.split(":");
            triples.add(field);
            if (field[1].equals("REGIONOF")) {
                regionMap.put(field[0], field[2]);
            } else if (field[1].equals("RESIDE")) {
                writer.println(line);
            }
        }

        for (String[] triple : triples) {
            if (triple[1].equals("RESIDE")) {
                String person = triple[0];
                String gpe = triple[2];
                if (regionMap.get(gpe) != null) {
                    writer.println (person + ":RESIDE:" + regionMap.get(gpe));
                    System.out.println (person + ":RESIDE:" + regionMap.get(gpe));
                }
            }
        }

        writer.close();
    }

}
