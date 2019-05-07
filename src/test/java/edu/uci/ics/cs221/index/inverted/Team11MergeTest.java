package edu.uci.ics.cs221.index.inverted;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.InvertedIndexSegmentForTest;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.io.File;

public class Team11MergeTest {
    String indexPath = "./index/Team11MergeTest";

    Document[] documents = new Document[] {
            new Document("import edu uci ics cs221 analysis  Analyzer"),
            new Document("import edu uci ics cs221 analysis  ComposableAnalyzer"),
            new Document("import edu uci ics cs221 analysis  PorterStemmer"),
            new Document("import edu uci ics cs221 analysis  PunctuationTokenizer"),
            new Document("import edu uci ics cs221 index     inverted            InvertedIndexManager"),
            new Document("import edu uci ics cs221 storage   Document")
    };

    Analyzer analyzer;
    InvertedIndexManager index;

    @Before
    public void before() {
        analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        index = InvertedIndexManager.createOrOpen(indexPath, analyzer);
    }

    @After
    public void clean() {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 8;

        try {
            // When deleting a folder in Java, all the files inside the folder needs to be deleted already
            // Reference: https://stackoverflow.com/questions/20281835/how-to-delete-a-folder-with-files-using-java
            File folder = new File(indexPath);
            String[] entries = folder.list();
            for(String s: entries) {
                File currentFile = new File(folder.getPath(),s);
                currentFile.delete();
            }

            if (folder.delete()) {
                System.out.println("Folder deleted successfully");
            } else {
                System.out.println("Failed to delete the folder");
            }
        } catch (Exception e) {
            System.out.println("Something went wrong when deleting file");
        }
    }

    /* Test an easy case of flush threshold=1 and merge threshold=2,
     * after we add 2 documents, the number of segments after merging
     * should be 1
     */
    @Test
    public void mergeSearchTest1() {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 2;

        index.addDocument(documents[0]);
        index.addDocument(documents[1]);
        int expectedNumSegments = 1;
        assertEquals(expectedNumSegments, index.getNumSegments());
    }

    @Test
    public void mergeSearchTest2() {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;

        for (int i = 0; i < 4 ; i++){
            index.addDocument(documents[i]);
        }
        index.mergeAllSegments();
        int expectedNumSegments = 2;
        assertEquals(expectedNumSegments, index.getNumSegments());
    }


    /* Test when the flush and merge thresholds are 1 and 4 respectively,
     * after merging the number of segments should be 3 and we also check
     * the detailed docIDs of the keywords
     */
    @Test
    public void mergeSearchTest3() {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 4;

        for (Document doc : documents) {
            index.addDocument(doc);
        }

        int expectedNumSegments = 2;
        assertEquals(expectedNumSegments, index.getNumSegments());
        InvertedIndexSegmentForTest it = index.getIndexSegment(0);
        Map<String, List<Integer>> invertedLists = it.getInvertedLists();
        List<Integer> docIds = invertedLists.get("import");
        List<Integer> expectedDocIds = Arrays.asList(0, 1, 2, 3);
        assertEquals(expectedDocIds, docIds);
    }
}
