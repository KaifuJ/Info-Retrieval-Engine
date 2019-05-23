package edu.uci.ics.cs221.index.positional;

import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.NaiveCompressor;
import edu.uci.ics.cs221.index.inverted.PageFileChannel;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static junit.framework.TestCase.assertTrue;

public class Team4IndexCompressionTest {

    Document doc1 = new Document("dog");
    Document doc2 = new Document(String.join(" ", Collections.nCopies(4096, "cat")));

    private static final String indexFolder = "./index/Team4IndexCompressionTest/";

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
     * Test 1
     * Add 4096 docs to see if inverted list becomes smaller.
     *
     * InvertedList File:
     *      before: "dog" -> DocId [0, 1, 2, 3, 4, 5,...., 1000] with pointers  (4 + 4)B * 4096
     *      after: "dog" -> DocId [0, 1, 1, 1, 1, 1, 1, 1, ...1]with pointers,  (1 + 4)B * 4096
     * Positional List:
     *      before: 0 -> [0]
     *              1 -> [0]
     *              ...     4096 position lists
     *     after: same contents. but var length changed.
     */
    @Test
    public void test1(){

        // make sure the data are stored in one file
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 4096;

        PageFileChannel.resetCounters();
        for(int i = 0; i < 4096; i++)
            this.naiveIndexManager.addDocument(doc1);
        this.naiveIndexManager.flush();
        int naiveCount = PageFileChannel.writeCounter;

        PageFileChannel.resetCounters();
        for(int i = 0; i < 4096; i++)
            this.dvlIndexManager.addDocument(doc1);
        this.dvlIndexManager.flush();
        int dvlCount = PageFileChannel.writeCounter;

        assertTrue(naiveCount/(double)dvlCount > 2);
    }

    /** Test 2
     * Add a document with 4096 "cat", test if posting
     *
     * InvertedList file:
     *      1 word("cat") with 1 docID(0)
     *         - write page are same for each compressor: 1 page word + 1 page posting list.
     * PositionList compression:
     *      1 list of 4096 integers, gap and length are all compressed.
     *         - naive posting list: [0, 1, 2, ... 4095] -> 4B * 4096 -> 4 pages
     *         - compressed posting list: [0, 1, 1, 1, .... 1] ->  1B * 4096 -> 1 page
     */
    @Test
    public void test2(){
        // Naive Compressor
        PageFileChannel.resetCounters();
        this.naiveIndexManager.addDocument(doc2);
        this.naiveIndexManager.flush();
        int naiveCount = PageFileChannel.writeCounter;

        // DeltaVarLen Compressor
        PageFileChannel.resetCounters();
        this.dvlIndexManager.addDocument(doc2);
        this.dvlIndexManager.flush();
        int dvlCount = PageFileChannel.writeCounter;

        assertTrue(naiveCount/(double)dvlCount < 4);
        assertTrue(naiveCount/(double)dvlCount > 1.5);
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
