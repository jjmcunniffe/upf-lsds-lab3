package upf.edu.util;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Objects;

public class LanguageMapUtils {

    private static PairFunction<String, String, String> parseTSV = (line) -> {
        String[] fields = line.split("\t"); // Split by tab (.tsv).

        // We only want fields [1] and [2]. We can discard [0] and [3].
        // We must further sanitise fields [1] and [2] to ensure no blank country codes.
        Tuple2 pair = null;
        if (fields[1].length() == 2 ) {
            // Remove language dialects from output.
            int cut = fields[2].indexOf(";");
            if (cut != -1) {
                fields[2] = fields[2].substring(0, cut);
            }

            // Pair is valid; therefore, not null.
            pair = new Tuple2<String, String>(fields[1], fields[2]);
        }
        return pair;
    };

    public static JavaPairRDD<String, String> buildLanguageMap(JavaRDD<String> lines) {
        // Remove the header.
        String header = lines.first();
        JavaRDD<String> filteredLines = lines.filter(line -> !line.equals(header));

        // Construct our pair with ISO 639-1 Code and English name.
        JavaPairRDD<String, String> languages = filteredLines
                .mapToPair(parseTSV)
                .filter(Objects::nonNull); // Our parseTSV places null if the pair is invalid.

        /* For testing.
        languages.foreach(data -> {
            System.out.println(data);
        });
        */

        return languages;
    }
}
