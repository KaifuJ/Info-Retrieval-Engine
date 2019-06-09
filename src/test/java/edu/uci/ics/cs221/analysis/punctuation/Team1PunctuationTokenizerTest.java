package edu.uci.ics.cs221.analysis.punctuation;

import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.analysis.StopWords;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for punctuation tokenizer by team 1.
 *
 * @author Zixu Wang
 */
public class Team1PunctuationTokenizerTest {

    /**
     * Test whether the punctuation tokenizer handles white spaces correctly.
     *
     * Note: a whitespace character is assumed to be one of:
     *   1. ' ' (normal space, Unicode codepoint U+0020, ASCII code 32);
     *   2. '\t' (horizontal tab, Unicode codepoint U+0009, ASCII code 9);
     *   3. '\n' (line feed, Unicode codepoint U+000A, ASCII code 10).
     * Other space-like characters or Unicode whitespace characters are not
     * considered, unless the project specification further indicates otherwise.
     *
     * Test text:       {@code "uci cs221\tinformation\nretrieval"}
     * Expected tokens: {@code ["uci", "cs221", "information", "retrieval"]}
     */
    @Test
    public void whiteSpacesShouldDelimit() {
        String text = "uci cs221\tinformation\nretrieval";
        List<String> expected = Arrays.asList("uci", "cs221", "information", "retrieval");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

    /**
     * Test whether the punctuation tokenizer handles punctuations correctly.
     *
     * Note: a punctuation delimiter is assumed to be one of ',', '.', ';', '?', and '!',
     * @see PunctuationTokenizer#punctuations
     *
     * Test text:       {@code "uci,cs221.information;retrieval?project!1"}
     * Expected tokens: {@code ["uci", "cs221", "information", "retrieval", "project", "1"]}
     */
    @Test
    public void punctuationsShouldDelimit() {
        String text = "uci,cs221.information;retrieval?project!1";
        List<String> expected =
                Arrays.asList("uci", "cs221", "information", "retrieval", "project", "1");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

    /**
     * Test whether the punctuation tokenizer handles non-delimiter characters correctly.
     *
     * Test text:       {@code "uci~cs221/information>retrieval"}
     * Expected tokens: {@code ["uci~cs221/information>retrieval"]}
     */
    @Test
    public void nonDelimiterShouldNotDelimit() {
        String text = "uci~cs221/information>retrieval";
        List<String> expected = Arrays.asList("uci~cs221/information>retrieval");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

    /**
     * Test whether the punctuation tokenizer converts tokens into lower cases.
     *
     * Test text:       {@code "UciCS221InformationRetrieval"}
     * Expected tokens: {@code ["ucics221informationretrieval"]}
     */
    @Test
    public void tokensShouldBeLowerCase() {
        String text = "UciCS221InformationRetrieval";
        List<String> expected = Arrays.asList("ucics221informationretrieval");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

    /**
     * Test whether the punctuation tokenizer correctly handles consecutive whitespace
     * characters.
     *
     * Test text:       {@code "uci \tcs221\t\ninformation\n \tretrieval"}
     * Expected tokens: {@code ["uci", "cs221", "information", "retrieval"]}
     */
    @Test
    public void consecutiveWhiteSpacesShouldDelimit() {
        String text = "uci \tcs221\t\ninformation\n \tretrieval";
        List<String> expected = Arrays.asList("uci", "cs221", "information", "retrieval");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

    /**
     * Test whether the punctuation tokenizer correctly handles consecutive punctuations.
     *
     * Test text:       {@code "uci,.cs221.;information;?retrieval?!project!,.1"}
     * Expected tokens: {@code ["uci", "cs221", "information", "retrieval", "project", "1"]}
     */
    @Test
    public void consecutivePunctuationsShouldDelimit() {
        String text = "uci,.cs221.;information;?retrieval?!project!,.1";
        List<String> expected =
                Arrays.asList("uci", "cs221", "information", "retrieval", "project", "1");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

    /**
     * Test whether the punctuation tokenizer correctly handles leading and trailing
     * whitespace characters.
     *
     * Test text:       {@code " \t\nucics221informationretrieval \t\n"}
     * Expected tokens: {@code ["ucics221informationretrieval"]}
     */
    @Test
    public void surroundingWhiteSpacesShouldBeRemoved() {
        String text = " \t\nucics221informationretrieval \t\n";
        List<String> expected = Arrays.asList("ucics221informationretrieval");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

    /**
     * Test whether the punctuation tokenizer correctly handles leading and trailing
     * punctuations.
     *
     * Test text:       {@code ",.;?!ucics221informationretrieval,.;?!"}
     * Expected tokens: {@code ["ucics221informationretrieval"]}
     */
    @Test
    public void surroundingPunctuationsShouldBeRemoved() {
        String text = ",.;?!ucics221informationretrieval,.;?!";
        List<String> expected = Arrays.asList("ucics221informationretrieval");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

    /**
     * Test whether the punctuation tokenizer correctly handles stop words.
     *
     * @see StopWords#stopWords
     */
    @Test
    public void stopWordsShouldBeRemoved() {
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        List<String> expected = Arrays.asList();

        for (String stopWord: StopWords.stopWords) {
            assertEquals(expected, tokenizer.tokenize(stopWord));
        }
    }

    /**
     * Integrated test case to check whether the punctuation tokenizer jointly
     * satisfies all requirements tested above.
     *
     * Test text:       {@code " Do UCI CS221:\tInformation Retrieval, project 1 by yourself.\n"}
     * Expected tokens: {@code ["uci", "cs221:", "information", "retrieval", "project", "1"]}
     */
    @Test
    public void integrationTest() {
        String text = " Do UCI CS221:\tInformation Retrieval, project 1 by yourself.\n";
        List<String> expected = Arrays.asList(
                "uci",
                "cs221:",
                "information",
                "retrieval",
                "project",
                "1"
        );
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }
}
