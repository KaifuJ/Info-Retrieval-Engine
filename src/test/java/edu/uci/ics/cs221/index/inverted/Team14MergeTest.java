package edu.uci.ics.cs221.index.inverted;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.InvertedIndexSegmentForTest;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.print.Doc;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class Team14MergeTest {
    InvertedIndexManager index;
    Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
    String path = "./index/Team14MergeTest/";

    Document[] documents1 = new Document[] {
            new Document("This morning I ate eggs"),
            new Document("Abstraction is often one floor above you."),
            new Document("Everyone was busy, so I went to the movie alone."),
            new Document("Please wait outside of the house."),
            new Document("Wednesday is hump day, but has anyone asked the camel if he’s happy about it?"),
            new Document("He told us a very exciting adventure story."),
            new Document("My Mom tries to be cool by saying that she likes all the same things that I do."),
            new Document("She advised him to come back at once."),
            new Document("She works two jobs to make ends meet; at least, that was her reason for not having time to join us."),
            new Document("How was the math test?"),
            new Document("Eggs come from chickens."),
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
            new Document("As a camel do you have one or two humps?") };

    Document[] documents2 = new Document[] {
            new Document("Hello"),
            new Document("I like to eat pineapples."),
            new Document("Last week I took the express train to San Diego."),
            new Document("Pineapple Express was a great movie."),
            new Document("Mother always said to eat my vegetables, but I never listened."),
            new Document("Fridays are the best part of my week."),
            new Document("Last Friday I watched a movie."),
            new Document("Next Friday I will watch the new Avengers movie."),
            new Document("I've started a new diet with vegetables and I've had a terrible week."),
            new Document("Atleast I can still eat pineapples."),
            new Document("My mother would be proud of me."),
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
            new Document("I'm going to have to find a way to get my diced pineapples into the movie theater")};

    @Before public void build() {
        index = InvertedIndexManager.createOrOpen(path, analyzer);
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
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

      For the first test we are checking to make sure that when the merge threshold is 2 we always only have
      a single segment. The reason for this is that after the first segment is entered every time an additional segment
      is inserted we will merge the two segments resulting in there only being one segment present.
     */
    @Test public void test1() {
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 2;
        for (Document d : documents1) {
            index.addDocument(d);
            assert index.getNumSegments() == 1;
        }

    }

    /*

        For this test we check if the number of documents is the same at the end of all the merges as what we inserted
        we also check to see if the number of occurrences of someone of the key words is the same as what we counted
        by hand. Rather than checking the number of segments this test is really trying to test the content of the
        final segment.
     */

    @Test public void test2() {
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 4;
        for (Document d : documents2) {
            index.addDocument(d);
        }
        while (index.getNumSegments() != 1) {
            index.mergeAllSegments();
        }
        InvertedIndexSegmentForTest segment = index.getIndexSegment(0);
        Map<Integer, Document> docs = segment.getDocuments();
        Map<String, List<Integer>> invertedLists = segment.getInvertedLists();
        assert docs.size() == 22;
        assert invertedLists.get("pineappl").size() == 12;
        assert invertedLists.get("movi").size() == 7;
        assert invertedLists.get("week").size() == 5;
        assert invertedLists.get("mother").size() == 4;
        assert invertedLists.get("dice").size() == 4;
        assert invertedLists.get("express").size() == 3;
        assert invertedLists.get("diego").size() == 3;
        assert invertedLists.get("fridai").size() == 4;
        assert invertedLists.get("san").size() == 3;
        assert invertedLists.get("veget").size() == 2;
        assert invertedLists.get("aveng").size() == 2;
        assert invertedLists.get("theater").size() == 2;
        assert invertedLists.get("chocol").size() == 2;
        assert invertedLists.get("stole").size() == 1;
    }

}