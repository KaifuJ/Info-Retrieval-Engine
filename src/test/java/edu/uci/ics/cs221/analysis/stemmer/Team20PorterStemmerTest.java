package edu.uci.ics.cs221.analysis.stemmer;

import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.Stemmer;
import org.junit.Test;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

public class Team20PorterStemmerTest {

    public static String testStem(Stemmer stemmer, String sentence) {
        return Arrays.stream(sentence.split("\\s+"))
                .map(token -> stemmer.stem(token))
                .collect(joining(" "));
    }

    @Test
    public void test_words_which_shouldnt_be_modified() {
        /*
         Test words that should not be modified/stemmed by the stemmer
         as they are already in their "root" forms.
         */

        String original = "rate roll sky feed bled sing caress 1234";
        String expected = "rate roll sky feed bled sing caress 1234";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

    @Test
    public void test_plurals() {
        /*
         Test plural words.
         Check whether words in their plural forms are converted into their singular forms by the stemmer.
         */

        String original = "caresses ponies cats";
        String expected = "caress poni cat";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

    @Test
    public void test_words_with_diff_suffix() {
        /*
         Test words with different suffixes.
         Check if words with different forms of suffixes are converted to their root forms correctly.
         */

        String original = "plastered probate relational goodness hopeful feudalism motoring differently formality " +
                "defensible adjustment bowdlerize adoption operator homologous irritant";
        String expected = "plaster probat relat good hope feudal motor differ formal defens adjust bowdler adopt " +
                "oper homolog irrit";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

    @Test
    public void test_empty_string() {
        /*
         Test an empty string as input; an empty string should be returned
         */

        String original = "";
        String expected = "";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

    @Test
    public void test_sentence() {
        /*
         Test a generic sentence with different types of words.
         */
        String original = "Indeed, my only wonder was that he had not already been mixed up in this extraordinary case," +
                " which was the one topic of conversation through the length and breadth of England.";
        String expected = "Indeed, my onli wonder wa that he had not alreadi been mix up in thi extraordinari case," +
                " which wa the on topic of convers through the length and breadth of England.";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

}
