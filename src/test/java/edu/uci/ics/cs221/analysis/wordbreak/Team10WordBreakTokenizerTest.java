package edu.uci.ics.cs221.analysis.wordbreak;

import edu.uci.ics.cs221.analysis.WordBreakTokenizer;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Team10WordBreakTokenizerTest {

    @Test
    public void test1() {
        String text = "Itisnotourgoal";
        // Original: "It is not our goal"
        // we didn't use "It's" because "it's" is not in the provided dictionary.
        // It's easy to be broken into "it is no tour goal", which is false.

        List<String> expected = Arrays.asList("goal");
        // "it" "is" "not" "our" are all stop words, which should be discarded.
        // False: {"tour", "goal"}

        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test2() {
        String text = "FindthelongestpalindromicstringYoumayassumethatthemaximumlengthisonehundred";
        // Original: "Find the longest palindromic string. You may assume that the maximum length is one hundred."
        // Test if the WordBreaker is efficient enough to handle long complex strings correctly.

        List<String> expected = Arrays.asList("find", "longest", "palindromic", "string", "may",
                "assume", "maximum", "length", "one", "hundred");

        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

}
