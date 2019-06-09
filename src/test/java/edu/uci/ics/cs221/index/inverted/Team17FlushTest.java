package edu.uci.ics.cs221.index.inverted;

import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.InvertedIndexSegmentForTest;

import edu.uci.ics.cs221.analysis.*;

import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class Team17FlushTest {

    @After
    public void cleanUp (){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        File dir = new File("./index/Team17");
        for (File file: dir.listFiles()){
            if (!file.isDirectory()){
                file.delete();
            }
        }
        dir.delete();
    }

    /**
     * Tests if initialization works correctly in InvertedIndexManager when creating a new folder.
     */
    @Test
    public void testInit() {
        InvertedIndexManager iim;
        iim = InvertedIndexManager.createOrOpen("./index/Team17", new ComposableAnalyzer( new PunctuationTokenizer(), new PorterStemmer()));

        int expectedNumSegments = 0;
        assertEquals(expectedNumSegments, iim.getNumSegments());
        assertEquals(null, iim.getIndexSegment(0));
    }


    /**
     *  Tests if addDocuments, flush and getIndexSegment works
     */
    @Test
    public void testAddDocumentFlush() {
        InvertedIndexManager iim;
        iim = InvertedIndexManager.createOrOpen("./index/Team17", new ComposableAnalyzer( new PunctuationTokenizer(), new PorterStemmer()));

        Document doc1 = new Document("test");
        Document doc2 = new Document("test case");

        iim.addDocument(doc1);
        iim.addDocument(doc2);
        iim.flush();
        int expectedNumSegments = 1;
        assertEquals(expectedNumSegments, iim.getNumSegments());
        InvertedIndexSegmentForTest iiTestSegment = iim.getIndexSegment(0);

        Map<String, List<Integer>> invertedLists = iiTestSegment.getInvertedLists();
        assertEquals(2,invertedLists.size());

        PorterStemmer ps = new PorterStemmer();

        List<Integer> expectedList = Arrays.asList(0,1);
        List<Integer> invertedList = invertedLists.get(ps.stem("test"));
        Collections.sort(invertedList);
        assertEquals(expectedList,invertedList);

        expectedList = Arrays.asList(1);
        invertedList = invertedLists.get(ps.stem("case"));
        assertEquals(expectedList, invertedList);

        Map<Integer, Document> expectedDocuments = iiTestSegment.getDocuments();
        assertEquals(2, expectedDocuments.size());
        assertEquals(doc1, expectedDocuments.get(0));
        assertEquals(doc2, expectedDocuments.get(1));

        File dir = new File("./index/Team17");
        for (File file: dir.listFiles()){
            if (!file.isDirectory()){
                file.delete();
            }
        }
    }

    /**
     *
     */
    @Test
    public void testAutoFlush(){
        InvertedIndexManager iim;
        iim = InvertedIndexManager.createOrOpen("./index/Team17", new ComposableAnalyzer( new PunctuationTokenizer(), new PorterStemmer()));

        Document doc1 = new Document("test case auto");
        for (int i=0; i<InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD; i++){
            iim.addDocument(doc1);
        }
        iim.addDocument(doc1);
        iim.addDocument(new Document("text base"));

        int expectedNumSegments = 1;
        assertEquals(expectedNumSegments, iim.getNumSegments());
        InvertedIndexSegmentForTest iiTestSegment = iim.getIndexSegment(0);

        Map<String, List<Integer>> invertedLists = iiTestSegment.getInvertedLists();
        assertEquals(3,invertedLists.size());

        Map<Integer, Document> expectedDocuments = iiTestSegment.getDocuments();
        assertEquals(InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD, expectedDocuments.size());

        PorterStemmer ps = new PorterStemmer();

        List<Integer> invertedList = invertedLists.get(ps.stem("test"));
        assertEquals(InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD,invertedList.size());
        Collections.sort(invertedList);
        for (int i=0; i<InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD; i++){
            assertEquals(i, invertedList.get(i).intValue());
            assertEquals(doc1, expectedDocuments.get(i));
        }

        invertedList = invertedLists.get(ps.stem("case"));
        assertEquals(InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD,invertedList.size());
        Collections.sort(invertedList);
        for (int i=0; i<InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD; i++){
            assertEquals(i, invertedList.get(i).intValue());
            assertEquals(doc1, expectedDocuments.get(i));
        }

        invertedList = invertedLists.get(ps.stem("auto"));
        assertEquals(InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD,invertedList.size());
        Collections.sort(invertedList);
        for (int i=0; i<InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD; i++){
            assertEquals(i, invertedList.get(i).intValue());
            assertEquals(doc1, expectedDocuments.get(i));
        }

        File dir = new File("./index/Team17");
        for (File file: dir.listFiles()){
            if (!file.isDirectory()){
                file.delete();
            }
        }
    }

    /**
     * tests if multiple documents of different contents can work properly; also tests if mixed explicit flush and auto flush will create conflicts.
     */
    @Test
    public void testMixedDocumentAndFlush(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;

        InvertedIndexManager iim;
        iim = InvertedIndexManager.createOrOpen("./index/Team17", new ComposableAnalyzer( new PunctuationTokenizer(), new PorterStemmer()));

        Document doc1 = new Document("test case auto");
        Document doc2 = new Document("text base");
        Document doc3 = new Document("tent vase");

        iim.addDocument(doc3);
        iim.flush();

        for (int i=0; i<1000; i++){
            if (i%2 == 0){
                iim.addDocument(doc1);
            }else {
                iim.addDocument(doc2);
            }
        }

        int expectedNumSegments = 2;
        assertEquals(expectedNumSegments, iim.getNumSegments());
        InvertedIndexSegmentForTest iiTestSegment = iim.getIndexSegment(1);

        Map<String, List<Integer>> invertedLists = iiTestSegment.getInvertedLists();
        assertEquals(5,invertedLists.size());

        Map<Integer, Document> expectedDocuments = iiTestSegment.getDocuments();
        assertEquals(InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD, expectedDocuments.size());

        PorterStemmer ps = new PorterStemmer();

        List<Integer> invertedList = invertedLists.get(ps.stem("auto"));
        assertEquals(InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD / 2,invertedList.size());
        Collections.sort(invertedList);
        for (int i=0; i<500; i++){
            assertEquals(i*2, invertedList.get(i).intValue());
            assertEquals(doc1, expectedDocuments.get(i*2));
        }

        invertedList = invertedLists.get(ps.stem("base"));
        assertEquals(InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD / 2,invertedList.size());
        Collections.sort(invertedList);
        for (int i=0; i<500; i++){
            assertEquals(i*2+1, invertedList.get(i).intValue());
            assertEquals(doc1, expectedDocuments.get(i*2));
        }

        File dir = new File("./index/Team17");
        for (File file: dir.listFiles()){
            if (!file.isDirectory()){
                file.delete();
            }
        }
    }

    /**
     * Test if flushes when buffer is empty is handled correctly.
     */
    @Test
    public void testFlushEmptyBuffer() {
        InvertedIndexManager iim;
        iim = InvertedIndexManager.createOrOpen("./index/Team17", new ComposableAnalyzer( new PunctuationTokenizer(), new PorterStemmer()));

        iim.flush();
        iim.flush();

        int expectedNumSegments = 0;
        assertEquals(expectedNumSegments, iim.getNumSegments());
        assertEquals(null, iim.getIndexSegment(0));

        Document doc1 = new Document("test case auto");
        for (int i=0; i<InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD; i++){
            iim.addDocument(doc1);
        }
        iim.addDocument(doc1);

        iim.flush();
        iim.flush();

        expectedNumSegments = 2;
        assertEquals(expectedNumSegments, iim.getNumSegments());
    }
}
