package edu.uci.ics.cs221.index.ranking;

import edu.uci.ics.cs221.analysis.*;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.InvertedIndexSegmentForTest;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Test;
import edu.uci.ics.cs221.index.inverted.Pair;

import java.io.File;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Team10TfIdfTest {
    private String path = "./index/Team10TfIdfTest";
    private static InvertedIndexManager iim;
    private static List<String> docs;
    @Test
    public void test1(){
        iim = InvertedIndexManager.createOrOpenPositional(path,
                new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()),
                new DeltaVarLenCompressor());
        Document doc1 = new Document("test search project");
        Document doc2 = new Document("search engine is our project");
        Document doc3 = new Document("we need to finish the project");

        iim.addDocument(doc1);
        iim.addDocument(doc2);
        iim.addDocument(doc3);
        iim.flush();

        List<String> phrase = new ArrayList<>(Arrays.asList("search","engine","project"));
        Iterator<Pair<Document, Double>> res = iim.searchTfIdf(phrase,2);

        Pair<Document, Double> res0 = res.next();
        Pair<Document, Double> res1 = res.next();

        // make sure we get the correct num of results
        assertFalse(res.hasNext());

        // check if we get the correct documents in the correct order
        assertEquals(res0.getLeft(), doc2);
        assertEquals(res1.getLeft(), doc1);

        // check if the scores are calculated correctly
        // similarity of doc2 and doc1 are 0.508 and 0.176 respectively
        assertTrue(0.51 - res0.getRight() < 0.01);
        assertTrue(0.18 - res1.getRight() < 0.01);
    }

    @After
    public void clean(){
        File file = new File(path);
        String[] filelist = file.list();
        for(String f : filelist){
            File temp = new File(path, f);
            temp.delete();
        }
        file.delete();
    }
}
