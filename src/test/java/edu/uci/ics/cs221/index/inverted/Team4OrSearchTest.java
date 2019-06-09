package edu.uci.ics.cs221.index.inverted;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.NaiveAnalyzer;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class Team4OrSearchTest {
    private final String FOLDER = "./index/Team4OrSearchTest";
    private InvertedIndexManager manager = null;
    // Initialize document
    private Document doc1 = new Document("cat dog cat dog");
    private Document doc2 = new Document("apple dog");
    private Document doc3 = new Document("cat smile");

    @Before
    public void before() {
        // Initialize analyzer
        Analyzer analyzer = new NaiveAnalyzer();
        // Initialize InvertedIndexManager
        this.manager = InvertedIndexManager.createOrOpen(FOLDER, analyzer);
        manager.addDocument(doc1);
        manager.addDocument(doc2);
        manager.addDocument(doc3);
        // Flush to disk
        manager.flush();
    }
    /**
     * Test 1:
     * Test for normal search or case
     * This test case is going to search "cat" or "apple"
     * The result should be doc1, doc2, doc3
     */
    @Test
    public void test1() {
        // Generate expected list
        List<Document> expected = Arrays.asList(doc1, doc2, doc3);

        // Generate keywords
        List<String> keywords = Arrays.asList("cat", "apple");

        // Make query
        Iterator<Document> results = manager.searchOrQuery(keywords);

        // Assertion
        for (int i = 0; results.hasNext(); i++) {
            assertEquals(results.next().getText(), expected.get(i).getText());
        }
    }

    /**
     * Test 2:
     * Test for empty keyword
     * Result should be an empty list of Documents
     */
    @Test
    public void test2() {
        // Generate keywords
        List<String> keywords = Arrays.asList("");

        // Make query
        Iterator<Document> results = manager.searchOrQuery(keywords);

        // Assertion
        assertFalse(results.hasNext());
    }

    /**
     * Test 3:
     * Test for punctuation characters
     * Results should be an empty list of Documents
     */
    @Test
    public void test3() {
        // Generate keywords
        List<String> keywords = Arrays.asList(",", ":./");

        // Make query
        Iterator<Document> results = manager.searchOrQuery(keywords);

        // Assertion
        assertFalse(results.hasNext());
    }

    /**
     * Clean up the cache files
     */
    @After
    public void after() {
        File cacheFolder = new File(FOLDER);
        for (File file : cacheFolder.listFiles()) {
            try {
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cacheFolder.delete();
    }
}