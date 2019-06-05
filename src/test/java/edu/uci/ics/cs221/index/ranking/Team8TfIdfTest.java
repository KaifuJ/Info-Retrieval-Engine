package edu.uci.ics.cs221.index.ranking;

import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.Pair;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class Team8TfIdfTest {
    private String path = "./index/Team8TfIdfTest";
    private InvertedIndexManager indexmanger;
    private Document[] documents;

    @Before
    public void init(){
        indexmanger = InvertedIndexManager.createOrOpenPositional(path,
                new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()),
                new DeltaVarLenCompressor());

        documents = new Document[] {
                new Document("An apple a day keeps a doctor away"),
                new Document("One rotten apple spoils the whole barrel"),
                new Document("Fortune knocks once at everyone's rotten door"),
                new Document("Throw away the apple because of the core")

        };
    }

    /**
     Test if searchTfIdf function works well with topK == null
     **/
    @Test
    public void test1(){
        //add documents
        for(int i=0;i<documents.length;i++){
            indexmanger.addDocument(documents[i]);
            indexmanger.flush();
        }
        List<String> keywords = Arrays.asList("apple", "apple", "rotten");
        Iterator<Pair<Document, Double>> res = indexmanger.searchTfIdf(keywords,null);
        List<Document> resDoc = Arrays.asList(documents[1],documents[2],documents[0],documents[3]);
        int counter = 0;
        Double pre = 1.0;
        while(res.hasNext()){
            Double cur = res.next().getRight();
            assertTrue(pre >= cur);
            pre = cur;
            counter++;
        }
        assertEquals(counter,4);
    }

    /**
     Test if searchTfIdf function works well with normal topK
     **/
    @Test
    public void test2(){
        //add documents
        for(int i=0;i<documents.length;i++){
            indexmanger.addDocument(documents[i]);
            indexmanger.flush();
        }
        List<String> keywords = Arrays.asList("apple", "apple", "rotten");
        Iterator<Pair<Document, Double>> res = indexmanger.searchTfIdf(keywords,2);
        Pair<Document,Double> res1 = res.next();
        Pair<Document,Double> res2 = res.next();
        assertFalse(res.hasNext());
        assertEquals(res1.getLeft(),documents[1]);
        assertTrue(res1.getRight()>0.37 && res1.getRight()<0.38);
        assertEquals(res2.getLeft(),documents[2]);
        assertTrue(res2.getRight()>0.30 && res2.getRight()<0.31);

    }

    @After
    public void clean() {

        File files = new File(path);
        for (File file: files.listFiles()){
            if (!file.isDirectory()){
                file.delete();
            }
        }
        files.delete();
    }
}
