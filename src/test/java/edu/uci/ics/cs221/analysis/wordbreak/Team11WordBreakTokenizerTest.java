package edu.uci.ics.cs221.analysis.wordbreak;

import edu.uci.ics.cs221.analysis.WordBreakTokenizer;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Team11WordBreakTokenizerTest {

    // Test with a sentence that is only made up of stop words
    @Test
    public void test1() {
        String text = "tobeornottobe";
        List<String> expected = Arrays.asList();
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

    // Empty input test
    @Test
    public void test2() {
        String text = "";
        List<String> expected = Arrays.asList();
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

    // Input string cannot be tokenized, an UnsupportedOperationException is expected
    @Test(expected = RuntimeException.class)
    public void test3() {
        String text = "b";
        List<String> expected = Arrays.asList();
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        tokenizer.tokenize(text);
        // An exception should be thrown above, and the program should never reach the next line
        assert(false);
    }

    // Test with a normal case: it can be tokenized with the highest probability
    @Test
    public void test4() {
        String text = "searchnewtimeuse";
        List<String> expected = Arrays.asList("search", "new", "time", "use");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        
        assertEquals(expected, tokenizer.tokenize(text));
    }

    // Test with a few upper cases
    @Test
    public void test5() {
        String text = "seaRchneWtiMeuSe";
        List<String> expected = Arrays.asList("search", "new", "time", "use");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        
        assertEquals(expected, tokenizer.tokenize(text));
    }

    // Test with all upper cases
    @Test
    public void test6() {
        String text = "SEARCHNEWTIMEUSE";
        List<String> expected = Arrays.asList("search", "new", "time", "use");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        
        assertEquals(expected, tokenizer.tokenize(text));
    }

    // Test with stop word at the beginning
    @Test
    public void test7() {
        String text = "thesearchnewtimeuse";
        List<String> expected = Arrays.asList("search", "new", "time", "use");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        
        assertEquals(expected, tokenizer.tokenize(text));
    }

    // Test with stop word in the middle
    @Test
    public void test8() {
        String text = "searchthenewtimeuse";
        List<String> expected = Arrays.asList("search", "new", "time", "use");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        
        assertEquals(expected, tokenizer.tokenize(text));
    }

    // Test with stop word in the end
    @Test
    public void test9() {
        String text = "searchnewtimeusethe";
        List<String> expected = Arrays.asList("search", "new", "time", "use");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        
        assertEquals(expected, tokenizer.tokenize(text));
    }
}
