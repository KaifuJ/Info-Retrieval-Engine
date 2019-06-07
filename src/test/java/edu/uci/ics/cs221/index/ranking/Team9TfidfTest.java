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
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class Team9TfidfTest {
    public static String text1 = "If you don't travel around, you'd think this is the world.\n" +
            "They travel round together to different locations.\n" +
            "He travel from one place to another.\n" +
            "Travel is the movement of people between distant geographical locations. \n" +
            "Travel can be done by foot, bicycle, automobile, train, boat, bus, airplane, ship or other means, with or without luggage, and can be one way or round trip.\n" +
            "Travel can also include relatively short stays between successive movements.\n" +
            "Reasons for traveling include recreation, tourism or vacationing, research travel, the gathering of information, visiting people, volunteer travel for charity\n" +
            "Travel for the purpose of tourism is reported to have started around this time when people began to travel for fun as travel was no longer a hard and challenging task. \n" +
            "Travel may be local, regional, national domestic or international.\n" +
            "In some countries, non-local internal travel may require an internal passport, while international travel typically requires a passport and visa.";

    public static String text2 = "mammal animal dog cat tiger lion dolphin panda bear deer\n" +
            "dog golden retriever, Shiba， Huskie\n" +
            "cat Ragdoll snowshoe, British shorthair, Siamese\n" +
            "Felidae cat tiger lion leopard\n" +
            "cold-blooded animal snake frog fish crocodile lizard \n" +
            "lovely animal bear deer cat dog sheep rabbit panda penguin animal\n" +
            "homothermal animal human bear deer monkey rabbit panda cat dog penguin dolphin\n" +
            "ocean animal penguin dolphin whale fish golden fish jellyfish sear sea lion sea snail shrimp crab \n" +
            "mollush animal sea snail clam mussel jellyfish shell mussel\n" +
            "animal for eat pig cow fish shrimp chicken duck crab";


    private String indexFolder  = "./index/Team9TfidfTest/";
    private ComposableAnalyzer analyzer;
    private Compressor compressor;
    private InvertedIndexManager invertedIndex;

    static List<String> allDocuments;

    @Before
    public void setUp() {
        analyzer = new ComposableAnalyzer(new PunctuationTokenizer(),new PorterStemmer());
        compressor = new NaiveCompressor();
        invertedIndex = InvertedIndexManager.createOrOpenPositional(indexFolder, analyzer, compressor);
        allDocuments = getDocuments(text1);
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
        assertEquals(20, invertedIndex.getDocumentFrequency(0, analyzer.analyze("travel").get(0)));
        assertEquals(4, invertedIndex.getDocumentFrequency(0, analyzer.analyze("tourism").get(0)));
        List<String> phrase = new ArrayList<>(Arrays.asList("travel", "local", "international", "internal", "tourism"));
        Iterator<Pair<Document, Double>> res = invertedIndex.searchTfIdf(phrase, 10);
        int count = 0;
        String expect = "Travel may be local, regional, national domestic or international.";
        while (res.hasNext()) {
            Pair<Document, Double> result = res.next();
            assertEquals(expect, result.getLeft().getText());
            count ++;
        }
        assertEquals(10, count);

    }

    //Test the functions. Check the correctness of the number of results and the order of results.
    //In this test, we have some corner cases. Two documents are very similar, but the score is different.
    @Test
    public void test2() {
        allDocuments = getDocuments(text2);
        for(int i=0;i<10;i++){
            invertedIndex.addDocument(new Document(allDocuments.get(i%allDocuments.size())));
        }
        assertEquals(1, invertedIndex.getNumSegments());
        assertEquals(10, invertedIndex.getNumDocuments(0));

        assertEquals(7, invertedIndex.getDocumentFrequency(0, analyzer.analyze("animal").get(0)));
        assertEquals(5, invertedIndex.getDocumentFrequency(0, analyzer.analyze("cat").get(0)));
        List<String> phrase = new ArrayList<>(Arrays.asList("bear", "penguin", "mussel", "animal"));
        Iterator<Pair<Document, Double>> res = invertedIndex.searchTfIdf(phrase, 2);
        int count = 0;
        List<String> expect = new ArrayList<>();
        List<String> resDoc = new ArrayList<>();
        expect.add("mollush animal sea snail clam mussel jellyfish shell mussel");
        expect.add("homothermal animal human bear deer monkey rabbit panda cat dog penguin dolphin");
        while (res.hasNext()) {
            Pair<Document, Double> result = res.next();
            System.out.println(result);
            resDoc.add(result.getLeft().getText());
            count ++;
        }
        assertEquals(2, count);
        assertEquals(expect, resDoc);

    }

    private static List<String> getDocuments(String text){
        return Arrays.stream(text.split("\\n")).map(s -> s.trim()).collect(Collectors.toList());
    }

}
