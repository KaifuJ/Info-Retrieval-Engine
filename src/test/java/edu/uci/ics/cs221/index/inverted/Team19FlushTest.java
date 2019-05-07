
package edu.uci.ics.cs221.index.inverted;

import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.InvertedIndexSegmentForTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.storage.Document;

public class Team19FlushTest {

    Analyzer an = new ComposableAnalyzer(new PunctuationTokenizer(), token -> token);
    InvertedIndexManager iim;
    String file = "./index/Team19FlushTest/";

    @Before
    public void setup() throws Exception {
        iim = iim.createOrOpen(file, an);
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

    // test flush when flush() is called by user, whether the total number of segments is correct
    // and whether the first disk segment is set correctly.
    @Test
    public void testFlush1() throws Exception {
        iim.addDocument(new Document("cat dog"));
        iim.addDocument(new Document("cat elephant"));
        iim.flush();
        iim.addDocument(new Document("cat dog"));
        iim.addDocument(new Document("wolf dog"));
        iim.flush();
        assertEquals(2, iim.getNumSegments());
        Map<String, List<Integer>> PostingList = new HashMap<>();
        List<Integer> l1 = new LinkedList<>();
        l1.add(0);
        l1.add(1);
        PostingList.put("cat", l1);
        List<Integer> l2 = new LinkedList<>();
        l2.add(0);
        PostingList.put("dog", l2);
        List<Integer> l3 = new LinkedList<>();
        l3.add(1);
        PostingList.put("elephant", l3);
        Map<Integer, Document> DocStore = new HashMap<>();
        DocStore.put(0, new Document("cat dog"));
        DocStore.put(1, new Document("cat elephant"));
        InvertedIndexSegmentForTest test = iim.getIndexSegment(0);
        assertEquals(PostingList, test.getInvertedLists());
        assertEquals(DocStore, test.getDocuments());
    }

    // test flush when flush() is called automatically when number of documents equals DEFAULT_FLUSH_THRESHOLD,
    // whether the total number of segments is correct and whether documentIterator is set correctly.
    @Test
    public void testFlush2() throws Exception {
        Document d1 = new Document("Information retrieval is the activity of obtaining information system resources relevant to an information need from a collection");
        Document d2 = new Document("Searches can be based on full-text or other content-based indexing");
        Document d3 = new Document("The process may then be iterated if the user wishes to refine the query");
        Document d4 = new Document("Web search engines are the most visible IR applications");
        iim.addDocument(d1);
        iim.addDocument(d2);
        iim.addDocument(d3);
        iim.addDocument(d4);
        iim.flush();
        assertEquals(2, iim.getNumSegments());
        Set<Document> result = new HashSet<>();
        Set<Document> expected = new HashSet<>();
        expected.add(d1);
        expected.add(d2);
        expected.add(d3);
        expected.add(d4);
        Iterator<Document> documentIterator = iim.documentIterator();
        while(documentIterator.hasNext()) {
            result.add(documentIterator.next());
        }
        assertEquals(result, expected);
    }

    // test when flush() is called, if the second segment have a correct inverted index
    // and if the total number of segments and documentIterator match
    @Test
    public void testFlush3() throws Exception {
        String s1 = "fox lion tiger puma panther";
        String s2 = "lion dog eagle giraffe fox";
        String s3 = "budget director film movie actor";
        String s4 = "theater actor director budget popcorn";
        Map<String, List<Integer>> postingList = new HashMap<>();
        List<Integer> l = new LinkedList<>();
        l.add(0);
        l.add(1);
        postingList.put("actor", l);
        postingList.put("budget", l);
        postingList.put("director", l);
        l = new LinkedList<>();
        l.add(0);
        postingList.put("film", l);
        postingList.put("movie", l);
        l = new LinkedList<>();
        l.add(1);
        postingList.put("popcorn", l);
        postingList.put("theater", l);
        String s5 = "pizza burger pasta salad sandwich";
        String s6 = "salad sandwich noodle rice pasta";
        Document d1 = new Document(s1);
        Document d2 = new Document(s2);
        Document d3 = new Document(s3);
        Document d4 = new Document(s4);
        Document d5 = new Document(s5);
        Document d6 = new Document(s6);
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
        InvertedIndexSegmentForTest num = iim.getIndexSegment(1);
        assertEquals(postingList, num.getInvertedLists());
        Set<Document> result = new HashSet<>();
        Set<Document> expected = new HashSet<>();
        expected.add(d1);
        expected.add(d2);
        expected.add(d3);
        expected.add(d4);
        expected.add(d5);
        expected.add(d6);
        Iterator<Document> documentIterator = iim.documentIterator();
        while(documentIterator.hasNext()) {
            result.add(documentIterator.next());
        }
        assertEquals(result, expected);
    }
    
}
