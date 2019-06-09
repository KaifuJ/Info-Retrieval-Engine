package edu.uci.ics.cs221.analysis.wordbreak;

import edu.uci.ics.cs221.analysis.WordBreakTokenizer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Team9WordBreakTokenizerTest {

    // Test1: test for empty input, should return an empty list
    @Test
    public void test1() {
        String text = "";
        List<String> expected = Arrays.asList();
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

    // Test2: Test for upper case input
    @Test
    public void test2() {
        String text = "ILIKEINFORMATIONRETRIEVAL";
        List<String> expected = Arrays.asList("like", "information", "retrieval");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

    // Test3: for the case which has multiple answers
    // BAD: the rear eel even pine apples
    // BAD: there are eleven pine apples
    // GOOD: there are eleven pineapples
    @Test
    public void test3() {
        String text = "thereareelevenpineapples";
        List<String> expected = Arrays.asList("eleven", "pineapples");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

    // Test4: invalid input, expect an exception.
    // exception type depends on the implementation.
    @Test(expected = RuntimeException.class)
    public void test4() {
        String text = "abc123";
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        tokenizer.tokenize(text);
    }

}
