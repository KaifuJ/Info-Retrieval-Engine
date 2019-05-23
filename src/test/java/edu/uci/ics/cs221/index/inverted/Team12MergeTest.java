

//check forceful merge
//check content of segment if it has both lists
//stop merge in middle of execution and see if merged segment has some content

package edu.uci.ics.cs221.index.inverted;


import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.storage.Document;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.io.File;

public class Team12MergeTest {

    PunctuationTokenizer tokenizer = new PunctuationTokenizer();
    PorterStemmer porterStemmer = new PorterStemmer();

    Analyzer analyzer = new ComposableAnalyzer(tokenizer, porterStemmer);

    InvertedIndexManager iim;

    /**
     * This testcase verifies the below:
     * that we can forcefully merge the segments
     * even before the number of segments reaches the threshold
     * threshold is not changed set to default 8
     */
    @Test
    public void test1()
    {

        iim.addDocument(new Document("Implement LSM-like disk-based inverted index that supports insertions"));
        iim.addDocument(new Document("Implement merge of inverted index segments"));
        iim.addDocument(new Document("Implement keyword search, boolean AND search, and boolean OR search"));
        iim.addDocument(new Document("(Optional Extra Credit): Implement deletions"));

        iim.mergeAllSegments();

        assert iim.getNumSegments() == 1;

    }

    /**
     * At this test case, we verify:
     * 1. We check whether the final number of segment equals to 2. The steps are illustrated as follow:
     * add doc1, doc2, doc3, doc4 -> total 4 seqments, merge to 2 segments
     * add doc5, doc6 -> total 4 seqments, merge to 2 segments
     * add doc7, doc8 -> total 4 seqments, merge to 2 segments
     */
    @Test
    public void Test2() {
        iim.DEFAULT_MERGE_THRESHOLD = 4;

        iim.addDocument(new Document("In this project"));
        iim.addDocument(new Document("you'll be implementing a disk-based inverted index and the search operations."));
        iim.addDocument(new Document("At a high level, inverted index stores a mapping from keywords to the ids of documents they appear in."));
        iim.addDocument(new Document("A simple in-memory structure could be"));
        iim.addDocument(new Document("where each key is a keyword token"));
        iim.addDocument(new Document("and each value is a list of document IDs"));
        iim.addDocument(new Document("In this project, the disk-based index structure is based on the idea of LSM"));
        iim.addDocument(new Document("Its main idea is the following"));

        assert iim.getNumSegments() == 2;
    }

    @Before
    public void init() {
        iim = InvertedIndexManager.createOrOpen("./index/Team12MergeTest/", analyzer);
        iim.DEFAULT_FLUSH_THRESHOLD = 1;
    }

    @After
    public void cleanup() {
        File p = new File("./index/Team12MergeTest/");
        String[] entries = p.list();
        for (int i = 0; i < entries.length; ++i) {
            File currentFile = new File(p.getPath(), entries[i]);
            currentFile.delete();
        }
        p.delete();
    }

}