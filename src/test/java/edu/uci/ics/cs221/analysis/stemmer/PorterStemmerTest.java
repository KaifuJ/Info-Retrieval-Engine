package edu.uci.ics.cs221.analysis.stemmer;

import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.Stemmer;
import org.junit.Test;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

public class PorterStemmerTest {

    public static String testStem(Stemmer stemmer, String sentence) {
        return Arrays.stream(sentence.split("\\s+"))
                .map(token -> stemmer.stem(token))
                .collect(joining(" "));
    }

    @Test
    public void test1() {
        String original = "stemming is an important concept in computer science";
        String expected = "stem is an import concept in comput scienc";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

}
