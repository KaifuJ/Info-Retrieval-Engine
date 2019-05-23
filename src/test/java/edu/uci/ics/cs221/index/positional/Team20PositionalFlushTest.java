package edu.uci.ics.cs221.index.positional;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.PageFileChannel;
import edu.uci.ics.cs221.index.inverted.PositionalIndexSegmentForTest;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class Team20PositionalFlushTest {

    /* Test to see if the posting lists and position lists are rightly stored onto the disk.
     * In this test, we set the threshold to 2. We then add two documents, after which flush() would be
     * automatically called*/
    @Test
    public void test_auto_call_flush() {
        Document d1 = new Document("rate roll rate sing roll");
        Document d2 = new Document("rate sky rate rate");

        Map<String, List<Integer>> expectedPostingList = new HashMap<>();
        expectedPostingList.put("rate", Arrays.asList(0, 1));
        expectedPostingList.put("roll", Arrays.asList(0));
        expectedPostingList.put("sing", Arrays.asList(0));
        expectedPostingList.put("sky", Arrays.asList(1));

        Map<Integer, Document> expectedDocStore = new HashMap<>();
        expectedDocStore.put(0, d1);
        expectedDocStore.put(1, d2);

        Table<String, Integer, List<Integer>> expectedPositions = TreeBasedTable.create();
        expectedPositions.put("rate", 0, Arrays.asList(0, 2));
        expectedPositions.put("roll", 0, Arrays.asList(1, 4));
        expectedPositions.put("sing", 0, Arrays.asList(3) );
        expectedPositions.put("rate", 1, Arrays.asList(0, 2, 3) );
        expectedPositions.put("sky", 1, Arrays.asList(1) );

        ComposableAnalyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        DeltaVarLenCompressor compressor = new DeltaVarLenCompressor();
        InvertedIndexManager ii = InvertedIndexManager.createOrOpenPositional("./index/Team20FlushTest/", analyzer, compressor);

        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 2;
        ii.addDocument(d1);
        ii.addDocument(d2);

        assertEquals(1, ii.getNumSegments());
        PositionalIndexSegmentForTest segment = ii.getIndexSegmentPositional(0);
        assertEquals(expectedPostingList, segment.getInvertedLists());
        assertEquals(expectedDocStore, segment.getDocuments());
        assertEquals(expectedPositions, segment.getPositions());
    }

    /* When the dictionary, posting list and the position list go beyond one page, check if the postings lists
     * and the position lists are rightly stored and retrieved. We change the PageFileChannel.PAGE_SIZE to 8 so that we write
     * more than a page when creating a dictionary file or a posting file or a position list file. We reset it back after the test.
     */
    @Test
    public void test_more_than_a_page_listings() {
        Document d1 = new Document("bled cat dog feed bled feed rate roll rate sing roll sing");
        Document d2 = new Document("bled rate sky rate rat bled rate rate rate");

        Map<String, List<Integer>> expectedPostingList = new HashMap<>();
        expectedPostingList.put("bled", Arrays.asList(0, 1));
        expectedPostingList.put("cat", Arrays.asList(0));
        expectedPostingList.put("dog", Arrays.asList(0));
        expectedPostingList.put("feed", Arrays.asList(0));
        expectedPostingList.put("rat", Arrays.asList(1));
        expectedPostingList.put("rate", Arrays.asList(0, 1));
        expectedPostingList.put("roll", Arrays.asList(0));
        expectedPostingList.put("sing", Arrays.asList(0));
        expectedPostingList.put("sky", Arrays.asList(1));

        Map<Integer, Document> expectedDocStore = new HashMap<>();
        expectedDocStore.put(0, d1);
        expectedDocStore.put(1, d2);

        Table<String, Integer, List<Integer>> expectedPositions = TreeBasedTable.create();
        expectedPositions.put("bled", 0, Arrays.asList(0, 4));
        expectedPositions.put("cat", 0, Arrays.asList(1));
        expectedPositions.put("dog", 0, Arrays.asList(2));
        expectedPositions.put("feed", 0, Arrays.asList(3, 5));
        expectedPositions.put("rate", 0, Arrays.asList(6, 8));
        expectedPositions.put("roll", 0, Arrays.asList(7, 10));
        expectedPositions.put("sing", 0, Arrays.asList(9, 11));
        expectedPositions.put("bled", 1, Arrays.asList(0, 5));
        expectedPositions.put("rat", 1, Arrays.asList(4));
        expectedPositions.put("rate", 1, Arrays.asList(1, 3, 6, 7, 8));
        expectedPositions.put("sky", 1, Arrays.asList(2));

        ComposableAnalyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        DeltaVarLenCompressor compressor = new DeltaVarLenCompressor();
        InvertedIndexManager ii = InvertedIndexManager.createOrOpenPositional("./index/Team20FlushTest/", analyzer, compressor);

        PageFileChannel.PAGE_SIZE = 8;
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 2;
        ii.addDocument(d1);
        ii.addDocument(d2);

        assertEquals(1, ii.getNumSegments());
        PositionalIndexSegmentForTest segment = ii.getIndexSegmentPositional(0);
        assertEquals(expectedPostingList, segment.getInvertedLists());
        assertEquals(expectedDocStore, segment.getDocuments());
        assertEquals(expectedPositions, segment.getPositions());
    }

    /* Check if postings and position lists are correctly stored and retrieved for more than one segments*/
    @Test
    public void check_auto_flush_call_twice() {
        Document d1 = new Document("rate roll rate rate roll");
        Document d2 = new Document("rate sky rate sky sky");

        Document d3 = new Document("feed bled feed bled cat dog cat");
        Document d4 = new Document("sing feed dog dog cat dog feed");

        Map<String, List<Integer>> expectedPostingList0 = new HashMap<>();
        expectedPostingList0.put("rate", Arrays.asList(0, 1));
        expectedPostingList0.put("roll", Arrays.asList(0));
        expectedPostingList0.put("sky", Arrays.asList(1));

        Map<Integer, Document> expectedDocStore0 = new HashMap<>();
        expectedDocStore0.put(0, d1);
        expectedDocStore0.put(1, d2);

        Table<String, Integer, List<Integer>> expectedPositions0 = TreeBasedTable.create(); //TODO need to check if TreeBasedTable is needed
        expectedPositions0.put("rate", 0, Arrays.asList(0, 2, 3));
        expectedPositions0.put("roll", 0, Arrays.asList(1, 4));
        expectedPositions0.put("rate", 1, Arrays.asList(0, 2));
        expectedPositions0.put("sky", 1, Arrays.asList(1, 3, 4));


        Map<String, List<Integer>> expectedPostingList1 = new HashMap<>();
        expectedPostingList1.put("feed", Arrays.asList(0, 1));
        expectedPostingList1.put("bled", Arrays.asList(0));
        expectedPostingList1.put("cat", Arrays.asList(0, 1));
        expectedPostingList1.put("dog", Arrays.asList(0, 1));
        expectedPostingList1.put("sing", Arrays.asList(1));

        Map<Integer, Document> expectedDocStore1 = new HashMap<>();
        expectedDocStore1.put(0, d3);
        expectedDocStore1.put(1, d4);

        Table<String, Integer, List<Integer>> expectedPositions1 = TreeBasedTable.create(); //TODO need to check if TreeBasedTable is needed
        expectedPositions1.put("feed", 0, Arrays.asList(0, 2));
        expectedPositions1.put("bled", 0, Arrays.asList(1, 3));
        expectedPositions1.put("cat", 0, Arrays.asList(4, 6));
        expectedPositions1.put("dog", 0, Arrays.asList(5));
        expectedPositions1.put("feed", 1, Arrays.asList(1, 6));
        expectedPositions1.put("sing", 1, Arrays.asList(0));
        expectedPositions1.put("cat", 1, Arrays.asList(4));
        expectedPositions1.put("dog", 1, Arrays.asList(2, 3, 5));


        ComposableAnalyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        DeltaVarLenCompressor compressor = new DeltaVarLenCompressor();
        InvertedIndexManager ii = InvertedIndexManager.createOrOpenPositional(
                "./index/Team20FlushTest/", analyzer, compressor);

        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 2;
        ii.addDocument(d1);
        ii.addDocument(d2);
        ii.addDocument(d3);
        ii.addDocument(d4);

        assertEquals(2, ii.getNumSegments());

        PositionalIndexSegmentForTest segment0 = ii.getIndexSegmentPositional(0);
        assertEquals(expectedPostingList0, segment0.getInvertedLists());
        assertEquals(expectedDocStore0, segment0.getDocuments());
        assertEquals(expectedPositions0, segment0.getPositions());

        PositionalIndexSegmentForTest segment1 = ii.getIndexSegmentPositional(1);
        assertEquals(expectedPostingList1, segment1.getInvertedLists());
        assertEquals(expectedDocStore1, segment1.getDocuments());
        assertEquals(expectedPositions1, segment1.getPositions());
    }

    @After
    public void deleteFiles() {
        PageFileChannel.PAGE_SIZE = 4096;
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
