package edu.uci.ics.cs221.index.ranking;

import edu.uci.ics.cs221.analysis.Analyzer;
import org.checkerframework.checker.units.qual.A;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.Pair;
import edu.uci.ics.cs221.storage.Document;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;


public class Team7TfIdfTest {
    private InvertedIndexManager manager;
    private static Document doc = new Document("cat dog monkey");
    private static Document doc1 = new Document("hello world");
    private static Document doc2 = new Document("cat cute ");
    String PATH = "./index/Team7TfIdfTest";

    @Before
    public void before(){
        Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        manager = InvertedIndexManager.createOrOpenPositional(PATH,analyzer, new DeltaVarLenCompressor());

        manager.addDocument(doc);
        manager.addDocument(doc1);
        manager.addDocument(doc2);

        manager.flush();
    }

    @After
    public void after(){
        try{
            File index = new File(PATH);
            String[]entries = index.list();
            for(String s: entries){
                File currentFile = new File(index.getPath(),s);
                currentFile.delete();
            }
            index.delete();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    /**
     * Test if the Top K returned in the expected order
     */
    public void test1(){
        List<String> keys = Arrays.asList("cat","dog");
        Iterator<Pair<Document, Double>> res = manager.searchTfIdf(keys,2);
        assertEquals(res.next().getLeft(),doc);
        assertEquals(res.next().getLeft(),doc2);
        assertFalse(res.hasNext());

    }

    @Test
    /**
     * Test if anything returns when search key is irrelevant
     */
    public void test2(){
        List<String> keys = Arrays.asList("good","test","case");
        Iterator<Pair<Document, Double>> res = manager.searchTfIdf(keys,2);
        assertFalse(res.hasNext());
    }


}
