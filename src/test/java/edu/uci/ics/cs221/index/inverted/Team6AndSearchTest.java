package edu.uci.ics.cs221.index.inverted;
import edu.uci.ics.cs221.analysis.*;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.print.Doc;
import java.io.File;
import java.util.*;

import static org.junit.Assert.assertEquals;

/*
- What's your team number?
Team 6

- What is the functionality being tested?
Boolean AND Search

- Describe your tests briefly:
Test case 1 is to test for a list of normal input document can AndSearch give the correct output.
Test case 2 is to give an empty input, and empty search words list can AndSearch gives an empty output.
Test case 3 is the test to search a larger word list. The result should return the correct documents
which contains that word list.
Test case 4 is the test to if that is not such element and iterator.next() callls, the program should
generate NoSuchElementException.

- Does each test case have comments/documentation?
Yes


 */

public class Team6AndSearchTest {

    private final String path = "./index/Team6AndSearchTest";

    private InvertedIndexManager manager = null;
    private Document doc1 = new Document("dog, bone and fishes");
    private Document doc2 = new Document("cats, fishes and dogs");
    private Document doc3 = new Document("fishes, birds and sky");
    private Document doc4 = new Document("cats, bones and something");
    private Document doc5 = new Document("Apple is the name of a dog and it is also the name of a tree.");
    private Document doc6 = new Document("The name of a dog is apple which is also a name of a tree.");
    private Document doc7 = new Document("Apple trees will have fruit once a year.");
    private Document doc8 = new Document("What is the name of that dog. Is it apple?");


    @Before
    public void before() {
    // Initialize analyzer
        Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());

        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        // Initialize InvertedIndexManager
        this.manager = InvertedIndexManager.createOrOpen(path, analyzer);
        manager.addDocument(doc1);
        manager.addDocument(doc2);
        manager.addDocument(doc3);
        manager.addDocument(doc4);
        manager.addDocument(doc5);
        manager.addDocument(doc6);
        manager.addDocument(doc7);
        manager.addDocument(doc8);
        // Flush to disk
        manager.flush();
    }



    @Test
    public void test1() {
        /*
        Test case 1 is to test for a list of normal input document
        Can AndSearch give the correct output
        * */

        Set<Document> expectedDocs = new HashSet<Document>();
        expectedDocs.add(doc4);

        List<String> keyWords = Arrays.asList("cat", "bone");
        Iterator<Document> ite = manager.searchAndQuery(keyWords);

        while(ite.hasNext()){
            Document cur = ite.next();
            if(expectedDocs.contains(cur))
            {
                expectedDocs.remove(cur);
            }
            else
            {
                break;
            }
        }
        assertEquals(true, expectedDocs.size() == 0);


    }

    @Test
    public void test2() {
        /*
        test case 2 is to give an empty input, and empty search words list
        Can AndSearch gives an empty output
        */

        List<String> keyWords = Arrays.asList("");
        Iterator<Document> ite = manager.searchAndQuery(keyWords);

        assertEquals(false, ite.hasNext());
    }

    @Test
    public void test3() {
        /*
        Test case 3 is the test to search a larger word list. The result should return the correct
        documents which contains that word list.
         */

        Set<Document> expectedDocs = new HashSet<Document>();
        expectedDocs.add(doc5);
        expectedDocs.add(doc6);

        List<String> keyWords = Arrays.asList("dog", "tree", "appl");
        Iterator<Document> ite = manager.searchAndQuery(keyWords);

        while(ite.hasNext()){
            Document cur = ite.next();
            if(expectedDocs.contains(cur))
            {
                expectedDocs.remove(cur);
            }
            else
            {
                break;
            }
        }
        assertEquals(true, expectedDocs.size() == 0);

    }

    @Test(expected = NoSuchElementException.class)
    public void test4() {
        /*
        Test case 4 is the test to if that is not such element and iterator.next() calls, the program should
        generate NoSuchElementException.
         */

        List<String> keyWords = Arrays.asList("cat", "tree", "apple");

        Iterator<Document> ite = manager.searchAndQuery(keyWords);

        ite.next();
    }

    @After
    public void after() {
        File cacheFolder = new File(path);
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
