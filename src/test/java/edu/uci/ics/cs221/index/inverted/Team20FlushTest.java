package edu.uci.ics.cs221.index.inverted;

import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.InvertedIndexSegmentForTest;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;


import static org.junit.Assert.assertEquals;

public class Team20FlushTest {

    /* Change the flush threshold and see if flush() is automatically called when the threshold is reached.
     * In this test, we set the threshold to 2. We then add two documents, after which flush() should be
     * automatically called*/
    @Test
    public void check_auto_flush_call() {
        Document d1 = new Document("rate roll");
        Document d2 = new Document("rate sky");
        Map<String, List<Integer>> expectedPostingList = new HashMap<>();
        expectedPostingList.put("rate", Arrays.asList(0, 1));
        expectedPostingList.put("roll", Arrays.asList(0));
        expectedPostingList.put("sky", Arrays.asList(1));
        Map<Integer, Document> expectedDocStore = new HashMap<>();
        expectedDocStore.put(0, d1);
        expectedDocStore.put(1, d2);
        ComposableAnalyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        InvertedIndexManager ii = InvertedIndexManager.createOrOpen("./index/Team20FlushTest/", analyzer);

        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 2;
        ii.addDocument(d1);
        ii.addDocument(d2);

        assertEquals(1, ii.getNumSegments());
        InvertedIndexSegmentForTest segment;
        segment = ii.getIndexSegment(0);
        assertEquals(expectedPostingList, segment.getInvertedLists());
        assertEquals(expectedDocStore, segment.getDocuments());
    }

    /* Change the flush threshold and see if flush() is automatically called twice when the threshold is reached.
     * In this test, we set the threshold to 2. We then add four documents. flush() should be
     * automatically called twice*/
    @Test
    public void check_auto_flush_call_twice() {
        Document d1 = new Document("rate roll");
        Document d2 = new Document("rate sky");
        Document d3 = new Document("feed bled");
        Document d4 = new Document("sing feed");

        Map<String, List<Integer>> expectedPostingList1 = new HashMap<>();
        expectedPostingList1.put("rate", Arrays.asList(0, 1));
        expectedPostingList1.put("roll", Arrays.asList(0));
        expectedPostingList1.put("sky", Arrays.asList(1));
        Map<Integer, Document> expectedDocStore1 = new HashMap<>();
        expectedDocStore1.put(0, d1);
        expectedDocStore1.put(1, d2);

        Map<String, List<Integer>> expectedPostingList2 = new HashMap<>();
        expectedPostingList2.put("feed", Arrays.asList(0, 1));
        expectedPostingList2.put("sing", Arrays.asList(1));
        expectedPostingList2.put("bled", Arrays.asList(0));
        Map<Integer, Document> expectedDocStore2 = new HashMap<>();
        expectedDocStore2.put(0, d3);
        expectedDocStore2.put(1, d4);

        ComposableAnalyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        InvertedIndexManager ii = InvertedIndexManager.createOrOpen("./index/Team20FlushTest/", analyzer);

        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 2;
        ii.addDocument(d1);
        ii.addDocument(d2);
        ii.addDocument(d3);
        ii.addDocument(d4);

        assertEquals(2, ii.getNumSegments());

        InvertedIndexSegmentForTest segment0;
        segment0 = ii.getIndexSegment(0);
        assertEquals(expectedPostingList1, segment0.getInvertedLists());
        assertEquals(expectedDocStore1, segment0.getDocuments());

        InvertedIndexSegmentForTest segment1;
        segment1 = ii.getIndexSegment(1);
        assertEquals(expectedPostingList2, segment1.getInvertedLists());
        assertEquals(expectedDocStore2, segment1.getDocuments());
    }

    /* Forcefully call flush(), keeping an empty in-memory buffer, flush() should do nothing*/
    @Test
    public void check_empty_mem_buffer() {
        ComposableAnalyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        InvertedIndexManager ii = InvertedIndexManager.createOrOpen("./index/Team20FlushTest/", analyzer);

        ii.flush();
        assertEquals(0, ii.getNumSegments());
    }

    @Test
    public void test4() {

        /* check if explicit call to flush works even when the number of documents in the in-memory data structure is below the
        threshold - checks that the number of segments is correct and the postings and document stores are returned correctly*/

        Document d1 = new Document("california arizona");
        Document d2 = new Document("california washington");
        Map<String, List<Integer>> expectedPostingList1 = new HashMap<>();
        Map<String, List<Integer>> expectedPostingList2 = new HashMap<>();
        expectedPostingList1.put("california", Arrays.asList(0));
        expectedPostingList1.put("arizona", Arrays.asList(0));
        expectedPostingList2.put("california", Arrays.asList(0));
        expectedPostingList2.put("washington", Arrays.asList(0));
        Map<Integer, Document> expectedDocStore1 = new HashMap<>();
        Map<Integer, Document> expectedDocStore2 = new HashMap<>();
        expectedDocStore1.put(0, d1);
        expectedDocStore2.put(0, d2);


        ComposableAnalyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        InvertedIndexManager ii = InvertedIndexManager.createOrOpen("./index/Team20FlushTest/", analyzer);
        ii.DEFAULT_FLUSH_THRESHOLD = 5;
        ii.addDocument(d1);
        ii.flush();
        ii.addDocument(d2);
        ii.flush();


        assertEquals(2, ii.getNumSegments());
        InvertedIndexSegmentForTest segment0;
        InvertedIndexSegmentForTest segment1;
        segment0 = ii.getIndexSegment(0);
        assertEquals(expectedPostingList1, segment0.getInvertedLists());
        assertEquals(expectedDocStore1, segment0.getDocuments());
        segment1 = ii.getIndexSegment(1);
        assertEquals(expectedPostingList2, segment1.getInvertedLists());
        assertEquals(expectedDocStore2, segment1.getDocuments());
    }


    @After
    public void deleteFiles() {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;

        String SRC_FOLDER = "./index/Team20FlushTest/";
        File directory = new File(SRC_FOLDER);
        File[] listOfFiles = directory.listFiles();
        for (File file : listOfFiles) {
            file.delete();
        }
        directory.delete();

    }

}