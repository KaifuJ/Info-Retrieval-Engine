package edu.uci.ics.cs221.index.inverted;

import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.WordBreakTokenizer;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

public class Team9KeywordSearchTest {

    private ComposableAnalyzer analyzer;
    private String indexFolder;
    private InvertedIndexManager invertedIndex;

    // Initial Document
    private Document Doc1;
    private Document Doc2;
    private Document Doc3;
    private Document Doc4;

    @Before
    public void setUp() {
        analyzer = new ComposableAnalyzer( new WordBreakTokenizer(), new PorterStemmer());
        indexFolder = "./index/Team9KeywordSearchTest/";
        invertedIndex = InvertedIndexManager.createOrOpen(indexFolder, analyzer);

        Doc1 = new Document("catdog");
        Doc2 = new Document("dogbird");
        // original was "catbird", but there's actually a kind of bird called "catbird", so it's not "cat bird"
        Doc3 = new Document("catpig");
        Doc4 = new Document("cat");
    }

    @After
    public void tearDown() {
        // local storage folder is a flat folder which doesn't contain sub folder
        // In case of any exception, that will be thrown out.
        File localStorageFolder = new File(indexFolder);
        for (File file : localStorageFolder.listFiles()) {
            file.delete();
        }
        localStorageFolder.delete();
    }

    // Normal search test
    // Search cat return doc1 doc3 and doc4
    @Test
    public void test1() {
        invertedIndex.addDocument(Doc1);
        invertedIndex.addDocument(Doc2);

        invertedIndex.addDocument(Doc3);
        invertedIndex.addDocument(Doc4);
        invertedIndex.flush();

        Iterator<Document> actualDoc = invertedIndex.searchQuery("cat");
        List<Document> expected = Arrays.asList(Doc1, Doc3, Doc4);

        for (Document expectedDoc : expected) {
            assertNotNull(actualDoc);
            assertEquals(expectedDoc.getText(), actualDoc.next().getText());
        }
        assertFalse(actualDoc.hasNext());
    }

    // add 4 documents and flush 2 times, then we have two segments in disk
    // Keyword search after calling mergeAllSegments()
    @Test
    public void test2() {
        invertedIndex.addDocument(Doc1);
        invertedIndex.addDocument(Doc2);
        invertedIndex.flush();

        invertedIndex.addDocument(Doc3);
        invertedIndex.addDocument(Doc4);
        invertedIndex.flush();

        invertedIndex.mergeAllSegments();

        Iterator<Document> actualDoc = invertedIndex.searchQuery("cat");
        List<Document> expected = Arrays.asList(Doc1, Doc3, Doc4);


        for (Document expectedDoc : expected) {
            assertNotNull(actualDoc);
            assertEquals(actualDoc.next().getText(), expectedDoc.getText());
        }
        assertFalse(actualDoc.hasNext());
    }

    // edge case, when no segment is generated, search result should be empty document iterator
    @Test
    public void test3() {
        Iterator<Document> actualDoc = invertedIndex.searchQuery("cat");
        assertFalse(actualDoc.hasNext());
    }

}