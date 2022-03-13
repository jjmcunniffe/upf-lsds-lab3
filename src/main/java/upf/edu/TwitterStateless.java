package upf.edu;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.twitter.TwitterUtils;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;
import twitter4j.Status;
import twitter4j.auth.OAuthAuthorization;
import upf.edu.util.ConfigUtils;
import upf.edu.util.LanguageMapUtils;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

public class TwitterStateless {
    public static void main(String[] args) throws IOException, InterruptedException {
        String propertiesFile = args[0];
        String input = args[1];
        OAuthAuthorization auth = ConfigUtils.getAuthorizationFromFileProperties(propertiesFile);

        SparkConf conf = new SparkConf().setAppName("Real-time Twitter Stateless Exercise");
        JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(30));
        jsc.checkpoint("/tmp/checkpoint");

        final JavaReceiverInputDStream<Status> stream = TwitterUtils.createStream(jsc, auth);

        // Read the language map tsv and transform it to a static RDD.
        final JavaRDD<String> languageMapLines = jsc
                .sparkContext()
                .textFile(input);

        final JavaPairRDD<String, String> languageMap = LanguageMapUtils
                .buildLanguageMap(languageMapLines);

        // Grab the different languages.
        JavaDStream<String> languages = stream.flatMap(new FlatMapFunction<Status, String>() {
            @Override
            public Iterator<String> call(Status s) {
                return Arrays.asList(s.getLang()).iterator();
            }
        });

        JavaPairDStream<String, Integer> pair = languages
                .mapToPair(s -> new Tuple2<>(s, 1))
                .reduceByKey((a, b) -> a + b);

        /** TODO: Exchange language code for full language name. **/
        // Use the full language name by leveraging the language map RDD.
        JavaPairDStream<String, Integer> formattedPair = pair;
                /*
                .transformToPair(
                        new Function<JavaPairRDD<String, Integer>, JavaPairRDD<String, Integer>>() {
                            @Override
                            public JavaPairRDD<String, Integer> call(JavaPairRDD<String, Integer> pairRDD) throws Exception {
                                return pairRDD.join(languageMap); // This isn't right...
                            }
                        }
                );
                */
        /** end TODO **/

        // Sort the pairs in decending order.
        JavaPairDStream<Integer, String> swappedPair = formattedPair.mapToPair(Tuple2::swap); // We swap before.
        JavaPairDStream<Integer, String> sortedPair = swappedPair
                .transformToPair(
                    new Function<JavaPairRDD<Integer, String>, JavaPairRDD<Integer, String>>() {
                        @Override
                        public JavaPairRDD<Integer, String> call(JavaPairRDD<Integer, String> pairRDD) throws Exception {
                            return pairRDD.sortByKey(false);
                        }
                    }
                );
        JavaPairDStream<String, Integer> languageRankStream = sortedPair.mapToPair(Tuple2::swap); // We swap after.

        // Output the top ten language pairs.
        languageRankStream.print(10);

        // Start the application and wait for termination signal
        jsc.start();
        jsc.awaitTermination();
    }
}
