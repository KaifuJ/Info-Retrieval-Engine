package edu.uci.ics.cs221.index.ranking;

import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.Pair;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class Team18SearchTfIdfTest {
    InvertedIndexManager manager;
    String folderPath = "./index/Team18SearchTfIdfTest";
    Document[] docs = new Document[]{
            new Document("The breed's distinctive folded ears are produced by an incompletely dominant gene that affects the cartilage of the ears, causing the ears to fold forward and downward, giving a cap-like appearance to the head."),
            new Document("Smaller, tightly ears set in a cap-like fashion are preferred to a loose fold and larger ear."),
            new Document(" The large, round eyes and rounded head, cheeks, and whisker pads add to the overall rounded appearance."),
            new Document("Despite the folded ears, folds still use their aural appendages to express themselves—the ears swivel to listen, lie back in anger and prick up when the treat bag rustles.")};

    @Before
    public void initialize(){
        manager = InvertedIndexManager.createOrOpenPositional(folderPath, new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()), new DeltaVarLenCompressor());

    }

    @After
    public void clear(){
        File dir = new File("./index/Team18SearchTfIdfTest");
        for (File file: dir.listFiles()){
            if (!file.isDirectory()){
                file.delete();
            }
        }
        dir.delete();
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 8;
    }

    // Test if searchTfIdf returns the correct order and number of documents
    @Test
    public void test1(){
        for(int i = 0; i < docs.length; i ++){
            manager.addDocument(docs[i]);
        }
        manager.flush();
        if(manager.getNumSegments() > 1)
            manager.mergeAllSegments();

        List<String> keywords = new ArrayList<>(Arrays.asList("fold","ears","round"));
        Iterator<Pair<Document, Double>> it = manager.searchTfIdf(keywords,3);

        List<Document> dList = new ArrayList<>();
        dList.add(docs[2]);
        dList.add(docs[3]);
        dList.add(docs[0]);

        assertTrue(it.hasNext());
        int counter = 0;
        while(it.hasNext()){
            Document d = it.next().getLeft();
            assertEquals(d, dList.get(counter));
            counter++;
        }
        assertEquals(3, counter);

    }

    // Test when topK is set to 0, the iterator is null or not
    @Test
    public void test2(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        for(int i = 0; i < docs.length; i ++){
            manager.addDocument(docs[i]);
        }
        manager.flush();
        if(manager.getNumSegments() > 1)
            manager.mergeAllSegments();

        List<String> keywords = new ArrayList<>(Arrays.asList("fold","ears"));
        Iterator<Pair<Document, Double>> it = manager.searchTfIdf(keywords,0);
        assert !it.hasNext();
    }
}
