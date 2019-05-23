package edu.uci.ics.cs221.index.positional;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.*;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class Team21PositionalMergeTest {
    String indexFolder = "./index/Team21PositionalMergeTest/";
    String[]  str = new String[]{
            "dog cat penguin whale",
            "snake bird cat lion",
            "fish bird whale penguin",
            "anteater fish snake dog",
            "cat dog bird penguin cat fish bird dog whale cat",
            "whale bird fish dog bird fish cat bird dog whale"
    };

    @After
    public void deleteWrittenFiles() throws IOException  {
        File file = new File(indexFolder);
        if (file.exists() && file.isDirectory()) {
            String[] fileList = file.list();
            File temp = null;
            for (int i = 0; i < fileList.length; i++) {
                if (indexFolder.endsWith(File.separator)) {
                    temp = new File(indexFolder + fileList[i]);
                } else {
                    temp = new File(indexFolder + File.separator + fileList[i]);
                }
                if (temp.isFile()) {
                    temp.delete();
                }
            }
        }
        Files.deleteIfExists(Paths.get(indexFolder));
    }

    @Test
    public void test1() {
        /*
        test mergeAllSegments() and getNumSegments() functions.
         */
        ComposableAnalyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), token -> token);
        InvertedIndexManager manager = InvertedIndexManager.createOrOpenPositional(indexFolder, analyzer, new NaiveCompressor());

        manager.addDocument(new Document(str[0]));
        manager.addDocument(new Document(str[1]));
        manager.flush();
        manager.addDocument(new Document(str[2]));
        manager.addDocument(new Document(str[3]));
        manager.flush();
        manager.addDocument(new Document(str[4]));
        manager.addDocument(new Document(str[5]));
        manager.flush();
        manager.addDocument(new Document(str[0]));
        manager.addDocument(new Document(str[1]));
        manager.flush();
        manager.mergeAllSegments();

        assertEquals(2, manager.getNumSegments());

    }

    @Test
    public void test2() {
        /*
        test mergeAllSegments() and getIndexSegment() functions.
         */
        ComposableAnalyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        InvertedIndexManager manager = InvertedIndexManager.createOrOpenPositional(indexFolder, analyzer, new NaiveCompressor());

        manager.addDocument(new Document(str[0]));
        manager.flush();
        manager.addDocument(new Document(str[2]));
        manager.flush();
        manager.mergeAllSegments();

//            "dog cat penguin whale",
//            "fish bird whale penguin",

        HashMap<String, List<Integer>> invertedLists = new HashMap<>();
        HashMap<Integer, Document> documents = new HashMap<>();
        Table<String, Integer, List<Integer>> positions = HashBasedTable.create();

        invertedLists.put("dog",Arrays.asList(0));
        invertedLists.put("cat",Arrays.asList(0));
        invertedLists.put("penguin",Arrays.asList(0,1));
        invertedLists.put("whale",Arrays.asList(0,1));
        invertedLists.put("fish",Arrays.asList(1));
        invertedLists.put("bird",Arrays.asList(1));

        documents.put(0, new Document(str[0]));
        documents.put(1, new Document(str[2]));

        positions.put("dog",0,Arrays.asList(0));
        positions.put("cat",0,Arrays.asList(1));
        positions.put("penguin",0,Arrays.asList(2));
        positions.put("whale",0,Arrays.asList(3));

        positions.put("fish",1,Arrays.asList(0));
        positions.put("bird",1,Arrays.asList(1));
        positions.put("whale",1,Arrays.asList(2));
        positions.put("penguin",1,Arrays.asList(3));

        PositionalIndexSegmentForTest expected = new PositionalIndexSegmentForTest(invertedLists,documents,positions);

        assertEquals(expected,manager.getIndexSegmentPositional(0));
    }

    @Test
    public void test3() {
        /*
        test mergeAllSegments(), getIndexSegment() and getPositions() functions.
         */
        ComposableAnalyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), token -> token);
        InvertedIndexManager manager = InvertedIndexManager.createOrOpenPositional(indexFolder, analyzer, new NaiveCompressor());

        manager.addDocument(new Document(str[4]));
        manager.flush();
        manager.addDocument(new Document(str[5]));
        manager.flush();
        manager.mergeAllSegments();

//            "cat dog bird penguin cat fish bird dog whale cat",
//            "whale bird fish dog bird fish cat bird dog whale"

        Table<String, Integer, List<Integer>> positions = HashBasedTable.create();

        positions.put("cat",0,Arrays.asList(0,4,9));
        positions.put("dog",0,Arrays.asList(1,7));
        positions.put("bird",0,Arrays.asList(2,6));
        positions.put("penguin",0,Arrays.asList(3));
        positions.put("fish",0,Arrays.asList(5));
        positions.put("whale",0,Arrays.asList(8));

        positions.put("whale",1,Arrays.asList(0,9));
        positions.put("bird",1,Arrays.asList(1,4,7));
        positions.put("fish",1,Arrays.asList(2,5));
        positions.put("dog",1,Arrays.asList(3,8));
        positions.put("cat",1,Arrays.asList(6));

        assertEquals(positions,manager.getIndexSegmentPositional(0).getPositions());
    }

}
