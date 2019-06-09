package edu.uci.ics.cs221.index.ranking;

import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.index.inverted.NaiveCompressor;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static junit.framework.TestCase.assertEquals;

public class Team4TfIdfTest {

    Document doc1 = new Document("dog cat fish apple banana");
    Document doc2 = new Document("dog apple banana fish");
    Document doc3 = new Document("fish apple");

    private static final String indexFolder = "./index/Team4TfIdfTest/";

    NaiveCompressor naiveCompressor = null;
    DeltaVarLenCompressor deltaVarLenCompressor = null;
    InvertedIndexManager naiveIndexManager = null;
    InvertedIndexManager dvlIndexManager = null;


    @Before
    public void init(){
        this.naiveCompressor = new NaiveCompressor();
        this.deltaVarLenCompressor = new DeltaVarLenCompressor();

        this.naiveIndexManager = InvertedIndexManager.createOrOpenPositional(indexFolder +"naive/", new ComposableAnalyzer(new PunctuationTokenizer(),new PorterStemmer()),naiveCompressor);
        this.dvlIndexManager = InvertedIndexManager.createOrOpenPositional(indexFolder +"dvl/", new ComposableAnalyzer(new PunctuationTokenizer(),new PorterStemmer()),deltaVarLenCompressor);
    }

    /**
     * Test for DeltaVarLen compressor
     * Add document 1: dog cat fish apple banana
     * Add document 2: dog apple banana fish
     * Add document 3: fish apple
     */
    @Test
    public void test1(){
        // Add documents
        this.dvlIndexManager.addDocument(doc1);
        this.dvlIndexManager.addDocument(doc2);
        this.dvlIndexManager.addDocument(doc3);
        // Do flush operation
        this.dvlIndexManager.flush();

        assertEquals(this.dvlIndexManager.getDocumentFrequency(0, "dog"), 2);
        assertEquals(this.dvlIndexManager.getDocumentFrequency(0, "appl"), 3);
        assertEquals(this.dvlIndexManager.getDocumentFrequency(0, "people"), 0);
    }

    /** Test 2
     * Test for Naive compressor
     * Add document 1: dog cat fish apple banana
     * Add document 2: dog apple banana fish
     * Add document 3: fish apple
     */
    @Test
    public void test2(){
        // Add documents
        this.naiveIndexManager.addDocument(doc1);
        this.naiveIndexManager.addDocument(doc2);
        this.naiveIndexManager.addDocument(doc3);
        // Do flush operation
        this.naiveIndexManager.flush();

        assertEquals(this.naiveIndexManager.getDocumentFrequency(0, "dog"), 2);
        assertEquals(this.naiveIndexManager.getDocumentFrequency(0, "appl"), 3);
        assertEquals(this.naiveIndexManager.getDocumentFrequency(0, "people"), 0);
    }

    @After
    public void after(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;

        File cacheFolder = new File(indexFolder + "naive/");
        for (File file : cacheFolder.listFiles()) {
            try {
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cacheFolder.delete();

        cacheFolder = new File(indexFolder + "dvl/");
        for (File file : cacheFolder.listFiles()) {
            try {
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cacheFolder.delete();
        new File(indexFolder).delete();
    }

}
