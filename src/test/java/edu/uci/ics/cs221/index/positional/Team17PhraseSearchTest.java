package edu.uci.ics.cs221.index.positional;

import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.NaiveCompressor;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.text.html.HTMLDocument;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Team17PhraseSearchTest {
    private final String path = "./index/Team17PhraseSearchTest";
    private Document doc1 = new Document("too young, too simple, sometimes naive");
    private Document doc2 = new Document("I'm angry!");
    private Document doc3 = new Document("The West Virginia Central Junction is a place in United States of America");
    private Document doc4 = new Document("Los Ranchos de Albuquerque is the name of a place");

    @Before
    public void init(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 5;
    }

    @After
    public void cleanUp (){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        File dir = new File(path);
        for (File file: dir.listFiles()){
            if (!file.isDirectory()){
                file.delete();
            }
        }
        dir.delete();
    }

    /**
     *  Tests if searching an empty phrase returns an empty iterator.
     */
    @Test
    public void testSearchEmptyPhrase(){
        InvertedIndexManager iim;
        iim = InvertedIndexManager.createOrOpenPositional(path, new ComposableAnalyzer( new PunctuationTokenizer(), new PorterStemmer()), new NaiveCompressor());
        Iterator<Document> iter = iim.searchPhraseQuery(new ArrayList<String>());
        assertEquals(false, iter.hasNext());
    }

    /**
     *  Tests if searching on an inverted index returns an UnsupportedOperationException.
     */
    @Test (expected = UnsupportedOperationException.class)
    public void testSearchNonPositional(){
        InvertedIndexManager iim;
        iim = InvertedIndexManager.createOrOpen(path, new ComposableAnalyzer( new PunctuationTokenizer(), new PorterStemmer()));
        Iterator<Document> iter = iim.searchPhraseQuery(new ArrayList<String>());
    }

    /**
     *  Tests if popular phrases in the dictionary is handled properly
     */
    @Test
    public void testPopularPhrases(){
        InvertedIndexManager iim;
        iim = InvertedIndexManager.createOrOpenPositional(path, new ComposableAnalyzer( new PunctuationTokenizer(), new PorterStemmer()), new NaiveCompressor());
        for(int i=0; i<InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD; i++){
            iim.addDocument(doc3);
            iim.addDocument(doc4);
        }
        List<String> phrases = new ArrayList<>();
        phrases.add("west");
        phrases.add("virginia");
        phrases.add("central");
        phrases.add("junction");
        Iterator<Document> iter = iim.searchPhraseQuery(phrases);
        int count = 0;
        while(iter.hasNext()){
            iter.hasNext();
            Document nextDoc = iter.next();
            assertEquals(doc3, nextDoc);
            count++;
        }
        assertEquals(InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD, count);
    }

    /**
     *  Tests if searching on a single-word phrase returns the correct result.
     *  Tests if the iterator returned is implemented correctly, i.e. hasNext() does not cause documents to be skipped
     *  Tests if searching after flush and merge is working correctly.
     */
    @Test
    public void testSearchSingleWordPhrase(){
        InvertedIndexManager iim;
        iim = InvertedIndexManager.createOrOpenPositional(path, new ComposableAnalyzer( new PunctuationTokenizer(), new PorterStemmer()), new NaiveCompressor());
        iim.addDocument(doc2);
        for(int i=0; i<InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD*4 - 1; i++){
            iim.addDocument(doc1);
        }
        iim.mergeAllSegments();
        List<String> phrases = new ArrayList<>();
        phrases.add("young");
        Iterator<Document> iter = iim.searchPhraseQuery(phrases);
        int count = 0;
        while(iter.hasNext()){
            iter.hasNext();
            Document nextDoc = iter.next();
            assertEquals(doc1, nextDoc);
            count++;
        }
        assertEquals(InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD*4-1, count);
    }

    /**
     *  Tests if searching on a multi-word phrase, containing stop words, returns the correct result.
     *  Tests if the iterator returned is implemented correctly, i.e. hasNext() does not cause documents to be skipped
     *  Tests if searching after flush and merge is working correctly.
     */
    @Test
    public void testSearchMultipleWordsPhrase(){
        InvertedIndexManager iim;
        iim = InvertedIndexManager.createOrOpenPositional(path, new ComposableAnalyzer( new PunctuationTokenizer(), new PorterStemmer()), new NaiveCompressor());
        iim.addDocument(doc2);
        for(int i=0; i<InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD*4 - 1; i++){
            iim.addDocument(doc1);
        }
        iim.mergeAllSegments();
        List<String> phrases = new ArrayList<>();
        phrases.add("too");
        phrases.add("young");
        phrases.add("too");
        phrases.add("simple");
        Iterator<Document> iter = iim.searchPhraseQuery(phrases);
        int count = 0;
        while(iter.hasNext()){
            iter.hasNext();
            Document nextDoc = iter.next();
            assertEquals(doc1, nextDoc);
            count++;
        }
        assertEquals(InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD*4-1, count);

        phrases.clear();
        phrases.add("young");
        phrases.add("naive");
        Iterator<Document> iter2 = iim.searchPhraseQuery(phrases);
        assertEquals(false, iter2.hasNext());
    }


}