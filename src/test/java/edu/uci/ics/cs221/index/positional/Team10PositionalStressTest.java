package edu.uci.ics.cs221.index.positional;

import com.google.common.collect.Table;
import edu.uci.ics.cs221.analysis.*;
import edu.uci.ics.cs221.index.inverted.NaiveCompressor;
import edu.uci.ics.cs221.storage.Document;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.PositionalIndexSegmentForTest;
import org.junit.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class Team10PositionalStressTest {

    private String path = "./index/Team10PositionalStressTest";
    private String bookurl = "http://www.gutenberg.org/cache/epub/4276/pg4276.txt";
    private InvertedIndexManager iim;
    private List<String> docs;

    @Test(timeout = 600000)
    public void init(){
        iim = InvertedIndexManager.createOrOpenPositional(path, 
                                new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()), 
                                new DeltaVarLenCompressor());
        
        int oldFlushThreshold = iim.DEFAULT_FLUSH_THRESHOLD;
        int oldMergeThreshold = iim.DEFAULT_MERGE_THRESHOLD;

        iim.DEFAULT_FLUSH_THRESHOLD = 500;
        iim.DEFAULT_MERGE_THRESHOLD = 20;
        docs = new ArrayList<>();

        java.net.URL url = new URL(bookurl);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            docs.add(line);
        }
        bufferedReader.close();

        for(int i = 0; i < docs.size(); i++){
            iim.addDocument(new Document(docs.get(i)));
        }
        iim.addDocument(new Document("zhege testcase wojuede youdianfan"));
        iim.addDocument(new Document("wohaiyao quebao zhege document limian meiyou stopwords"));
        iim.flush();

        test1();
        test2();
        test3();
        test4();

        iim.DEFAULT_FLUSH_THRESHOLD = oldFlushThreshold;
        iim.DEFAULT_MERGE_THRESHOLD = oldMergeThreshold;
    }

    // make sure searchQuery give right number of results
    public void test1(){
        Iterator<Document> result = iim.searchQuery("ebook");
        int count = 0;
        while(result.hasNext()){
            count++;
            result.next();
        }
        assertEquals(17, count);
    }

    // check if Phrase Query work properly
    public void test2(){
        Iterator<Document> res = iim.searchPhraseQuery(new ArrayList<String>(Arrays.asList("wojuede", "youdianfan")));

        Document doc = res.next();

        assertEquals("zhege testcase wojuede youdianfan", doc.getText());
    }

    // check if position index works properly
    // check if stopwords are ignored when calculate position
    public void test3(){
        PositionalIndexSegmentForTest result = iim.getIndexSegmentPositional(0);
        Table<String, Integer, List<Integer>> position = result.getPositions();

        // We prefer 0-based position.
        // If your implementation is 1-based, you can change the number to '3'
        assertEquals(new ArrayList<Integer>(Arrays.asList(2)), position.get("ebook", 0));
    }

    // check if position index works properly
    public void test4(){
        int segNum = iim.getNumSegments();

        PositionalIndexSegmentForTest res = iim.getIndexSegmentPositional(segNum - 1);
        Table<String, Integer, List<Integer>> position = res.getPositions();
        ArrayList<Integer> positionList = position.get("meiyou", iim.getDocuments().size() - 1);

        assertEquals(new ArrayList<Integer>(Arrays.asList(5)), positionList);
    }

    @After
    public void clean(){
        File file = new File(path);
        String[] FileList = file.list();
        for(String f : FileList){
            File temp = new File(path, f);
            temp.delete();
        }
        file.delete();
    }

}
