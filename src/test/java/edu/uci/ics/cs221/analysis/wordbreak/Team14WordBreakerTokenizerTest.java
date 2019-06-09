package edu.uci.ics.cs221.analysis.wordbreak;

import edu.uci.ics.cs221.analysis.WordBreakTokenizer;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Team14WordBreakerTokenizerTest {


    /*
        The purpose of test one is to see if when input string can
        not be broken into tokens a runtime exception is throw per
        the last requirement of task 2. For the given input string if
        the tokenizer breaks the first token to be fra which is in the
        dictionary. There are no words in the dictionary that are lpr.
        And there are no words that have just pr or prt. So this should
        be an example where the word breaker tokenizer fails.
     */
    @Test(expected = RuntimeException.class)
    public void test1() {
        String text = "fralprtnqela";
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        tokenizer.tokenize(text);

    }
    /*
     In the second test we want to test the ability to find matches
     that are lower cased and the ability to remove the stop words.
     In order to do this we are creating a sentence that
     */
    @Test
    public void test2() {
        String text = "WEhaveaCOOLTaskinFrontOfUSANDwEShouldbehavingAgoodTIme";
        List<String> expected = Arrays.asList("cool","task","front","us","behaving","good","time");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));

    }

    @Test(expected = RuntimeException.class)
    public void test3() {
        String text = "WhatHappensWhenWeaddAperiod.";
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        tokenizer.tokenize(text);

    }

    @Test(expected = RuntimeException.class)
    public void test4() {
        String text = "This is too check if an exception is thrown when there are spaces";
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        tokenizer.tokenize(text);

    }

}