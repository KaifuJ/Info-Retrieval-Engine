package edu.uci.ics.cs221.analysis.punctuation;

import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class Team3PunctuationTokenizerTest {
    //This is the first testcase, we use a List of String to store the expected outcomes
    //and use "assertEquals" to check if the function "tokenize" will provide the same outcomes
    //as what we expected.
    @Test
    public void team3TestCase1(){
        String text = "Good morning, Sara!";
        List<String> expected = Arrays.asList("good", "morning","sara");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }
    //Our testcase 2
    @Test
    public void team3TestCase2(){
        String text = "Information Retrival is      the best course in UCI!";
        List<String> expected = Arrays.asList("information", "retrival","best","course","uci");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }
    @Test
    public void team3TestCase3(){
        String text = "Information Retrival is \t \n the best course in UCI!";
        List<String> expected = Arrays.asList("information", "retrival","best","course","uci");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }
}