package edu.uci.ics.cs221.index.ranking;

import edu.uci.ics.cs221.analysis.*;
import edu.uci.ics.cs221.index.inverted.*;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.print.Doc;
import java.io.File;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class Team22TfIdfTest {
    String folder = "./index/Team22TfidfTest";

    InvertedIndexManager im;
    PositionalIndexSegmentForTest imt;
    Analyzer analyzer;
    Compressor compressor;

    @Before
    public void setup() throws Exception {
        Tokenizer tokenizer = new PunctuationTokenizer();
        Stemmer stemmer = new PorterStemmer();
        analyzer = new ComposableAnalyzer(tokenizer, stemmer); // create composable analyzer for documents tokenization
        compressor = new NaiveCompressor();
        im = InvertedIndexManager.createOrOpenPositional(folder,analyzer, compressor );
    }

    @After
    public void cleanup() throws Exception {
        try{
            File index = new File(folder);
            String[] f = index.list();
            for(String s: f){
                File currentFile = new File(index.getPath(),s);
                currentFile.delete();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Files.deleteIfExists(Paths.get(folder));
    }

    /*
    This test case test whether the searchTfidf function return the correct order about the document.
    Also test whether the iterator return only topK documents
     */
    @Test
    public void testOrderAndTopK(){
        Document d1 = new Document("Information retrieval " +
                "is the activity of obtaining information system resources " +
                "relevant to an information need from a collection.");
        Document d2 = new Document("An automated storage and retrieval " +
                "system (ASRS or AS/RS) consists of a variety of computer-controlled " +
                "systems for automatically placing and retrieving loads " +
                "from defined storage locations");
        Document d3 = new Document("Information theory studies the quantification, storage, and communication of information. ");

        im.addDocument(d1);
        im.addDocument(d2);
        im.addDocument(d3);
        im.flush();

        List<Document> dList = new ArrayList<>();
        dList.add(d2);
        dList.add(d1);
        dList.add(d3);

        List<String> keywords = analyzer.analyze("information retrieval retrieval");
        Iterator<Pair<Document, Double>> it = im.searchTfIdf(keywords, 3);

        assertTrue(it.hasNext());
        int counter = 0;
        while(it.hasNext()){
            Document d = it.next().getLeft();
            assertEquals(d, dList.get(counter));
            counter++;
        }
        assertEquals(3, counter);

        counter = 0;
        dList.clear();
        dList.add(d3);
        dList.add(d2);
        keywords = analyzer.analyze("theory storage storage");
        it = im.searchTfIdf(keywords, 2);
        while(it.hasNext()){
            Document d = it.next().getLeft();
            assertEquals(d, dList.get(counter));
            counter++;
        }
        assertEquals(2, counter);
    }

    /*
    This test case test whether searchTfidf function return the correct result
    when topK is null.
     */
    @Test
    public void testOrderAndNull(){
        Document d1 = new Document("Betty Botter bought some better butter butter");
        Document d2 = new Document("but she said the butter bitter");
        Document d3 = new Document("If I put it in my batter, it will make my batter bitter");
        Document d4 = new Document("But a bit of better butter will make my butter better");
        Document d5 = new Document("So it was better Betty Botter don't bought a bit of better butter");

        im.addDocument(d1);
        im.addDocument(d2);
        im.flush();
        im.addDocument(d3);
        im.addDocument(d4);
        im.addDocument(d5);
        im.flush();

        // expected list
        List<Document> dList = new ArrayList<>();
        List<String> keywords = analyzer.analyze("better butter");
        dList.add(d4);
        dList.add(d5);
        dList.add(d1);
        dList.add(d2);

        Iterator<Pair<Document, Double>> it = im.searchTfIdf(keywords, null);
        int counter = 0;
        while(it.hasNext()){
            Document d = it.next().getLeft();
            assertEquals(d, dList.get(counter));
            counter++;
        }
        assertEquals(4, counter);
    }
}
