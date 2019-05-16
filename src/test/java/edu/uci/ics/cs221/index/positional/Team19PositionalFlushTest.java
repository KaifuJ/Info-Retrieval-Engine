
package edu.uci.ics.cs221.index.positional;

import com.google.common.collect.Table;
import com.google.common.collect.HashBasedTable;
import edu.uci.ics.cs221.analysis.NaiveAnalyzer;
import edu.uci.ics.cs221.index.inverted.Compressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.PositionalIndexSegmentForTest;
import edu.uci.ics.cs221.index.inverted.NaiveCompressor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.storage.Document;

public class Team19PositionalFlushTest {
    Analyzer an = new NaiveAnalyzer();
    Compressor cp = new NaiveCompressor();
    InvertedIndexManager iim;
    String file = "./index/Team19PositionalFlushTest/";
    
    private Document d1 = new Document("cat dog bird");
    private Document d2 = new Document("dog wolf tiger");
    private Document d3 = new Document("cat bird elephant");
    private Document d4 = new Document("wolf tiger mouse");
    private Document d5 = new Document("cat puma mouse");
    private Document d6 = new Document("elephant cat puma");

    @Before
    public void setup() throws Exception {
        Path path = Paths.get(file);
        Files.deleteIfExists(path);
        iim = iim.createOrOpenPositional(file, an, cp);
        iim.DEFAULT_FLUSH_THRESHOLD = 3;
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

        iim.DEFAULT_FLUSH_THRESHOLD = 1000;
    }

    // test flush when flush() is called automatically, whether the total number of segments is correct
    // and whether the first disk segment is set correctly.
    @Test
    public void testPositionalFlush1() throws Exception {
        iim.addDocument(new Document("cat dog"));
        iim.addDocument(new Document("cat elephant"));
        iim.addDocument(new Document("wolf dog dog"));
        iim.addDocument(new Document("cat dog"));
        iim.flush();
        assertEquals(2, iim.getNumSegments());

        Map<String, List<Integer>> PostingList = new HashMap<>();
        PostingList.put("cat", Arrays.asList(0, 1));
        PostingList.put("dog", Arrays.asList(0, 2));
        PostingList.put("elephant", Arrays.asList(1));
        PostingList.put("wolf", Arrays.asList(2));

        Map<Integer, Document> DocStore = new HashMap<>();
        DocStore.put(0, new Document("cat dog"));
        DocStore.put(1, new Document("cat elephant"));
        DocStore.put(2, new Document("wolf dog"));

        Table<String, Integer, List<Integer>> Positions = HashBasedTable.create();
        Positions.put("cat", 0, Arrays.asList(1));
        Positions.put("cat", 1, Arrays.asList(1));
        Positions.put("dog", 0, Arrays.asList(2));
        Positions.put("dog", 2, Arrays.asList(2, 3));
        Positions.put("elephant", 1, Arrays.asList(2));
        Positions.put("wolf", 2, Arrays.asList(1));

        PositionalIndexSegmentForTest test = iim.getIndexSegmentPositional(0);
        assertEquals(PostingList, test.getInvertedLists());
        assertEquals(DocStore, test.getDocuments());
        assertEquals(Positions, test.getPositions());
    }

    // test flush() has no operation when no document is added
    @Test
    public void testPositionalFlush2(){
        iim.flush();
        assertEquals(0, iim.getNumSegments());
        assertEquals(null, iim.getIndexSegmentPositional(0));
    }
    
    // test flush() functionality as well as correct list of documnets in a sample segment
    @Test
    public void testPositionalFlush3() {
        assertEquals(0, iim.getNumSegments());
        iim.addDocument(d1);
        iim.addDocument(d2);
        iim.flush();
        assertEquals(1, iim.getNumSegments());
        PositionalIndexSegmentForTest inCase = iim.getIndexSegmentPositional(0);
        Map<Integer, Document> docs = inCase.getDocuments();
        assertFalse(docs.isEmpty());
        assertTrue(docs.containsKey(0) && docs.containsKey(1));
        assertTrue(docs.containsValue(d1) && docs.containsValue(d2));
        assertFalse(docs.size() > 2);
    }

    // after testing flush() functionality, this tests if the inverted list and the positional list are correct
    // in a sample segment
    @Test
    public void testPositionalFlush4() {
        String target = "cat";
        iim.addDocument(d1);
        iim.addDocument(d2);
        iim.flush();
        iim.addDocument(d3);
        iim.addDocument(d4);
        iim.flush();
        assertEquals(2, iim.getNumSegments());
        PositionalIndexSegmentForTest inCase = iim.getIndexSegmentPositional(1);
        List<Integer> list = inCase.getInvertedLists().get(target);
        assertFalse(list.isEmpty());
        assertFalse(list.size() > 1);
        assertEquals(0, (int) list.get(0));
        list = inCase.getPositions().get(target, 0);
        assertFalse(list.isEmpty());
        assertFalse(list.size() > 1);
        assertEquals(0, (int) list.get(0));
    }

    // tests flush() functionality, also tests two sample segments and their credibility
    @Test
    public void testPositionalFlush5() {
        iim.addDocument(d1);
        iim.addDocument(d2);
        iim.flush();
        iim.addDocument(d3);
        iim.addDocument(d4);
        iim.flush();
        iim.addDocument(d5);
        iim.addDocument(d6);
        iim.flush();
        assertEquals(3, iim.getNumSegments());
        PositionalIndexSegmentForTest inCase = iim.getIndexSegmentPositional(2);
        Collection<Document> docs = inCase.getDocuments().values();
        assertTrue(docs.contains(d5) && docs.contains(d6));
        String target = "cat";
        List<Integer> list = inCase.getPositions().get(target, 0);
        assertFalse(list.isEmpty());
        assertFalse(list.size() > 1);
        assertTrue(list.contains(0));
        list = inCase.getPositions().get(target, 1);
        assertFalse(list.isEmpty());
        assertFalse(list.size() > 1);
        assertTrue(list.contains(1));
    }
}
