package edu.uci.ics.cs221.analysis;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static edu.uci.ics.cs221.analysis.StopWords.stopWords;

/**
 * Project 1, task 2: Implement a Dynamic-Programming based Word-Break Tokenizer.
 *
 * Word-break is a problem where given a dictionary and a string (text with all white spaces removed),
 * determine how to break the string into sequence of words.
 * For example:
 * input string "catanddog" is broken to tokens ["cat", "and", "dog"]
 *
 * We provide an English dictionary corpus with frequency information in "resources/cs221_frequency_dictionary_en.txt".
 * Use frequency statistics to choose the optimal way when there are many alternatives to break a string.
 * For example,
 * input string is "ai",
 * dictionary and probability is: "a": 0.1, "i": 0.1, and "ai": "0.05".
 *
 * Alternative 1: ["a", "i"], with probability p("a") * p("i") = 0.01
 * Alternative 2: ["ai"], with probability p("ai") = 0.05
 * Finally, ["ai"] is chosen as result because it has higher probability.
 *
 * Requirements:
 *  - Use Dynamic Programming for efficiency purposes.
 *  - Use the the given dictionary corpus and frequency statistics to determine optimal alternative.
 *      The probability is calculated as the product of each token's probability, assuming the tokens are independent.
 *  - A match in dictionary is case insensitive. Output tokens should all be in lower case.
 *  - Stop words should be removed.
 *  - If there's no possible way to break the string, throw an exception.
 *
 */

public class WordBreakTokenizer implements Tokenizer {
    private Map<String, Double> dict;

    public WordBreakTokenizer() {
        this.dict = new HashMap<>();

        try {
            // load the dictionary corpus
            URL dictResource = WordBreakTokenizer.class.getClassLoader().getResource("cs221_frequency_dictionary_en.txt");
            // dictLines contains all lines in dict file
            List<String> dictLines = Files.readAllLines(Paths.get(dictResource.toURI()));

            // initiate dict with words and possibilities
            for(String line : dictLines){
                String[] entry = line.split(" ");
                dict.put(entry[0], Double.parseDouble(entry[2]));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> tokenize(String text) {
        text = text.toLowerCase();

        char[] str = text.toCharArray();
        List<String> res = new LinkedList<>();
        double[] maxPos = new double[1]; // maxPos[0] automatically initiated to 0.0
        List<String> path = new ArrayList<>(); // when "path" is given to "res", it will be transformed to LinkedList.


        // if the string is not dividable, then "maxPos" will not be touched.
        // if the string is dividable, then "curPos" will multiply at least one possibility.
        // so we can safely give "curPos" initial value 1.0
        tokenize(str, this.dict, res, maxPos, path, 1.0, 0);

        if(maxPos[0] == 0.0){ // not dividable  // when the input is an empty string, the final maxPos is 1.0, and is considered as dividable.
//            throw new UnsupportedOperationException("Porter Stemmer Unimplemented");
            throw new UnsupportedOperationException("Text Not Breakable");
        }else{
            removeStopWords(res, stopWords);
            return res;
        }

    }


    private void tokenize(char[] str, Map<String, Double> dict, List<String> res, double[] maxPos,
                          List<String> path, double curPos, int start){
        // when the remaining str is not dividable,
        // the "start" var will never reach str.length,
        // so the "maxPos" and "res" will never be modified.

        // when start == str.length
        // it means we have reached the end
        // it means we have successfully divided the string
        if(start == str.length){
            if(curPos > maxPos[0]){
                maxPos[0] = curPos;

                // we need to remove stop words from "res" later
                // LinkedList give better performance for deleting.
                res.clear();
                res.addAll(path);
            }
            return;
        }

        for(int i = start; i < str.length; i++){
            String substr = new String(str, start, i - start + 1);

            if(dict.containsKey(substr)){ // we can cut the string here
                path.add(substr);
                tokenize(str, dict, res, maxPos, path, curPos * dict.get(substr), i + 1);
                path.remove(path.size() - 1);
            }
        }
    }

    private void removeStopWords(List<String> tokens, Set<String> stopWords){
        for(int i = 0; i < tokens.size(); i++){
            if(stopWords.contains(tokens.get(i))){
                tokens.remove(i);
                i--;
            }
        }
    }


}

//class main{
//    public static void main(String[] args){
//        WordBreakTokenizer wbt = new WordBreakTokenizer();
//
//        List<String> res = wbt.tokenize("itisnotourgoal");
//
//        for(String s : res){
//            System.out.print(s + " ");
//        }
//
//    }
//}











