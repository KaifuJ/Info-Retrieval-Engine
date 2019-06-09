package edu.uci.ics.cs221.index.ranking;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.Compressor;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class Team2TfIdfTest {
    InvertedIndexManager invertedIndexManager;
    String folderPath = "./index/Team2TfIdfTest";
    private Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
    private Compressor compressor = new DeltaVarLenCompressor();

    Document[] documents = new Document[]{
            new Document("The cat (Felis catus) is a small carnivorous mammal"),
            new Document("Cats are similar in anatomy to the other felid species"),
            new Document("Female domestic cats can have kittens from spring to late autumn"),
            new Document("As of 2017, the domestic cat was the second-most popular pet in the U.S."),
            new Document("She lay curled up on the sofa in the back drawing-room in Harley Street, looking very lovely in her white muslin and blue ribbons"),
            new Document("I suffered too much myself"),
            new Document("Margaret went up into the old nursery at the very top of the house")
    };

    @After
    public void clear(){
        File dir = new File("./index/Team2TfIdfTest");
        for (File file: dir.listFiles()){
            if (!file.isDirectory()){
                file.delete();
            }
        }
        dir.delete();
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 8;
    }

    // Add several documents below the merge threshold. Do a normal test to check whether to documents is calculated correctly
    @Test
    public void test1() {
        invertedIndexManager = InvertedIndexManager.createOrOpenPositional(folderPath, analyzer, compressor);
        for (Document doc : documents) {
            invertedIndexManager.addDocument(doc);
        }
        invertedIndexManager.flush();
        assertEquals(7,invertedIndexManager.getNumDocuments(0));
    }

    // Test whether the documents is calculated correctly when merge happens
    @Test
    public void test2() {
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 4;
        invertedIndexManager = InvertedIndexManager.createOrOpenPositional(folderPath, analyzer, compressor);
        for (Document doc : documents) {
            invertedIndexManager.addDocument(doc);
        }
        invertedIndexManager.flush();
        int segmentNumber = invertedIndexManager.getNumSegments();
        int documentNum = 0;
        for (int i = 0; i < segmentNumber; i++) {
            documentNum += invertedIndexManager.getNumDocuments(i);
        }
        assertEquals(7, documentNum);
    }
}
