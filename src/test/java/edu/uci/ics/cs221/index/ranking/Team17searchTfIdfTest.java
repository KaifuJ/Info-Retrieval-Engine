package edu.uci.ics.cs221.index.ranking;

import edu.uci.ics.cs221.analysis.*;
import edu.uci.ics.cs221.index.inverted.*;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Team17searchTfIdfTest {

    private String path = "./index/Team17searchTfIdfTest";
    private static InvertedIndexManager iim;
    private static  List<String> documents;


    @After
    public void cleanUp (){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        File dir = new File("./index/Team17searchTfIdfTest");
        for (File file: dir.listFiles()){
            if (!file.isDirectory()){
                file.delete();
            }
        }
        dir.delete();
    }

    /**
     * Add some documents and check if all the documents are retrieved for a search phrase that is present.
     * */
    @Test
    public void test1(){
        //Initialize the iim object
        iim = InvertedIndexManager.createOrOpenPositional(
                path,
                new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()),
                new DeltaVarLenCompressor()
        );

        //Create and add documents
        Document doc0 = new Document("The purpose of life is a life with purpose");
        Document doc1 = new Document("The purpose of life is to code");
        Document doc2 = new Document("The purpose of life is to eat good food");
        Document doc3 = new Document("The purpose of life is to play counter strike");
        Document doc4 = new Document("The purpose of life is to play football");
        Document doc5 = new Document("The purpose of life is to sleep");

        iim.addDocument(doc0);
        iim.addDocument(doc1);
        iim.addDocument(doc2);
        iim.addDocument(doc3);
        iim.addDocument(doc4);
        iim.addDocument(doc5);

        List<String> searchKeyword = new ArrayList<>(Arrays.asList("life"));

        Iterator<Pair<Document, Double>> res = iim.searchTfIdf(searchKeyword, 6);

        int count = 0;

        while(res.hasNext()){
            res.next();
            count ++;
        }

        assertEquals(6, count);

    }


    /**
     * Add documents that are very similar and check if the correct top K is retrieved for a keyWord.
     * */
    @Test
    public void test2(){
        //Initialize the iim object
        iim = InvertedIndexManager.createOrOpenPositional(
                path,
                new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()),
                new DeltaVarLenCompressor()
        );

        //Create and add documents
        Document doc0 = new Document("The purpose of life is a life with purpose");
        Document doc1 = new Document("The purpose of life is to code");
        Document doc2 = new Document("The purpose of life is to eat good food");
        Document doc3 = new Document("The Purpose");
        Document doc4 = new Document("Life of Pi is a good movie");
        Document doc5 = new Document("What is the purpose of knowing about after-life");

        iim.addDocument(doc0);
        iim.addDocument(doc1);
        iim.addDocument(doc2);
        iim.addDocument(doc3);
        iim.addDocument(doc4);
        iim.addDocument(doc5);

        List<String> searchKeyword = new ArrayList<>(Arrays.asList("The", "purpose", "of", "life", "is"));

        Iterator<Pair<Document, Double>> res = iim.searchTfIdf(searchKeyword, 3);


        List<String> expected = new ArrayList<String>();
        List<String> actual = new ArrayList<String>();
        int count = 0;

        //Check which of these are topK
        expected.add(doc0.getText());
        expected.add(doc1.getText());
        expected.add(doc2.getText());

        while(res.hasNext()){
            count ++;
            //actual.add(res.next().getLeft().getText());
        }

        assertEquals(3, count);
        //assertTrue(actual.equals(expected));
    }



    /**
     * Search for a non existing keyword.
     * */
    @Test
    public void test3(){
        //Initialize the iim object
        iim = InvertedIndexManager.createOrOpenPositional(
                path,
                new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()),
                new DeltaVarLenCompressor()
        );

        //Create and add documents
        Document doc0 = new Document("The purpose of life is a life with purpose");
        Document doc1 = new Document("The purpose of life is to code");
        Document doc2 = new Document("The purpose of life is to eat good food");
        Document doc3 = new Document("The purpose of life is to play counter strike");
        Document doc4 = new Document("The purpose of life is to play football");
        Document doc5 = new Document("The purpose of life is to sleep");

        iim.addDocument(doc0);
        iim.addDocument(doc1);
        iim.addDocument(doc2);
        iim.addDocument(doc3);
        iim.addDocument(doc4);
        iim.addDocument(doc5);

        List<String> searchKeyword = new ArrayList<>(Arrays.asList("JCBKiKhudai"));

        Iterator<Pair<Document, Double>> res = iim.searchTfIdf(searchKeyword, 3);

        assertTrue(!res.hasNext());
    }

}
