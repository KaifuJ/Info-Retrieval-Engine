package edu.uci.ics.cs221.index.positional;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.Compressor;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Team14PhraseSearchTest {
    InvertedIndexManager index;
    Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
    Compressor compressor = new DeltaVarLenCompressor();
    String path = "./index/Team14PhraseSearchTest/";

    Document[] documents1 = new Document[] { new Document("This morning I ate eggs"),
            new Document("Abstraction is often one floor above you."),
            new Document("Everyone was busy, so I went to the movie alone."),
            new Document("Please wait outside of the house."),
            new Document("Wednesday is hump day, but has anyone asked the camel if he’s happy about it?"),
            new Document("He told us a very exciting adventure story."),
            new Document("My Mom tries to be cool by saying that she likes all the same things that I do."),
            new Document("She advised him to come back at once."), new Document(
            "She works two jobs to make ends meet; at least, that was her reason for not having time to join us."),
            new Document("How was the math test?"), new Document("Eggs come from chickens."),
            new Document("Abstraction is used in this project."),
            new Document("Everyone was busy with math homework, and so I went out alone"),
            new Document("The job we did alone at the house of my boss and we weren't happy about it."),
            new Document("Camels are the horses of the middle east. "),
            new Document("Once upon a time the egg came from the camel and became a project."),
            new Document("At the end we had a chance to meet at the movie, but weren't thinking the same thing."),
            new Document("Math is like an egg the hard it is the better it is."),
            new Document("Jobs seem like a waste of time if you aren't happy"),
            new Document("My mom has a job that is like an adventure every day."),
            new Document("The weather outside was too cool for the camel."),
            new Document("Wednesday is the day that our chicken produces a lot of eggs."),
            new Document("Two jobs to make ends meet, means we need to less eggs."),
            new Document("As a camel do you have one or two humps?"),
            new Document("I hate going to the movie alone."),
            new Document("the movie alone."),
            new Document("You went to the  movie alone.")};

    Document[] documents2 = new Document[] { new Document("Hello"), new Document("I like to eat pineapples."),
            new Document("Last week I took the express train to San Diego."),
            new Document("Pineapple Express was a great movie."),
            new Document("Mother always said to eat my vegetables, but I never listened."),
            new Document("Fridays are the best part of my week."), new Document("Last Friday I watched a movie."),
            new Document("Next Friday I will watch the new Avengers movie."),
            new Document("I've started a new diet with vegetables and I've had a terrible week."),
            new Document("Atleast I can still eat pineapples."), new Document("My mother would be proud of me."),
            new Document("I ate a lot of pineapples in San Diego."),
            new Document("I can't believe mother keeps eating all of my chocolate."),
            new Document("I live for chocolate and pineapples"),
            new Document("My favorite activity is watching a movie and eating pineapples at the same time"),
            new Document("Last Friday I ate my pineapples diced"),
            new Document("Next week I will try eating my pineapple raw"),
            new Document("I wonder if next week I will take the express back to San Diego"),
            new Document("Don't tell mother but I stole her credit card and used it to buy pineapples"),
            new Document("I predict the new Avengers movie will be worthy of a diced pineapples"),
            new Document("Unfortudently, the movie theater doesn't sell diced pineapples"),
            new Document("I'm going to have to find a way to get my diced pineapples into the movie theater"),};

    @Before public void build() {
        index = InvertedIndexManager.createOrOpenPositional(path, analyzer, compressor);
    }

    @After public void tear() {
        File index = new File(path);
        String[] entries = index.list();
        for (String s : entries) {
            File currentFile = new File(index.getPath(), s);
            currentFile.delete();
        }
        index.delete();
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 8;
    }


    /*

        For this test we check to make sure that if all of the documents are on one segment that we can correctly find
        all of the phrases.
     */
    @Test public void test1() {
        for (Document doc : documents2) {
            index.addDocument(doc);
        }
        index.flush();
        queryIndex(Arrays.asList("movie", "theater"), 2);
        queryIndex(Arrays.asList("credit", "card"), 1);
        queryIndex(Arrays.asList("diced", "pineapples"), 3);
        queryIndex(Arrays.asList("eat", "pineapples"), 4);
        queryIndex(Arrays.asList("San", "Diego"), 3);
        queryIndex(Arrays.asList("Avengers", "Movie"), 2);
        queryIndex(Arrays.asList("next", "week"), 2);
        queryIndex(Arrays.asList("Last", "Friday"), 2);
    }

 /*
 *
 * For test2 we check to make sure that if the phrase isn't in the documents that has next doesn't return true.
 * This test is rather straight forward, however it checks to make sure that the positional index is correct and
 * that we aren't returning when we shouldn't.
 *
 * */
    @Test public void test2() {
        for (Document doc : documents1) {
            index.addDocument(doc);
        }
        index.flush();
        queryIndex(Arrays.asList("never", "mind"), 0);
    }

/*
*
* For test3 we are making sure that phrase search spans multiple segments. We do this by creating a new segment after
* each document is added.
*
* We are expecting the phrase "movie alone" to appear 4 times.
*
* */

    @Test public void test3(){
        for (Document doc : documents1) {
            index.addDocument(doc);
            index.flush();
        }
        queryIndex(Arrays.asList("movie", "alone"),4);

    }
    private void queryIndex(List<String> keyWords, int expectedCount) {
        Iterator<Document> it = index.searchPhraseQuery(keyWords);
        int counter = 0;
        while (it.hasNext()) {
            counter++;
            it.next();
        }
        assertEquals(expectedCount, counter);
    }
}
