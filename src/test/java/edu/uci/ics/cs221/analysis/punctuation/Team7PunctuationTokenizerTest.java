package edu.uci.ics.cs221.analysis.punctuation;

import edu.uci.ics.cs221.analysis.PunctuationTokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Test;

public class Team7PunctuationTokenizerTest {


    /*
        Test 1:
            Check if the tokenizer can tokenize the input string with empty space and punctuations, and
            convert the tokens to lower case.
     */
    @Test
    public void team7test01(){
        String input = " HELLO,WORLD ! ";
        List<String> output = Arrays.asList("hello","world");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(output, tokenizer.tokenize(input));
    }

    /*
        Test 2:
            Check if the tokenizer can deal with the empty string that only contains the space, table, and
            new line mark.
     */
    @Test
    public void team7test02(){
        String input = "\n \t";
        List<String> output = new ArrayList<>();
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(output, tokenizer.tokenize(input));
    }
}
