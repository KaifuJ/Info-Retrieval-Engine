package edu.uci.ics.cs221.analysis.wordbreak;

import edu.uci.ics.cs221.analysis.WordBreakTokenizer;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Team17WordBreakTokenizerTest {
    /*
     * The test1() is to check that the "san francisco" is tokenized as one single token instead of two separate tokens.
     * If there are two separate tokens "san" and "francisco" then the meaning of the input string changes drastically.
     * Additionally, the upper case characters must be converted to lower case.
     * */
    @Test
    public void test1() {
        String text = "IlOveSAnFrancIsCo";
        List<String> expected = Arrays.asList("love", "san", "francisco");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));

    }

    /*
     * The test2() has only the stopwords.
     * Hence the output must be an empty array and should not throw any exception as the string can broken into
     * words that exist in the dictionary.
     * Additionally, this testcase also tests the program to handle a really long string.
     * */

    @Test
    public void test2() {
        String text = "imemymyselfweouroursourselvesyouyouryoursyourselfyourselveshehimhishimselfsheherhersherselfititsitselftheythemtheirtheirsthemselveswhatwhichwhowhomthisthatthesethoseamisarewaswerebebeenbeinghavehashadhavingdodoesdiddoing";
        List<String> expected = Arrays.asList();
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));

    }

    /*
     * The test3() has an empty string
     * Hence the wordbreak tokenizer must return empty set of tokens
     * */

    @Test
    public void test3() {
        String text = "";
        List<String> expected = Arrays.asList();
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }


    /*
     * The test4() has string with punctuations
     * Hence the wordbreak tokenizer must throw an exception
     * */

    @Test(expected = RuntimeException.class)
    public void test4() {
        String text = "mother-in-law";
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        tokenizer.tokenize(text);
    }

    /*
     * The test5() has two possible set of tokens
     * But the wordbreak tokenizer must come up with the most optimal one that is ["hello", "range"] instead of ["hell", "orange"]
     * */

    @Test
    public void test5() {
        String text = "hellorange";
        List<String> expected = Arrays.asList("hello","range");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text)); //must expect an exception

    }
}

