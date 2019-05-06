package edu.uci.ics.cs221.index.inverted;

import edu.uci.ics.cs221.analysis.*;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class Team3StressTest {

    static Analyzer analyzer;
    static List<String> allDocuments;
    static InvertedIndexManager invertedIndexManager;
    private static final int TOTALNUM = 100000;
    private static final String textUrl = "https://grape.ics.uci.edu/wiki/public/raw-attachment/wiki/cs221-2019-spring-project2/Team3StressTest.txt";
    private static final String indexFolder = "./index/Team3StressTest/";


    //Construct to do some initialization like downloading the text resources
    //and objects construction.
    // finish this test in 10 min.
    @Test(timeout = 600000)
    public void setupAndRun(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 5000;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 12;

        allDocuments = getOnlineTextFile(textUrl);
        analyzer = new ComposableAnalyzer(new PunctuationTokenizer(),new PorterStemmer());
        invertedIndexManager = InvertedIndexManager.createOrOpen(indexFolder, analyzer);
        PageFileChannel.resetCounters();
        //Copy the resource text 100000 times so that the total resources will be very large
        //so that we could meet the demand of a Stress Test.(Very large dataset)
        for(int i=0;i<TOTALNUM;i++){
            invertedIndexManager.addDocument(new Document(allDocuments.get(i%allDocuments.size())));
        }
        assertTrue(PageFileChannel.writeCounter>=TOTALNUM/invertedIndexManager.DEFAULT_FLUSH_THRESHOLD);


        try {
            test1();
        } catch (Throwable e) {
            System.out.println("Team3StressTest test1 FAILED");
            e.printStackTrace();
        }

        try {
            test2();
        } catch (Throwable e) {
            System.out.println("Team3StressTest test2 FAILED");
            e.printStackTrace();
        }

        try {
            test3();
        } catch (Throwable e) {
            System.out.println("Team3StressTest test3 FAILED");
            e.printStackTrace();
        }

        try {
            test4();
        } catch (Throwable e) {
            System.out.println("Team3StressTest test4 FAILED");
            e.printStackTrace();
        }

    }


    //Test searchQuery with keyword "CD" and see if it can get the right answer.
    //If true it means the engine didn't crash under a Stress Condition.

    public void test1(){
        PageFileChannel.resetCounters();
        Iterator<Document> result1 = invertedIndexManager.searchQuery("CD");
        int count = 0;
        while(result1.hasNext()){
            result1.next();
            count++;
        }
        assertTrue(PageFileChannel.readCounter>=50);
        assertEquals(10000, count);
    }


    //Test searchQuery with keyword "DVD" and see if it can get the right answer.
    //If true it means the engine didn't crash under a Stress Condition.

    public void test2(){
        PageFileChannel.resetCounters();
        Iterator<Document> result1 = invertedIndexManager.searchQuery("DVD");
        int count = 0;
        while(result1.hasNext()){
            result1.next();
            count++;
        }
        assertTrue(PageFileChannel.readCounter>=100);
        assertEquals(20000, count);

    }


    //Test searchAndQuery with keyword "CD" and "DVD" and see if it finds no answer.

    public void test3(){
        Iterator<Document> result1 = invertedIndexManager.searchAndQuery(Arrays.asList("DVD","CD"));
        assertTrue(!result1.hasNext());
    }


    //Test searchOrQuery with keyword "DVD" or "CD" and see if it can find the right answer.
    //If true it means the engine didn't crash under a Stress Condition.

    public void test4(){
        PageFileChannel.resetCounters();
        Iterator<Document> result1 = invertedIndexManager.searchOrQuery(Arrays.asList("DVD","CD"));
        int count = 0;
        while(result1.hasNext()){
            result1.next();
            count++;
        }
        assertEquals(30000, count);
        assertTrue(PageFileChannel.readCounter>=150);
    }


    //Delete the files that are created during the process of searching.
    @After
    public void after(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 8;
        Team2StressTest.delAllFile(indexFolder);
    }


    //Get our large resources text file from a URL
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