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
import scala.Tuple2;
import twitter4j.Status;
import twitter4j.auth.OAuthAuthorization;
import upf.edu.util.ConfigUtils;
import upf.edu.util.LanguageMapUtils;

import java.io.IOException;
import java.util.Arrays;

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

        // Count the number of times a language code is present.
        JavaPairDStream<String, Integer> languageRankStream = stream
                .flatMap(s -> Arrays.asList(s.getLang()).iterator())
                .mapToPair(s -> new Tuple2<>(s, 1))
                .reduceByKey((a, b) -> a + b)
                // Join language map.
                .transformToPair(s -> s.join(languageMap))
                .mapToPair(s -> s._2()) // We want the second tuple.
                // Sort by decending order and swap.
                .transformToPair(s -> s.sortByKey(false))
                .mapToPair(Tuple2::swap);

        // Output the top ten language pairs.
        languageRankStream.print(10);

        // Start the application and wait for termination signal
        jsc.start();
        jsc.awaitTermination();
    }
}
