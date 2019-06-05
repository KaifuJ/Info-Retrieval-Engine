package edu.uci.ics.cs221.index.ranking;

import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class Team20TfIdfTest {
    private String path = "./index/Team20TfIdfTest";
    private static InvertedIndexManager index;
    private Document doc1 = new Document("cats are moody and fishes are not cats");
    private Document doc2 = new Document("whales are mammals");
    private Document doc3 = new Document("dogs hate fishes");
    private Document doc4 = new Document("cats and dogs hate each other");

    @Before
    public void before() {
        index = InvertedIndexManager.createOrOpenPositional(path,
                new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()),
                new DeltaVarLenCompressor());

        index.addDocument(doc1);
        index.flush();
        index.addDocument(doc2);
        index.flush();
        index.mergeAllSegments();
        index.addDocument(doc3);
        index.addDocument(doc4);
        index.flush();


    }

    /***
     * Tests whether the documents are ranked in the correct order
     */

    @Test
    public void test1(){
        List<String> search_phrase = new ArrayList<>(Arrays.asList("cats","Dogs"));
        Iterator<Pair<Document, Double>> result = index.searchTfIdf(search_phrase,4);

        while (result.hasNext()){
            assertEquals(result.next().getLeft().getText(), doc4.getText());
            assertEquals(result.next().getLeft().getText(), doc3.getText());
            assertEquals(result.next().getLeft().getText(), doc1.getText());
            //assertEquals(result.next().getLeft().getText(), doc2.getText());

        }
        assertFalse(result.hasNext());

    }
    /***
     * Tests whether the documents are ranked in the correct order when the no. of documents in the index
     * is less than the no of documents asked for in the query.
     */

    @Test
    public void test2(){
        List<String> search_phrase = new ArrayList<>(Arrays.asList("cats","Dogs"));
        Iterator<Pair<Document, Double>> result = index.searchTfIdf(search_phrase,1000);

        while (result.hasNext()){
            assertEquals(result.next().getLeft().getText(), doc4.getText());
            assertEquals(result.next().getLeft().getText(), doc3.getText());
            assertEquals(result.next().getLeft().getText(), doc1.getText());
            //assertEquals(result.next().getLeft().getText(), doc2.getText());
        }

        assertFalse(result.hasNext());

    }

    /***
     * Tests whether all the documents are ranked in the correct order when the no of documents
     * asked for in the query is null.
     */

    @Test
    public void test3(){
        List<String> search_phrase = new ArrayList<>(Arrays.asList("cats","Dogs"));
        Iterator<Pair<Document, Double>> result = index.searchTfIdf(search_phrase, null);

        while (result.hasNext()){
            assertEquals(result.next().getLeft().getText(), doc4.getText());
            assertEquals(result.next().getLeft().getText(), doc3.getText());
            assertEquals(result.next().getLeft().getText(), doc1.getText());
           // assertEquals(result.next().getLeft().getText(), doc2.getText());
        }

        assertFalse(result.hasNext());

    }

    @After
    public void deleteFiles() {

        String SRC_FOLDER = path;
        File directory = new File(SRC_FOLDER);
        File[] listOfFiles = directory.listFiles();
        for (File file : listOfFiles) {
            file.delete();
        }
        directory.delete();

    }
}
