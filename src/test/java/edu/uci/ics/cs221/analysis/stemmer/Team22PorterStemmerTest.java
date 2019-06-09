package edu.uci.ics.cs221.analysis.stemmer;

import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.Stemmer;
import org.junit.Test;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

public class Team22PorterStemmerTest {
    public static String testStem(Stemmer stemmer, String sentence) {
        return Arrays.stream(sentence.split("\\s+"))
                .map(token -> stemmer.stem(token))
                .collect(joining(" "));
    }

    @Test
    public void test1() {

        // This test case test whether stemmer could stem words end with "ible", "ize" and "ion" as well as word in past tense.


        String original = "this wall is regarded as of the indestructible construction in ancient time which was built with"
                + " marble in standardized size and designed by smartest scientist at that time";

        String expected = "thi wall is regard as of the indestruct construct in ancient time which wa built with"
                + " marbl in standard size and design by smartest scientist at that time";


        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));


    }
    @Test
    public void test2() {

        // This test case test whether stemmer could stem words end with "fully", "ator", "ment", "ness" and "ing".


        String original = "hopefully the refrigerator start working again in that chen li made some adjustment with carefulness";

        String expected = "hopefulli the refriger start work again in that chen li made some adjust with care";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));


    }
    @Test
    public void test3() {

        // This test case test whether stemmer could stem words end with "ance", "ate", "y", "val" and "ism".


        String original = "the allowance of collaboration between media and tech company help activate the revival of journalism";

        String expected = "the allow of collabor between media and tech compani help activ the reviv of journal";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));


    }






}
