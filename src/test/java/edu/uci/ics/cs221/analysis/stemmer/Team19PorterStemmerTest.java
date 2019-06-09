package edu.uci.ics.cs221.analysis.stemmer;

import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.Stemmer;
import org.junit.Test;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

public class Team19PorterStemmerTest {

    public static String testStem(Stemmer stemmer, String sentence) {
        return Arrays.stream(sentence.split("\\s+"))
                .map(token -> stemmer.stem(token))
                .collect(joining(" "));
    }

    // Changing the words "writing", "Turning", "results", "applications", and "this" to their roots.
    // To test if the Stemmer can change words with added suffix and complex pattern word to their roots.
    @Test
    public void test1() {
        String original = "I am writing to test the Stemmer. Turning in the final results of the applications is due this week";
        String expected = "I am write to test the Stemmer. Turn in the final result of the applic is due thi week";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

    // Changing the words "information", "retrieval", "activity", "obtaining",  "information", "resources", "relevant", and "collection".
    // To test if the Stemmer can turn complex words to their roots.
    @Test
    public void test2() {
        String original = "information retrieval is the activity of obtaining information system resources relevant to an information need from a collection";
        String expected = "inform retriev is the activ of obtain inform system resourc relev to an inform need from a collect";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

    // Turning the words "fished" and "weeks" to their roots.
    // To test if the Stemmer can turn words with added suffix to their roots.
    @Test
    public void test3() {
        String original = "He is an old man who fished alone in a skiff in the Gulf Stream and he had gone twenty-two weeks without taking a fish";
        String expected = "He is an old man who fish alon in a skiff in the Gulf Stream and he had gone twenty-two week without take a fish";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

}
