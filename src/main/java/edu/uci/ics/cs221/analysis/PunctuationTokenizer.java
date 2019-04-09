package edu.uci.ics.cs221.analysis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.*;

/**
 * Project 1, task 1: Implement a simple tokenizer based on punctuations and white spaces.
 *
 * For example: the text "I am Happy Today!" should be tokenized to ["happy", "today"].
 *
 * Requirements:
 *  - White spaces (space, tab, newline, etc..) and punctuations provided below should be used to tokenize the text.
 *  - White spaces and punctuations should be removed from the result tokens.
 *  - All tokens should be converted to lower case.
 *  - Stop words should be filtered out. Use the stop word list provided in `StopWords.java`
 *
 */
public class PunctuationTokenizer implements Tokenizer {

    public static Set<String> punctuations = new HashSet<>();
    static {
        punctuations.addAll(Arrays.asList(",", ".", ";", "?", "!"));
    }

    public PunctuationTokenizer() {}

    public List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        char[] words = text.toCharArray();
        for(int i = 0; i < words.length; i++){
            if(punctuations.contains(String.valueOf(words[i]))) words[i]=' ';
        }
        String tmp = new String(words);
        String [] arr = tmp.split("\\s+");
        for(String ss : arr){
            String ls =ss.toLowerCase();
            if(!StopWords.stopWords.contains(ls)) tokens.add(ls);
        }
        return tokens;
    }

}
