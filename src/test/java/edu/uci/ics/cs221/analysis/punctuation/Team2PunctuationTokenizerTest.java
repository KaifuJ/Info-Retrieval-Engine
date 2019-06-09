package edu.uci.ics.cs221.analysis.punctuation;

import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import org.junit.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class Team2PunctuationTokenizerTest {

    /**
     *  test1 tests whether multiple newlines, tabs,together with spaces works
     */
    @Test
    public void test1() {
        String text = "UCI: \n \n a public research university located in Irvine, \t \t California!";
        List<String> expected = Arrays.asList("uci:", "public", "research", "university", "located","irvine","california");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals("check whether multiple newlines and tabs works",expected, tokenizer.tokenize(text));
    }

    /**
     *  test2 tests whether the punctuation tokenizer can identify some emojis and save them as a token
     *  test string: 🐴 is a very cute horse head!
     */
    @Test
    public void test2() {
        String text = "\uD83D\uDC34 is a very cute horse head!";
        List<String> expected = Arrays.asList("\uD83D\uDC34", "cute", "horse", "head");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals("check whether the tokenizer can deal with emojis" ,expected, tokenizer.tokenize(text));
    }

    /**
     *  test3: this test tests whether the punctuation tokenizer can deal with consecutive punctuations
     */
    @Test
    public void test3() {
        String text = "UCI : \na, public research university located in Irvine,California!!!!";
        List<String> expected = Arrays.asList("uci", ":", "public", "research", "university", "located","irvine","california");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals("check consecutive punctuation works",expected, tokenizer.tokenize(text));
    }
}
