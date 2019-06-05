package edu.uci.ics.cs221.index.ranking;

import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.Compressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.NaiveCompressor;
import edu.uci.ics.cs221.index.inverted.Pair;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class Team9TfidfTest {
    private String indexFolder  = "./index/Team9TfidfTest/";
    private ComposableAnalyzer analyzer;
    private Compressor compressor;
    private InvertedIndexManager invertedIndex;
    private static String textUrl1 = "https://raw.githubusercontent.com/DanniUCI/Master-Courses/master/Team9TfidfTest.txt";
    private static String textUrl2 = "https://raw.githubusercontent.com/DanniUCI/Master-Courses/master/Team9TfidfTest2.txt";
    static List<String> allDocuments;

    @Before
    public void setUp() {
        analyzer = new ComposableAnalyzer(new PunctuationTokenizer(),new PorterStemmer());
        compressor = new NaiveCompressor();
        invertedIndex = InvertedIndexManager.createOrOpenPositional(indexFolder, analyzer, compressor);
        allDocuments = getOnlineTextFile(textUrl1);
        invertedIndex.DEFAULT_FLUSH_THRESHOLD = 10;
        invertedIndex.DEFAULT_MERGE_THRESHOLD = 10;

    }

    @After
    public void tearDown() {
        // local storage folder is a flat folder which doesn't contain sub folder
        // In case of any exception, that will be thrown out.
        invertedIndex.DEFAULT_FLUSH_THRESHOLD = 1000;
        invertedIndex.DEFAULT_MERGE_THRESHOLD = 8;
        File localStorageFolder = new File(indexFolder);
        for (File file : localStorageFolder.listFiles()) {
            file.delete();
        }
        localStorageFolder.delete();
    }

    //Test the functions. Check the correctness of the number of results and the order of results.
    @Test
    public void test1() {
        for(int i=0;i<100;i++){
            invertedIndex.addDocument(new Document(allDocuments.get(i%allDocuments.size())));
        }
        assertEquals(5, invertedIndex.getNumSegments());
        for (int i = 0; i < invertedIndex.getNumSegments(); i ++) {
            assertEquals(20, invertedIndex.getNumDocuments(i));
        }
        assertEquals(20, invertedIndex.getDocumentFrequency(0, "travel"));
        assertEquals(4, invertedIndex.getDocumentFrequency(0, "tourism"));
        List<String> phrase = new ArrayList<>(Arrays.asList("travel", "international", "internal", "tourism"));
        Iterator<Pair<Document, Double>> res = invertedIndex.searchTfIdf(phrase, 10);
        int count = 0;
        String expect = "In some countries, non-local internal travel may require an internal passport, while international travel typically requires a passport and visa.";
        while (res.hasNext()) {
            assertEquals(expect, res.next().getLeft().getText());
            count ++;
        }
        assertEquals(10, count);

    }

    //Test the functions. Check the correctness of the number of results and the order of results.
    //In this test, we have some corner cases. Two documents are very similar, but the score is different.
    @Test
    public void test2() {
        allDocuments = getOnlineTextFile(textUrl2);
        for(int i=0;i<10;i++){
            invertedIndex.addDocument(new Document(allDocuments.get(i%allDocuments.size())));
        }
        assertEquals(1, invertedIndex.getNumSegments());
        assertEquals(10, invertedIndex.getNumDocuments(0));

        assertEquals(7, invertedIndex.getDocumentFrequency(0, "animal"));
        assertEquals(5, invertedIndex.getDocumentFrequency(0, "cat"));
        List<String> phrase = new ArrayList<>(Arrays.asList("bear", "penguin", "mussel", "animal"));
        Iterator<Pair<Document, Double>> res = invertedIndex.searchTfIdf(phrase, 2);
        int count = 0;
        List<String> expect = new ArrayList<>();
        List<String> resDoc = new ArrayList<>();
        expect.add("homothermal animal human bear deer monkey rabbit panda cat dog penguin dolphin");
        expect.add("mollush animal sea snail clam mussel jellyfish shell mussel");
        while (res.hasNext()) {
            resDoc.add(res.next().getLeft().getText());
            count ++;
        }
        assertEquals(2, count);
        assertEquals(expect, resDoc);

    }

    private static List<String> getOnlineTextFile(String URL){
        List<String> result = new ArrayList<>();
        try {
            java.net.URL url = new URL(URL);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String s;
            while ((s = reader.readLine()) != null) {
                result.add(s);
            }
            reader.close();
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
        return result;
    }

}
