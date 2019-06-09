package edu.uci.ics.cs221.analysis.punctuation;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/*
- What's your team number?
Team 6

- What is the functionality being tested?
Punctuation Tokenizer

- Describe your tests briefly:
Test Case 1 is to test if the tokenizer handles different white spaces correctly by having characters of \t,
\n and white spaces in the input text.
Test Case 2 is to test if the tokenizer can handles splits the text by given punctuations marks and removes them correctly.
The punctuations include ",", ".", ";", "?" and "!". Moreover, punctuation marks that are not on the list should not be
considered, such as i'am and four-year-old.
Test Case 3 is to test if the tokenizer can convert all tokens into lower case.
Test Case 4 is to test if the tokenizer can filter out the stop words.

- Does each test case have comments/documentation?
Yes


 */

public class Team6PunctuationTokenizerTest {

    @Test
    public void test1() {
        /*
        Test Case 1 is to test if the tokenizer handles different
        white spaces correctly by having characters of \t, \n and white spaces in the input text.
        */
//        System.out.printf("test case 1\n");

        String text = " testcase\tgood example\nyes great example\n";
        List<String> expected = Arrays.asList("testcase", "good", "example",
               "yes", "great", "example");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test2() {
        /*
        Test Case 2 is to test if the tokenizer can handles splits the text by given punctuations marks
        and removes them correctly. The punctuations include ",", ".", ";", "?" and "!". Moreover,
        punctuation marks that are not on the list should not be considered, such as i'am and four-year-old.
         */
//        System.out.println("test case 2\n");

        String text = "Word LOL means Laughing. WHO";
        List<String> expected = Arrays.asList("word", "lol", "means", "laughing");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test3() {
        /*
        Test Case 3 is to test if the tokenizer can convert all tokens into lower case.
        */
//        System.out.println("test case 3\n");

        String text = "good, four-year-old children. never asia come? it's china! thanks.";
        List<String> expected = Arrays.asList("good", "four-year-old", "children", "never", "asia",
                "come", "it's", "china", "thanks");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test4() {
        /*
        Test Case 4 is to test if the tokenizer can filter out the stop words.
         */
//        System.out.println("test case 4\n");

        String text = "I cannot decide which car I like best " +
                "the Ferrari, with its quick acceleration and " +
                "sporty look; the midsize Ford Taurus, with " +
                "its comfortable seats and ease of handling; " +
                "or the compact Geo, with its economical fuel consumption.";
        List<String> expected = Arrays.asList("cannot", "decide", "car", "like", "best",
                "ferrari", "quick", "acceleration", "sporty", "look", "midsize", "ford",
                "taurus", "comfortable", "seats", "ease", "handling", "compact", "geo", "economical",
                "fuel", "consumption");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }
}
