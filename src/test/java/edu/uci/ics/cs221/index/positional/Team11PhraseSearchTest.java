package edu.uci.ics.cs221.index.positional;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.Compressor;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.*;


public class Team11PhraseSearchTest {
    String indexPath = "./index/Team11PhraseSearchTest";

    Document[] documents = new Document[] {
            new Document("That sounds like a good idea. Maybe we should go out to eat beforehand."),
            new Document("Let’s meet at Summer Pizza House. I have not gone there for a long time."),
            new Document("Good idea again. I heard they just came up with a new pizza."),
            new Document("We can meet at Summer Pizza House at noon. That will give us plenty of time to enjoy our pizza."),
            new Document("She graduated last June, and she will start her teaching career next week when the new school term begins."),
            new Document("The kids might even look forward to going to school since they have so many friends to play with."),
            new Document("I am always amazed by the things kindergarten teachers do so it's a good idea to let her join us.")
    };

    Analyzer analyzer;
    InvertedIndexManager index;
    Compressor compressor;

    @Before
    public void before() {
        analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        compressor = new DeltaVarLenCompressor();
        index = InvertedIndexManager.createOrOpenPositional(indexPath, analyzer, compressor);
    }

    @After
    public void clean() {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 8;

        try {
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

    /*
    * Test1 tests an easy case with only 1 segment and it has 1 document. We test if we
    * can get the right result.
    **/
    @Test
    public void PhraseSearchTest1(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        index.addDocument(documents[0]);

        Set<Document> expected = new HashSet<>(Arrays.asList(documents[0]));
        Set<Document> actual = new HashSet<>();

        Iterator<Document> itr = index.searchPhraseQuery(Arrays.asList("eat", "beforehand"));
        while (itr.hasNext()){
            actual.add(itr.next());
        }
        Assert.assertEquals(expected, actual);
    }

    /*
     * Test2 tests if we can get the right result of phrase query with more segments and phrase
     * is a name of restaurant.
     **/
    @Test
    public void PhraseSearchTest2(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        for (Document doc : documents){
            index.addDocument(doc);
        }

        Set<Document> expected = new HashSet<>(Arrays.asList(documents[1], documents[3]));
        Set<Document> actual = new HashSet<>();

        Iterator<Document> itr = index.searchPhraseQuery(Arrays.asList("Summer", "Pizza", "House"));
        while (itr.hasNext()){
            actual.add(itr.next());
        }
        Assert.assertEquals(expected, actual);
    }

    /*
     * Test3 tests if we can get the right result of phrase query after the segments merge.
     **/
    @Test
    public void PhraseSearchTest3(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 4;
        for (Document doc : documents){
            index.addDocument(doc);
        }

        Set<Document> expected = new HashSet<>(Arrays.asList(documents[0], documents[2], documents[6]));
        Set<Document> actual = new HashSet<>();

        Iterator<Document> itr = index.searchPhraseQuery(Arrays.asList("good", "idea"));
        while (itr.hasNext()){
            actual.add(itr.next());
        }
        Assert.assertEquals(expected, actual);
    }

    /*
     * Test4 tests if the result is null when the phrase is in the documents but not
     * in the consecutive sequence.
     **/
    @Test
    public void PhraseSearchTest4(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        for (Document doc : documents){
            index.addDocument(doc);
        }
        Iterator<Document> itr = index.searchPhraseQuery(Arrays.asList("start", "teaching", "school"));
        assertFalse(itr.hasNext());
    }

}
