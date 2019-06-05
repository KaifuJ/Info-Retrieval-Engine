package edu.uci.ics.cs221.index.ranking;
import edu.uci.ics.cs221.analysis.*;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Team6DocumentFrequencyTest {

    private final String path = "./index/Team6AndSearchTest";
    Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());

    private InvertedIndexManager manager = null;
    private Document doc1 = new Document("dog, bone and fishes");
    private Document doc2 = new Document("cats, fishes and dogs");
    private Document doc3 = new Document("fishes, birds and sky dog");
    private Document doc4 = new Document("cats, bones and something");
    private Document doc5 = new Document("Apple is the name of a dog and it is also the name of a tree.");
    private Document doc6 = new Document("The name of a dog is apple which is also a name of a tree.");
    private Document doc7 = new Document("Apple trees will have fruit once a year.");
    private Document doc8 = new Document("What is the name of that dog. Is it apple?");

    @Before
    public void before() {
        manager = InvertedIndexManager.createOrOpenPositional(path, analyzer, new DeltaVarLenCompressor());
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        // Initialize InvertedIndexManager
        manager.addDocument(doc1);
        manager.addDocument(doc2);
        manager.addDocument(doc3);
        manager.flush();
        manager.addDocument(doc4);
        manager.addDocument(doc5);
        manager.addDocument(doc6);
        manager.flush();
        manager.addDocument(doc7);
        manager.addDocument(doc8);
        manager.flush();
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

    /**
     * Test a series of word across different segment, including words which don't exist
     * */

    @Test
    public void test1() {
        List<Integer> expectedList = new ArrayList<>();
        expectedList.add(3);
        expectedList.add(2);
        expectedList.add(0);
        List<Integer> resultList = new ArrayList<>();
        resultList.add(manager.getDocumentFrequency(0, "dog"));
        resultList.add(manager.getDocumentFrequency(1, "tree"));
        resultList.add(manager.getDocumentFrequency(2, "cat"));
        assertEquals(expectedList, resultList);

    }

    /**
     * Test if the sum of frequency of one specific word is accurate before and after merge
     * */
    @Test
    public void test2() {
        int freqSum1 = 0;
        for(int i = 0; i < manager.getNumSegments(); i++){
            freqSum1 += manager.getDocumentFrequency(i, "dog");
        }
        manager.mergeAllSegments();

        int freqSum2 = 0;
        for(int i = 0; i < manager.getNumSegments(); i++){
            freqSum2 += manager.getDocumentFrequency(i, "dog");
        }

        assertEquals(6, freqSum1);
        assertEquals(6, freqSum2);
    }


}
