package edu.uci.ics.cs221.index.positional;

import com.google.common.collect.Table;
import edu.uci.ics.cs221.analysis.*;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.index.inverted.NaiveCompressor;
import edu.uci.ics.cs221.storage.Document;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.PositionalIndexSegmentForTest;
import org.junit.*;
import org.junit.rules.Timeout;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class Team10PositionalStressTest {

    @ClassRule
    public static Timeout classTimeout = Timeout.seconds(600);

    private static String path = "./index/Team10PositionalStressTest";
    // original document from http://www.gutenberg.org/cache/epub/4276/pg4276.txt
    private static String bookurl = "https://grape.ics.uci.edu/wiki/public/raw-attachment/wiki/cs221-2019-spring-project3/Team10PositionalStressTest.txt";
    private static InvertedIndexManager iim;
    private static List<String> docs;

    private static int oldFlushThreshold = InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD;
    private static int oldMergeThreshold = InvertedIndexManager.DEFAULT_MERGE_THRESHOLD;

    private static PorterStemmer stemmer = new PorterStemmer();

    @BeforeClass
    public static void init() throws Exception {
        iim = InvertedIndexManager.createOrOpenPositional(path, 
                                new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()),
                                new DeltaVarLenCompressor());

        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 500;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 20;
        docs = new ArrayList<>();

        java.net.URL url = new URL(bookurl);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("\uFEFF")) {
                line = line.substring(1);
            }
            docs.add(line);
        }
        bufferedReader.close();

        for(int i = 0; i < docs.size(); i++){
            iim.addDocument(new Document(docs.get(i)));
        }
        iim.addDocument(new Document("write testcase difficult"));
        iim.addDocument(new Document("exclude stopwords add nonexistkeywords"));
        iim.flush();

    }

    // make sure searchQuery give right number of results
    @Test
    public void test1(){
        Iterator<Document> result = iim.searchQuery("ebook");
        int count = 0;
        while(result.hasNext()){
            count++;
            result.next();
        }
        assertEquals(16, count);
    }

    // check if Phrase Query work properly
    @Test
    public void test2(){
        Iterator<Document> res = iim.searchPhraseQuery(new ArrayList<String>(Arrays.asList("testcase", "difficult")));

        Document doc = res.next();

        assertEquals("write testcase difficult", doc.getText());
    }

    // check if position index works properly
    // check if stopwords are ignored when calculate position
    @Test
    public void test3(){
        PositionalIndexSegmentForTest result = iim.getIndexSegmentPositional(0);
        Table<String, Integer, List<Integer>> position = result.getPositions();

        assertEquals(new ArrayList<Integer>(Arrays.asList(2)), position.get(stemmer.stem("ebook"), 0));
    }

    // check if position index works properly
    @Test
    public void test4(){
        int segNum = iim.getNumSegments();

        PositionalIndexSegmentForTest res = iim.getIndexSegmentPositional(segNum - 1);
        Table<String, Integer, List<Integer>> position = res.getPositions();

        Map<Integer, List<Integer>> result = position.row(stemmer.stem("nonexistkeywords"));

        assertEquals(1, result.size());
        assertEquals(new ArrayList<Integer>(Arrays.asList(3)), result.values().iterator().next());
    }

    @AfterClass
    public static void clean(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = oldFlushThreshold;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = oldMergeThreshold;

        File file = new File(path);
        String[] FileList = file.list();
        for(String f : FileList){
            File temp = new File(path, f);
            temp.delete();
        }
        file.delete();
    }

}
