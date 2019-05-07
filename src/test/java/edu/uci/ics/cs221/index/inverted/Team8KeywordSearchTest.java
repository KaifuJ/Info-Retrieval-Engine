package edu.uci.ics.cs221.index.inverted;

import edu.uci.ics.cs221.analysis.*;

import edu.uci.ics.cs221.storage.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class Team8KeywordSearchTest {

    //generate docs
    private Document doc1 = new Document("cat and dog");
    private Document doc2 = new Document("cat and fish");
    private Document doc3 = new Document("fish and dog");

    private String pathname = "./index/Team8KeywordSearchTest";

    private InvertedIndexManager indexManager = null;

    @Before
    public void initial() throws Exception {
        //initialize an InvertedIndexManager
        Analyzer analyzer = new NaiveAnalyzer();
        this.indexManager = InvertedIndexManager.createOrOpen(pathname, analyzer);
        //add doc
        indexManager.addDocument(doc1);
        indexManager.addDocument(doc2);
        indexManager.addDocument(doc3);
        //flush to disk
        indexManager.flush();
    }

    //test query single word "cat", the result should be doc1 and doc2
    @Test
    public void test1() throws Exception {
        String query = "cat";

        List<Document> expected = Arrays.asList(doc1,doc2);
        Iterator<Document> results = indexManager.searchQuery(query);
        int count = 0;
        while(results.hasNext()){
            assertEquals(results.next().getText(), expected.get(count++).getText());
        }
        assertEquals(count, expected.size());
    }
    //test empty query word, the result should be empty
    @Test
    public void test2() throws Exception {
        String query = "";

        Iterator<Document> results = indexManager.searchQuery(query);
        assertFalse(results.hasNext());

    }

    //test when query word is not in inverted list
    @Test
    public void test3() throws Exception {
        String query = "elephant";

        Iterator<Document> results = indexManager.searchQuery(query);
        assertFalse(results.hasNext());

    }

    @After
    public void delete() throws Exception {
        File files = new File(pathname);
        for (File file: files.listFiles()){
            if (!file.isDirectory()){
                file.delete();
            }
        }
        files.delete();
    }
}
