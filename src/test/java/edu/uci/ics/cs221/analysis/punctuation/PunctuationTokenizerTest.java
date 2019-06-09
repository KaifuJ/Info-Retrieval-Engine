package edu.uci.ics.cs221.analysis.punctuation;

import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PunctuationTokenizerTest {

    @Test
    public void test1() {
        String text = "I am Happy Today!";
        List<String> expected = Arrays.asList("happy", "today");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

}
