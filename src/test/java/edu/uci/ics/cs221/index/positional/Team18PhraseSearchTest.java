package edu.uci.ics.cs221.index.positional;

import com.google.common.collect.Table;
import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.PositionalIndexSegmentForTest;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class Team18PhraseSearchTest {
    InvertedIndexManager manager;
    String folderPath = "./index/Team18PhraseSearchTest";
    Document[] documents1 = new Document[]{
            new Document("this is a test case"),
            new Document("Even though I devoted quantities of time to the test, cases appeared to be like they were rarely covered in class"),
            new Document("I had a test last Friday which is the hardest case I've ever experienced"),
            new Document("test of the case")
    };
    Document[] documents2 = new Document[]{
            new Document("I visited new york city last summer."),
            new Document("New York is a popular travelling destination and a city with a huge population."),
            new Document("Before the new year's day, mom made some dishes with york, and we will drive to the city tomorrow.")
    };

    @Before
    public void initialize(){
        manager = InvertedIndexManager.createOrOpenPositional(folderPath, new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()), new DeltaVarLenCompressor());

    }

    @After
    public void clear(){
        File dir = new File("./index/Team18PhraseSearchTest");
        for (File file: dir.listFiles()){
            if (!file.isDirectory()){
                file.delete();
            }
        }
        dir.delete();
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 8;
    }

    // test if the basic phrase query has been implemented successfully
    // test if there are stop words between two consecutive words of one phrase in a document, the document will be chosen as a search result
    // test by comparing result with the correct document
    @Test
    public void test1(){
        for(Document doc : documents1){
            manager.addDocument(doc);
        }
        manager.flush();
        Iterator<Document> it = manager.searchPhraseQuery(Arrays.asList("test", "case"));
        List<Document> res = new LinkedList<>();
        while(it.hasNext()){
            res.add(it.next());
        }
        assertEquals(3, res.size());
        assertTrue(documents1[0].getText().equals(res.get(0).getText()));
        assertTrue(documents1[1].getText().equals(res.get(1).getText()));
        assertTrue(documents1[3].getText().equals(res.get(2).getText()));
    }

    // test if phrase search applies to cases with more or equal than 3 words in a single phrase
    @Test
    public void test2(){
        for(Document doc : documents2){
            manager.addDocument(doc);
        }
        manager.flush();

        Iterator<Document> it = manager.searchPhraseQuery(Arrays.asList("new", "york", "city"));
        while(it.hasNext()){
            Document result = it.next();
            assertTrue(documents2[0].getText().equals(result.getText()));
        }

    }

    // test if search spans in multiple segments and merged segments
    @Test
    public void test3(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 2;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 4;
        for(Document doc : documents1){
            manager.addDocument(doc);
        }

        Iterator<Document> it = manager.searchPhraseQuery(Arrays.asList("test", "case"));
        List<Document> expectedResult = new LinkedList<>();
        expectedResult.add(documents1[0]);
        expectedResult.add(documents1[1]);
        expectedResult.add(documents1[3]);
        List<Document> result = new LinkedList<>();
        while(it.hasNext()){
            result.add(it.next());
        }

        assertEquals(expectedResult.size(), result.size());
        for(int i = 0; i < expectedResult.size(); i ++){
            assertTrue(expectedResult.get(i).getText().equals(result.get(i).getText()));
        }


    }




}
