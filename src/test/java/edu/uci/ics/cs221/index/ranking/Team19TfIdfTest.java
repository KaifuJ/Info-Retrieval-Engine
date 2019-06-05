package edu.uci.ics.cs221.index.ranking;

import edu.uci.ics.cs221.analysis.NaiveAnalyzer;
import edu.uci.ics.cs221.index.inverted.Compressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.NaiveCompressor;
import edu.uci.ics.cs221.index.inverted.Pair;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.storage.Document;
import org.junit.Test;

public class Team19TfIdfTest {
    private Analyzer an = new NaiveAnalyzer();
    private Compressor cp = new NaiveCompressor();
    private InvertedIndexManager iim;
    private String file = "./index/Team19TfIdfTest/";

    private Document d1 = new Document("cat dog");
    private Document d2 = new Document("cat elephant");
    private Document d3 = new Document("wolf dog dog");
    private Document d4 = new Document("cat dog dog");

    @Before
    public void setup() {
        iim = InvertedIndexManager.createOrOpenPositional(file, an, cp);
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 3;
    }

    @After
    public void cleanup() throws Exception {
        try{
            File index = new File(file);
            String[] f = index.list();
            for(String s: f){
                File currentFile = new File(index.getPath(),s);
                currentFile.delete();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Files.deleteIfExists(Paths.get(file));

        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
    }

    // test searchTfIdf() returns correct documents with right order, when there are two segments
    @Test
    public void testTfIdf1() {
        List<String> query = new ArrayList<>(Arrays.asList("cat", "dog", "dog"));
        iim.addDocument(d1);
        iim.addDocument(d2);
        iim.addDocument(d3);
        iim.addDocument(d4);
        iim.flush();

        Iterator<Pair<Document, Double>> actual = iim.searchTfIdf(query, 3);
        assertEquals(d4, actual.next().getLeft());
        assertEquals(d1, actual.next().getLeft());
        assertEquals(d3, actual.next().getLeft());
        assertFalse(actual.hasNext());
    }
}
