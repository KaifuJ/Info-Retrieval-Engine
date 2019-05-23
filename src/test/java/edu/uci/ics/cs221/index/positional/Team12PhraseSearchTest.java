package edu.uci.ics.cs221.index.positional;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.Compressor;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.PageFileChannel;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;


public class Team12PhraseSearchTest {
    private String path = "./index/Team12PhraseSearchTest";
    private Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
    private Compressor compressor = new DeltaVarLenCompressor();
    private InvertedIndexManager invertedIndex;

    /**
     * tests if an empty list of phrases is passed should return an empty iterator
     */
    @Test
    public void test1() {
        List<String> phrase = new ArrayList<>();
        invertedIndex = InvertedIndexManager.createOrOpenPositional(path, analyzer, compressor);

        Iterator<Document> iterator = invertedIndex.searchPhraseQuery(phrase);
        assertFalse(iterator.hasNext());
    }

    /**
     * tests if phrase is present in documents but not in consecutive order should return an empty iterator
     */
    @Test
    public void test2() {
        invertedIndex = InvertedIndexManager.createOrOpenPositional(path, analyzer, compressor);
        invertedIndex.addDocument(new Document("She goes to University of California, Los Angeles. She likes it there." +
                " Despite the fact that she doesn't have time to travel, she visits Orange county every weekend. Most of her friends live in " +
                "Irvine"));
        invertedIndex.addDocument(new Document("Although California is not the largest state, there are many universities."));
        invertedIndex.flush();
        List<String> phrase = new ArrayList<>();
        phrase.add("University");
        phrase.add("of");
        phrase.add("California");
        phrase.add("Irvine");
        Iterator<Document> iterator = invertedIndex.searchPhraseQuery(phrase);
        assertFalse(iterator.hasNext());
    }

    /**
     * tests if returning more than a document, it also tests capitalized words in the phrase and stop words and punctuation
     */
    @Test
    public void test3() {

        invertedIndex = InvertedIndexManager.createOrOpenPositional(path, analyzer, compressor);
        Document doc1 = new Document("He studies Computer Science at the University of California, Irvine.");
        Document doc2 = new Document("The University of California, Irvine is a public research university" +
                " located in Irvine, California.");
        Document doc3 = new Document("Unlike most other University of California campuses, UCI was not named" +
                " for the city it was built in; at the time of the university's founding (1965), the current city of Irvine" +
                " (incorporated in 1971) did not exist. The name Irvine is a reference to James Irvine, a landowner who" +
                " administered the 94,000-acre (38,000 ha) Irvine Ranch.");//text taken from https://en.wikipedia.org/wiki/University_of_California,_Irvine
        invertedIndex.addDocument(doc1);
        invertedIndex.addDocument(doc2);
        invertedIndex.addDocument(doc3);
        invertedIndex.flush();
        List<String> phrase = new ArrayList<>();
        phrase.add("UnivErsIty");
        phrase.add("of");
        phrase.add(",California");
        phrase.add("Irvine");
        Iterator<Document> iterator = invertedIndex.searchPhraseQuery(phrase);
        int counter = 0;
        while (iterator.hasNext()) {
            String text = iterator.next().getText();
            assertTrue(text.equals(doc1.getText()) || text.equals(doc2.getText()));
            counter++;
        }
        assertEquals(2, counter);

    }

    /**
     * tests if the inverted index is not a positional index should throw an UnsupportedOperationException
     */
    @Test
    public void test4() {
        invertedIndex = InvertedIndexManager.createOrOpen(path, analyzer);
        Document doc1 = new Document("He studies Computer Science at the University of California, Irvine.");
        Document doc2 = new Document("The University of California, Irvine is a public research university" +
                " located in Irvine, California.");
        Document doc3 = new Document("Unlike most other University of California campuses, UCI was not named" +
                " for the city it was built in; at the time of the university's founding (1965), the current city of Irvine" +
                " (incorporated in 1971) did not exist. The name Irvine is a reference to James Irvine, a landowner who" +
                " administered the 94,000-acre (38,000 ha) Irvine Ranch.");//text taken from https://en.wikipedia.org/wiki/University_of_California,_Irvine
        invertedIndex.addDocument(doc1);
        invertedIndex.addDocument(doc2);
        invertedIndex.addDocument(doc3);
        invertedIndex.flush();
        List<String> phrase = new ArrayList<>();
        phrase.add("University");
        phrase.add("of");
        phrase.add("California");
        phrase.add("Irvine");
        Iterator<Document> iterator = invertedIndex.searchPhraseQuery(phrase);

    }

    /**
     * tests if searchPhraseQuery function is robust after merging 16 documents
     */
    @Test
    public void test5() {
        invertedIndex = InvertedIndexManager.createOrOpenPositional(path, analyzer, compressor);
        Document doc = new Document("The University of California, Irvine is a public research university" +
                " located in Irvine, California.");

        for (int i = 0; i < 16; ++i) {
            invertedIndex.addDocument(doc);
            invertedIndex.flush();
        }

        while (invertedIndex.getNumSegments() != 1) {
            invertedIndex.mergeAllSegments();
        }

        List<String> phrase = new ArrayList<>();
        phrase.add("University");
        phrase.add("of");
        phrase.add("California");
        phrase.add("Irvine");
        Iterator<Document> iterator = invertedIndex.searchPhraseQuery(phrase);

        int counter = 0;
        while (iterator.hasNext()) {
            iterator.next();
            counter++;
        }
        assertEquals(16, counter);
    }


    @After

    public void cleanUp() {
        PageFileChannel.resetCounters();
        File f = new File(path);
        File[] files = f.listFiles();
        for (File file : files) {
            file.delete();
        }
        f.delete();
    }

}