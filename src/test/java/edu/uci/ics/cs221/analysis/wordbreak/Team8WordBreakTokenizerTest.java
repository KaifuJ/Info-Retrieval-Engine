package edu.uci.ics.cs221.analysis.wordbreak;

import edu.uci.ics.cs221.analysis.WordBreakTokenizer;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class Team8WordBreakTokenizerTest {


	//check if the output is lowercased and stop words are removed
    @Test
    public void test1() {
        String text = "THISiswhATItoldyourI'llFRIendandI'llgoonlinecontactcan'tforget";
        List<String> expected = Arrays.asList("old", "i'll", "friend", "i'll","go","online","contact","can't","forget");

        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));

    }

    //check whether the work-break functions when meet strings like "whatevergreen" and the string constists of more than one frequenctly used word
    @Test
    public void test2(){
    	String text = "informationinforTHOUGHTFULLYcopyrightwhatevercontactablewhatevergreen";
    	List<String> expected = Arrays.asList("information", "thoughtfully", "copyright", "whatever", "contact", "able","whatever", "green" );
    	
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }


    //check if the program can throw an exception when the string is unbreakable
    @Test(expected = RuntimeException.class)
    public void test3(){
    	String text = "$reLLL(  ghn)iog*";
        //throw exception
    	WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        tokenizer.tokenize(text);

    }

}
