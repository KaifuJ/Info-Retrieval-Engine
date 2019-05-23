package edu.uci.ics.cs221.index.inverted;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class Team5OrSearchTest {
    private String path = "./index/Team5OrSearchTest";
    private Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
    private InvertedIndexManager invertedList;

    @Before
    public void setUp() throws Exception {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        invertedList = InvertedIndexManager.createOrOpen(path, analyzer);
        invertedList.addDocument(new Document("cat dog toy"));
        invertedList.flush();
        invertedList.addDocument(new Document("cat Dot"));
        invertedList.flush();
        invertedList.addDocument(new Document("cat dot toy"));
        invertedList.flush();
        invertedList.addDocument(new Document("cat toy Dog"));
        invertedList.flush();
        invertedList.addDocument(new Document("toy dog cat"));
        invertedList.flush();
        invertedList.addDocument(new Document("cat Dog"));//docs cannot be null
        invertedList.flush();
    }

    // test if multiple keywords work or not. And we set 5 as a threshold for write counter and read counter,
    // because I think the number will increase when we call the flush() function and we dont know the execution order
    // of test cases, so we set them all to 5.
    @Test
    public void Test1() throws Exception {
        List<String> words = new ArrayList<>();
        words.add("cat");
        words.add("dog");

        Iterator<Document> iterator = invertedList.searchOrQuery(words);
        int counter = 0;
        while (iterator.hasNext()) {
            String text = iterator.next().getText();
            assertEquals(true, text.contains("dog") || text.contains("cat"));
            counter++;
        }
        assertEquals(6, counter);
        assertTrue(PageFileChannel.readCounter >= 5 && PageFileChannel.writeCounter >= 5);
        words.clear();

    }

    //test if single key words works or not    
    @Test
    public void Test2() throws Exception {
        List<String> words = new ArrayList<>();
        words.add("dog");

        Iterator<Document> iterator = invertedList.searchOrQuery(words);
        int counter = 0;
        while (iterator.hasNext()) {
            String text = iterator.next().getText();
            assertEquals(true, text.toLowerCase().contains("dog"));
            counter++;

        }
        assertEquals(4, counter);
        assertTrue(PageFileChannel.readCounter >= 5 && PageFileChannel.writeCounter >= 5);
        words.clear();

    }

    //test the case that the key word does not match any file
    @Test
    public void Test3() throws Exception {
        List<String> words = new ArrayList<>();
        words.add("sdasjdlslsah");
        words.add("*7&");
        Iterator<Document> iterator = invertedList.searchOrQuery(words);
        int counter = 0;
        while (iterator.hasNext()) {

            String text = iterator.next().getText();
            assertEquals(true, text.contains("sdasjdlslsah") || text.contains("*7&"));
            counter++;

        }
        assertEquals(0, counter);
        assertTrue(PageFileChannel.readCounter >= 5 && PageFileChannel.writeCounter >= 5);
        words.clear();

    }

    //test other words combination
    @Test
    public void Test4() throws Exception {
        List<String> words = new ArrayList<>();
        words.add("toy");
        words.add("dog");
        Iterator<Document> iterator = invertedList.searchOrQuery(words);
        int counter = 0;
        while (iterator.hasNext()) {

            String text = iterator.next().getText();
            assertEquals(true,
                    text.toLowerCase().contains("dog") || text.toLowerCase().contains("toy"));
            counter++;

        }
        assertEquals(5, counter);
        assertTrue(PageFileChannel.readCounter >= 5 && PageFileChannel.writeCounter >= 5);
        words.clear();

    }

    //test empty query list.
    @Test
    public void Test5() {
        List<String> words = new ArrayList<>();
        Iterator<Document> iterator = invertedList.searchOrQuery(words);
        assertFalse(iterator.hasNext());
    }

    @After

    public void deleteTmp() throws Exception {
        PageFileChannel.resetCounters();
        File f = new File(path);
        File[] files = f.listFiles();
        for (File file : files) {
            file.delete();
        }
        f.delete();
    }

}