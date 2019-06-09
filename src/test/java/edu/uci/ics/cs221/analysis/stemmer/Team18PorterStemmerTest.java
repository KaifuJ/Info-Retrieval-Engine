package edu.uci.ics.cs221.analysis.stemmer;

import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.Stemmer;
import org.junit.Test;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

public class Team18PorterStemmerTest {

    public static String testStem(Stemmer stemmer, String sentence) {
        return Arrays.stream(sentence.split("\\s+"))
                .map(token -> stemmer.stem(token))
                .collect(joining(" "));
    }

    // test1 tests if plural, adjective form of nouns, passive, present-indefinite and past-tense form of verbs will be stemmed to their root form
    @Test
    public void test1() {
        String original = "clothes satisfactory wearing worn wore";
        String expected = "cloth satisfactori wear worn wore";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

    // test2 tests if proper nouns will be stemmed or not. We expected the name and some product name will stay unchanged based on porter
    // stemmer algorithms, however, some proper nouns for example, names do have a root.
    @Test
    public void test2() {
        String original = "Intellij IDEA is so popular among programmers that my friends Tom and Jerry both use it often.";
        String expected = "Intellij IDEA is so popular among programm that my friend Tom and Jerri both us it often.";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

    // test3 tests if a word is composed of two distinct words, the root form will be the adding up of the roots of the two element words or not.
    // However, the answer is no.
    @Test
    public void test3() {
        String original = "how ever however";
        String expected = "how ever howev";

        PorterStemmer porterStemmer = new PorterStemmer();
        assertEquals(expected, testStem(porterStemmer, original));
    }

}
