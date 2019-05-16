package edu.uci.ics.cs221.analysis.wordbreak;

import edu.uci.ics.cs221.analysis.WordBreakTokenizer;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Team12WordBreakTokenizer {
    //below test tests removing stop words
    @Test
    public void test1() {
        String text = "thelordofthering";
        List<String> expected = Arrays.asList("lord", "ring");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));

    }
    //this test case checks if word is capitalized, should be lowered and also checks removal of stop
    //words as well as returning peanut butter instead of pea, nut, but, ter which are also in the
    //dictionary
    @Test
    public void test2()
    {
        String text = "IWANTtohavepeanutbuttersandwich";
        List<String> expected = Arrays.asList("want", "peanut", "butter", "sandwich");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }
    //this test checks that when text has a word not in dictionary and has spaces and question mark,
    //then it should throw an exception
    @Test(expected = RuntimeException.class)
    public void test3()
    {
        String text = "Where did Ghada go?";
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        tokenizer.tokenize(text);
    }
}
