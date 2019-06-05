package edu.uci.ics.cs221.index.ranking;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.Compressor;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.index.inverted.Pair;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.*;

public class Team11TfIdfTest {
    String indexPath = "./index/Team11TfIdfTest";

    Document[] documents = new Document[] {
            new Document("That sounds like a good idea. Maybe we should go out to eat beforehand."),
            new Document("Let’s meet at Summer Pizza House. I have not gone there for a long time."),
            new Document("Good thought again. I heard they just came up with a new pizza."),
            new Document("We can meet at Summer Pizza House at noon. That will give us plenty of time to enjoy our pizza."),
            new Document("She graduated last June, and she will start her teaching career next week when the new school term begins."),
            new Document("The kids might even look forward to going to school since they have so many friends to play with."),
            new Document("I am always amazed by the things kindergarten teachers do so it's a great idea to let her join us."),
            new Document("Good idea! It is always good to eat pizza right here in summer.")
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
     * Test1 tests an easy case which only has 1 segment and returns the document
     * with the highest score for query "good" and "thought".
     **/
    @Test
    public void TfIdfTest1(){
        for (Document doc : documents){
            index.addDocument(doc);
        }
        index.flush();

        List<Document> expected = new ArrayList<>(Arrays.asList(documents[2]));
        List<String> keywords = Arrays.asList("good","thought");
        Iterator<Pair<Document, Double>> itr = index.searchTfIdf(keywords, 1);
        int i = 0;

        while (itr.hasNext()){
            assertEquals(expected.get(i), itr.next().getLeft());
            i++;
        }
    }

    /*
    * Test2 tests if we can get the top 4 documents with multiple segments.
    **/
    @Test
    public void TfIdfTest2(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 2;

        for (Document doc : documents){
            index.addDocument(doc);
        }

        List<Document> expected = new ArrayList<>(Arrays.asList(documents[7], documents[0],
                                                                documents[2], documents[6]));
        List<String> keywords = Arrays.asList("good", "good", "idea");
        Iterator<Pair<Document, Double>> itr = index.searchTfIdf(keywords, 4);
        int i = 0;

        while (itr.hasNext()){
            assertEquals(expected.get(i), itr.next().getLeft());
            i++;
        }
    }

    /*
    * Test3 tests a more complicated case with multiple segments and they also are merged. We want to
    * test if we can get the right result of top 4 documents.
    **/
    @Test
    public void TfIdfTest3(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;

        for (Document doc : documents){
            index.addDocument(doc);
        }

        List<Document> expected = new ArrayList<>(Arrays.asList(documents[1], documents[3],
                                                                documents[7], documents[2]));
        List<String> keywords = Arrays.asList("summer", "pizza", "house");
        Iterator<Pair<Document, Double>> itr = index.searchTfIdf(keywords, 4);
        int i = 0;

        while (itr.hasNext()){
            assertEquals(expected.get(i), itr.next().getLeft());
            i++;
        }
    }

    /*
    * Test4 tests if we can get all the ordered documents if the topK is null.
    **/
    @Test
    public void TfIdfTest4(){
        index.addDocument(documents[0]);
        index.addDocument(documents[2]);
        index.addDocument(documents[6]);
        index.addDocument(documents[7]);
        index.flush();

        List<Document> expected = new ArrayList<>(Arrays.asList(documents[7], documents[0],
                                                                documents[2], documents[6]));
        List<String> keywords = Arrays.asList("good", "good", "idea");
        Iterator<Pair<Document, Double>> itr = index.searchTfIdf(keywords, null);
        int i = 0;

        while (itr.hasNext()){
            assertEquals(expected.get(i), itr.next().getLeft());
            i++;
        }

        assertEquals(expected.size(), i);
    }

}
