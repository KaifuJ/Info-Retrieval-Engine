package edu.uci.ics.cs221.index.ranking;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.*;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class Team12SearchTfIdf {
    private Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
    private String path = "./index/Team12SearchTfIdfTest/";
    private InvertedIndexManager invertedIndex;
    Document doc1 = new Document("The University of California, Irvine is a public research university located in Irvine, California");
    Document doc2 = new Document("Irvine University offers 87 undergraduate degrees and 129 graduate and professional degrees");
    Document doc3 = new Document("Irvine Company earns a lot of money");
    Document doc4 = new Document("2019 Mercedes-Benz UCI Mountain Bike World Cup");
    Compressor compressor = new DeltaVarLenCompressor();

    /**
     * tests if null is passed for topk then should return all documents
     */
    @Test
    public void test1() {
        invertedIndex = InvertedIndexManager.createOrOpenPositional(path, analyzer, compressor);
        invertedIndex.addDocument(doc1);
        invertedIndex.addDocument(doc2);
        invertedIndex.addDocument(doc3);
        invertedIndex.addDocument(doc4);
        invertedIndex.flush();

        List<String> phrase = new ArrayList<>();
        phrase.add("University");
        phrase.add("of");
        phrase.add("California");
        phrase.add("Irvine");
        Iterator<Pair<Document, Double>> iterate = invertedIndex.searchTfIdf(phrase, null);
        int counter = 0;
        while (iterate.hasNext()) {
            iterate.next();
            counter++;
        }
        assertEquals(4,counter);
    }
    /**
     * test if the score of documents is in descending order
     */
    @Test
    public void test2() {
        invertedIndex = InvertedIndexManager.createOrOpenPositional(path, analyzer, compressor);

        invertedIndex.addDocument(doc1);
        invertedIndex.addDocument(doc2);
        invertedIndex.addDocument(doc3);
        invertedIndex.addDocument(doc4);
        invertedIndex.flush();

        List<String> phrase = new ArrayList<>();
        phrase.add("University");
        phrase.add("of");
        phrase.add("California");
        phrase.add("Irvine");
        Iterator<Pair<Document, Double>> iterate = invertedIndex.searchTfIdf(phrase, 4);

        Double prev = 1.0;

        while (iterate.hasNext()) {
            Double val = iterate.next().getRight();
            assert prev > val;
            prev = val;
        }
    }
    @After
    public void cleanUp() {
        PageFileChannel.resetCounters();
        File f = new File(path);
        File[] files = f.listFiles();
        for (File file : files) {
            file.delete();
        }
        f.delete();
    }
}