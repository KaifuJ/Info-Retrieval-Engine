package edu.uci.ics.cs221.index.positional;

import edu.uci.ics.cs221.analysis.*;
import edu.uci.ics.cs221.index.inverted.*;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class Team8PositionalStressTest {

    private Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(),new PorterStemmer());
    private Compressor compressor = new DeltaVarLenCompressor();
    private InvertedIndexManager invertedIndexManager;
    private int docNum = 200000;
    private String textUrl = "https://raw.githubusercontent.com/NinoXing/Resource-UCI-CS221/master/Team8StressTest.txt";
    private String pathName = "./index/Team8PositionalStressTest";


    //Use the stress test structure modified by TA in project2

    //init and go on testing
    //timeout is 20 min.
    @Test(timeout = 1200000)
    public void init(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 5000;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 12;

        List<String> textContent = getContent(textUrl);
        invertedIndexManager = InvertedIndexManager.createOrOpenPositional(pathName, analyzer, compressor);
        PageFileChannel.resetCounters();

        //Add document. There will be auto flush and auto merge.
        //The total number of documents in this test is 200000.
        //Among these 200000 documents, there're 4 kinds of documents with different content, each of which has a number of 50000.
        //We assume this is large enough.
        for(int i=0;i<docNum;i++){
            invertedIndexManager.addDocument(new Document(textContent.get(i%textContent.size())));
        }
        assertTrue(PageFileChannel.writeCounter>=docNum/invertedIndexManager.DEFAULT_FLUSH_THRESHOLD);


        try {
            test1();
        } catch (Throwable e) {
            System.out.println("Team8PositionalStressTest test1 FAILED");
            e.printStackTrace();
        }

        try {
            test2();
        } catch (Throwable e) {
            System.out.println("Team8PositionalStressTest test2 FAILED");
            e.printStackTrace();
        }

        try {
            test3();
        } catch (Throwable e) {
            System.out.println("Team8PositionalStressTest test3 FAILED");
            e.printStackTrace();
        }

        try {
            test4();
        } catch (Throwable e) {
            System.out.println("Team8PositionalStressTest test4 FAILED");
            e.printStackTrace();
        }

        try {
            test5();
        } catch (Throwable e) {
            System.out.println("Team8PositionalStressTest test5 FAILED");
            e.printStackTrace();
        }

    }

    //Test if all the documents have been flushed into segments
    public void test1(){
        int res = 0;
        for(int i = 0; i < invertedIndexManager.getNumSegments(); ++i){
            res += invertedIndexManager.getIndexSegmentPositional(i).getDocuments().size();
        }

        assertEquals(200000, res);

    }

    //Test if the searchQuery function still works well
    public void test2(){
        Iterator<Document> res = invertedIndexManager.searchQuery("apple");
        int count = 0;
        while(res.hasNext()){
            res.next();
            count++;
        }
        assertEquals(50000, count);
    }


    //Test if the searchAndQuery function still works well
    public void test3(){
        Iterator<Document> res = invertedIndexManager.searchAndQuery(Arrays.asList("Fortune","butter"));
        assertTrue(!res.hasNext());
    }


    //Test if the searchOrQuery function still works well
    public void test4(){
        Iterator<Document> res = invertedIndexManager.searchOrQuery(Arrays.asList("apple","Judge"));
        int count = 0;
        while(res.hasNext()){
            res.next();
            count++;
        }
        assertEquals(100000, count);
    }


    //Test if the searchPhraseQuery function works well
    public void test5(){
        Iterator<Document> res = invertedIndexManager.searchPhraseQuery(Arrays.asList("first","sight"));
        int count = 0;
        while(res.hasNext()){
            res.next();
            count++;
        }
        assertEquals(50000, count);
    }


    //Clean up and reset threshold
    @After
    public void after(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 8;

        try{
            File files = new File(pathName);
            for (File file: files.listFiles()){
                if (!file.isDirectory()){
                    file.delete();
                }
            }
            files.delete();
        }catch (Exception e){
            System.out.println("Error when deleting files");
        }

    }

    //Get text content from a URL
    private List<String> getContent(String URL){
        List<String> res = new ArrayList<>();
        try {
            java.net.URL url = new URL(URL);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String s;
            while ((s = reader.readLine()) != null) {
                if(!s.isEmpty()) res.add(s);
            }
            reader.close();
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
        return res;
    }
}
