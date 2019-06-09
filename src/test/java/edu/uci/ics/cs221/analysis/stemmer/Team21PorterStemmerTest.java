package edu.uci.ics.cs221.analysis.stemmer;

import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.Stemmer;
import org.junit.Test;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

public class Team21PorterStemmerTest {
    public static String testStem(Stemmer stemmer, String sentence) {
        return Arrays.stream(sentence.split("\\s+"))
                .map(token -> stemmer.stem(token))
                .collect(joining(" "));
    }

    @Test
    public void test1() {
        /*
        test plurals and -ed or -ing.
         */

        String original = "ties dogs caress need agreed disabled fitting making missing meeting meetings";
        String expected = "ti dog caress need agre disabl fit make miss meet meet";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

    @Test
    public void test2() {
        /*
        test of taking off -ization, -izer, -tional, -ibility, -ness.
         */

        String original = "organization organizer international responsibility fitness";
        String expected = "organ organ intern respons fit";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

    @Test
    public void test3() {
        /*
        test of taking off -ment, -ness, -ence, -fulness, -ical, -ism.
         */

        String original = "department humorousness dependence helpfulness analytical despotism";
        String expected = "depart humor depend help analyt despot";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }
}
