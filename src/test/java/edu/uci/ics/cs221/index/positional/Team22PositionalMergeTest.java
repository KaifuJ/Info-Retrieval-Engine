package edu.uci.ics.cs221.index.positional;

import com.google.common.collect.HashBasedTable;
import edu.uci.ics.cs221.analysis.*;
import edu.uci.ics.cs221.index.inverted.Compressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.InvertedIndexSegmentForTest;
import edu.uci.ics.cs221.index.inverted.PositionalIndexSegmentForTest;
import edu.uci.ics.cs221.index.inverted.NaiveCompressor;
import javafx.geometry.Pos;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import edu.uci.ics.cs221.storage.Document;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import javax.print.Doc;

import static org.junit.Assert.*;

public class Team22PositionalMergeTest {
    String folder = "./index/Team22PositionalMergeTest";

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
    create ground truth of positional table
     */
    private Table<String, Integer, List<Integer>> createPositional(List<Document> docs){
        Table<String, Integer, List<Integer>> table = HashBasedTable.create();
        int docID = 0; // assume docID start from 0
        for(Document d: docs){
            List<String> tokens = analyzer.analyze(d.getText());
            for (int i = 0; i < tokens.size(); i++){
                if (table.get(tokens.get(i), docID) == null){
                    table.put(tokens.get(i), docID, new ArrayList<>());
                }
                table.get(tokens.get(i), docID).add(i+1); // positional idx start from 1
            }
            docID++; // assume docID always continuous
        }
        return table;
    }

    /*
    This function check the correctness of positional list
     */
    private void checkPositional(int segmentNum, List<Document> docs){
        // get corresponding segment
        imt = im.getIndexSegmentPositional(segmentNum);
        Table<String, Integer, List<Integer>> ptable = imt.getPositions(), groundTruth = createPositional(docs);

        for (Cell<String, Integer, List<Integer>> cell: groundTruth.cellSet()){
            List<Integer> l = ptable.get(cell.getRowKey(), cell.getColumnKey());

            assertTrue(l != null); // check list contain in ptable
            Set<Integer> pSet = new HashSet<Integer>(l);

            for(int pos: cell.getValue()){
                assertTrue(pSet.contains(pos)); // check all ground truth position index in target list
            }
        }
    }

    /*
    This function create the ground truth only about inverted index of "one segments" of given documents lists
     */
    private HashMap<String, HashSet<Document>> createInvertedIndex2Doc(List<Document> docs){

        // to use the output,
        // just iterate through the posting list, then get the corresponding
        HashMap<String, HashSet<Document>> output = new HashMap<>();
        for(Document d: docs){
            List<String> tokens = analyzer.analyze(d.getText()); // split the text to tokens
            for (String token: tokens){
                if (!output.containsKey(token)){ // if the output map doesn't contain this token, create new entry

                    output.put(token, new HashSet<Document>());
                }
                output.get(token).add(d); // add this document to corresponding tokens

            }

        }

        return output;
    }

    // function to check if segment is read correctly
    private void checkSegment(int segmentNum, List<Document> docs){
        // get corresponding segment
        imt = im.getIndexSegmentPositional(segmentNum);

        // get inverted list and documents of the segment
        Map<String, List<Integer>> segIndex = imt.getInvertedLists();
        Map<Integer, Document> segDoc = imt.getDocuments();

        // create ground true inverted list
        Map<String,HashSet<Document>> segGroundTrue = createInvertedIndex2Doc(docs);

        // check if the posting list are correct
        for (Map.Entry<String, List<Integer>> entry : segIndex.entrySet()) {
            String keyword = entry.getKey();
            List<Integer> postinglist = entry.getValue();

            for (int docID: postinglist){
                Document d = segDoc.get(docID);
                assertTrue(segGroundTrue.get(keyword).contains(d));
            }
        }

        // check if the documents are correct
        for (Document d: docs){
            assertTrue(segDoc.containsValue(d));
        }

    }
    /*
    This test case check whether index manager manually merge the segment and
    produce correct inverted index and positional index
     */

    @Test
    public void simpleMergeTest(){
        Document doc1 = new Document("Information retrieval");
        Document doc2 = new Document("There is no easy way");

        im.addDocument(doc1);
        im.addDocument(doc2);
        im.flush();

        Document doc3 = new Document("vector space and Boolean queries");
        Document doc4 = new Document("A general theory of information retrieval");
        im.addDocument(doc3);
        im.addDocument(doc4);
        im.flush();


        im.mergeAllSegments();

        List<Document> seg1Docs = new ArrayList<>();
        seg1Docs.add(doc1);
        seg1Docs.add(doc2);
        seg1Docs.add(doc3);
        seg1Docs.add(doc4);


        assertEquals(1, im.getNumSegments()); // there is only one segment after merge

        // check inverted index
        checkSegment(0, seg1Docs);
        // check positional index
        checkPositional(0, seg1Docs);

    }

    /*
    This test case check whether index manager produce correct inverted index
    and positional index with specific merge threshold
     */

    @Test
    public void thresholdMergeTest(){
        int ori_flush_th = im.DEFAULT_FLUSH_THRESHOLD, ori_mer_th = im.DEFAULT_MERGE_THRESHOLD;

        // change default mergethreshold
        im.DEFAULT_FLUSH_THRESHOLD = 1;
        im.DEFAULT_MERGE_THRESHOLD = 4;

        Document[] documents = new Document[] {
            new Document("In this project"),
            new Document("you'll be implementing a disk-based inverted index and the search operations."),
            new Document("At a high level, inverted index stores a mapping from keywords to the ids of documents they appear in."),
            new Document("A simple in-memory structure could be"),
            new Document("where each key is a keyword token"),
            new Document("and each value is a list of document IDs"),
            new Document("In this project, the disk-based index structure is based on the idea of LSM"),
            new Document("Its main idea is the following")
        };

        // define ground truth segment docs
        List<Document> seg1docs = new ArrayList<>();
        List<Document> seg2docs = new ArrayList<>();
        for (int i = 0; i < documents.length; i++){
            im.addDocument(documents[i]);
            if (i < documents.length-1) {
                seg1docs.add(documents[i]);
            } else {
                seg2docs.add(documents[i]);
            }
        }


        // check number of segments
        assertEquals(2, im.getNumSegments());

        // check correctness
        checkSegment(0, seg1docs);
        checkSegment(1, seg2docs);
        checkPositional(0, seg1docs);
        checkPositional(1, seg2docs);

        im.DEFAULT_FLUSH_THRESHOLD = ori_flush_th;
        im.DEFAULT_MERGE_THRESHOLD = ori_mer_th;
    }
}
