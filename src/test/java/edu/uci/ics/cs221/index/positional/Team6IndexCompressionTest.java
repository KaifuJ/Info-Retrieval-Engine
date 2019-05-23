package edu.uci.ics.cs221.index.positional;
import edu.uci.ics.cs221.analysis.*;
import edu.uci.ics.cs221.index.inverted.*;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.print.Doc;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.security.cert.TrustAnchor;
import java.util.*;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/*
- What's your team number?
    Team 6
- What is the functionality being tested?
    The effect of Delta-compression in building positional index
    Method:
        compare the IO counters between non-compression and compression
- Describe your tests briefly:
    To compare the difference, we need to set up two different index managers.
    In test1:
        We test while writing documents to disk, the difference between IO counters
    In test2:
        We test while searching for phrase in document, the difference between IO counters
- Does each test case have comments/documentation?
    Yes
 */

public class Team6IndexCompressionTest {

    private final String path = "./index/Team6AndSearchTest1";
    private final String path2 = "./index/Team6AndSearchTest2";
    private final String docPath = "https://grape.ics.uci.edu/wiki/public/raw-attachment/wiki/cs221-2019-spring-project2/Team2StressTest.txt";

    private InvertedIndexManager nonCompressManager = null;
    private InvertedIndexManager compressManager = null;
    private Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
    Document sampleDoc = generateDoc();
    Document emptyDoc = new Document(" ");

    //initialize a set of counters to record IO performance
    double nonCompressReadCounter = 0;
    double nonCompressWriteCounter = 0;
    double compressReadCounter = 0;
    double compressWriteCounter = 0;


    @Before
    public void before() {
        // Initialize InvertedIndexManager
        this.nonCompressManager = createPositionalManager(path, analyzer, false);
        this.compressManager = createPositionalManager(path2, analyzer, true);
    }

    /**
     *  input documents 10 times into each manager, compare the reader counters
     *  We assume the compressed one will be 2/3 smaller than non-compressed one
     *  search the phase and compare read counters
     *  We assume the compressed one will be 2/3 smaller than non-compressed one
     * */

    @Test
    public void test1() {
        //naive compressor manager add document
        for(int i = 0; i<100; i++)
            nonCompressManager.addDocument(sampleDoc);
        nonCompressManager.flush();
        nonCompressWriteCounter = PageFileChannel.writeCounter;
        PageFileChannel.resetCounters();

        //Compress manager add documents
        for(int i = 0; i<100; i++)
            compressManager.addDocument(sampleDoc);
        compressManager.flush();
        compressWriteCounter = PageFileChannel.writeCounter;
        PageFileChannel.resetCounters();

        //search for phase, test the differences between read counters
        List<String> keywords = new ArrayList<>();
        keywords.add("Pride ");
        keywords.add("Prejudice");

        //searching the keywords using two managers for multiple times
        for(int i = 0; i<10; i++)
            nonCompressManager.searchPhraseQuery(keywords);
        nonCompressReadCounter = PageFileChannel.readCounter;
        PageFileChannel.resetCounters();

        for(int i = 0; i<10; i++)
            compressManager.searchPhraseQuery(keywords);
        compressReadCounter = PageFileChannel.readCounter;
        PageFileChannel.resetCounters();

        assertEquals(true, compressWriteCounter/nonCompressWriteCounter < (double)2/3);


    }

    /**
     This testcase is to test the empty input document. The write counter of
     compressed and non-compressed should be the same number.
     * */

    @Test
    public void test2() {
        nonCompressManager.addDocument(emptyDoc);
        nonCompressManager.flush();
        nonCompressWriteCounter = PageFileChannel.writeCounter;
        PageFileChannel.resetCounters();

        //Compress manager add documents
        compressManager.addDocument(emptyDoc);
        compressManager.flush();
        compressWriteCounter = PageFileChannel.writeCounter;
        PageFileChannel.resetCounters();

        assertEquals(true,compressWriteCounter == nonCompressWriteCounter);

    }


    @After
    public void after() {
        deleteDirectory(path);
        deleteDirectory(path2);
    }

    /**
     * Create Positional inverted index using different compressions
     *
     * @param ifCompress choose whether using Delta-compression
     * @return Inverted index managers
     * */

    private InvertedIndexManager createPositionalManager(String path, Analyzer analyzer, boolean ifCompress){
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if(ifCompress)
            return InvertedIndexManager.createOrOpenPositional(path, analyzer, new DeltaVarLenCompressor());
        else
            return InvertedIndexManager.createOrOpenPositional(path, analyzer, new NaiveCompressor());
    }

    /**
     * Reading text from online resource and generate document
     *
     * @return document
     * */
    private Document generateDoc(){
        String text = new String();
        try {
            URL docURL = new URL(docPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(docURL.openStream()));
            StringBuilder strbuilder = new StringBuilder();
            String inputStr;
            while((inputStr = reader.readLine()) != null){
                strbuilder.append(inputStr + "\n");
            }
            reader.close();
            text = strbuilder.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new Document(text);
    }

    private void deleteDirectory(String path){
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