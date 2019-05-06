package edu.uci.ics.cs221.index.inverted;
import edu.uci.ics.cs221.analysis.*;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.InvertedIndexSegmentForTest;
import edu.uci.ics.cs221.index.inverted.PageFileChannel;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.print.Doc;
import java.nio.file.Files;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.*;

import static org.junit.Assert.*;

public class Team22FlushTest {
    String folder = "./index/Team22FlushTest";

    InvertedIndexManager indexManager;
    InvertedIndexSegmentForTest indexManagerTest;
    Analyzer analyzer;



    @Before
    public void setup() throws Exception {

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
    This test case check whether flushed segment stored correctly
    We check for both 1. invertedIndexList 2. documents store


     */
    @Test
    public void verifyContextTest() throws Exception{
        // In this test, we verify the inverted index list in the segment in disk.
        // Also, we verify the documents in each segment are correct

        Tokenizer tokenizer = new PunctuationTokenizer();
        Stemmer stemmer = new PorterStemmer();
        analyzer = new ComposableAnalyzer(tokenizer, stemmer); // create composable analyzer for documents tokenization

        indexManager = InvertedIndexManager.createOrOpen(folder,analyzer);


        // documents in seg 0
        Document doc1 = new Document("Information retrieval (IR) is the activity"
                + " of obtaining information system resources"
                + " relevant to an information need from a collection.");
        Document doc2 = new Document("There is no easy way of combining " +
                "vector space and Boolean queries from a user's standpoint: " +
                "vector space queries are fundamentally a form of evidence " +
                "accumulation,");
        Document doc3 = new Document("");

        ArrayList<Document> seg0Docs = new ArrayList<>();
        seg0Docs.add(doc1);
        seg0Docs.add(doc2);
        seg0Docs.add(doc3);
        indexManager.addDocument(doc1);
        indexManager.addDocument(doc2);
        indexManager.addDocument(doc3);
        indexManager.flush(); // create segment 0 in disk


        // documents in seg 1
        Document doc4 = new Document("Four basic operations in the effective"+
                " use of graphic records (documents), to store information and make " +
                "it available, have been listed by Hyslop: " +
                "A, recording information in documents; B, " +
                "storing recorded information—documentary items; ");
        Document doc5 = new Document("A general theory of information retrieval would cover");

        ArrayList<Document> seg1Docs = new ArrayList<>();

        seg1Docs.add(doc4);
        seg1Docs.add(doc5);

        indexManager.addDocument(doc4);
        indexManager.addDocument(doc5);
        indexManager.flush(); // create segment 0 in disk


        assertEquals(indexManager.getNumSegments(), 2); // should create one segment

        checkSegment(0, seg0Docs);
        checkSegment(1, seg1Docs);


    }


    @Test
    public void verifyContextTest2() throws Exception{
        // store the original threshold
        int old_threshold = InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD;


        Tokenizer tokenizer = new PunctuationTokenizer();
        Stemmer stemmer = new PorterStemmer();
        analyzer = new ComposableAnalyzer(tokenizer, stemmer); // create composable analyzer for documents tokenization

        indexManager = InvertedIndexManager.createOrOpen(folder,analyzer);

        int flush_threshold = 50; // our flush threshold
        int total_docs = 200; // total documents we gonna add

        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = flush_threshold;

        // the documents are created in the combination of these vocabularies
        ArrayList<String> vocabs = new ArrayList<>();
        vocabs.add("apple");
        vocabs.add("banana");
        vocabs.add("cat");
        vocabs.add("dinner");
        vocabs.add("elephant");


        ArrayList<ArrayList<Document>> segs = new ArrayList<>();
        for (int i = 0; i < total_docs/flush_threshold; i++){
            segs.add(new ArrayList<>());
        }


        // generate random documents and add to indexManager

        for (int i = 0; i < total_docs; i++){

            String[] words = new String [10]; // each document has 10 words

            // generates random text from vocabs
            for ( int j = 0; j < 10; j++){
                Random random = new Random();
                words[j] = vocabs.get(random.nextInt(vocabs.size()));
            }

            String text = String.join(" ",words);
            Document d = new Document(text);
            indexManager.addDocument(d);

            int index = i / flush_threshold;
            segs.get(index).add(d);
        }

        // check the number of segment
        assertEquals(indexManager.getNumSegments(), total_docs/flush_threshold);

        // check the context of each segment
        for (int i = 0; i < total_docs; i++){
            int index = i/ flush_threshold;
            checkSegment(index, segs.get(index));
        }

        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = old_threshold;

    }

    /* This test checks two aspects of the flush function.
       1. Check if flush does nothing if no document added
       2. Create an empty document, and check if flush added the document properly, with empty posting list.
    */
    @Test
    public void checkEmptyTest(){
        Tokenizer tokenizer = new WordBreakTokenizer();
        Stemmer stemmer = new PorterStemmer();
        analyzer = new ComposableAnalyzer(tokenizer, stemmer);
        indexManager = InvertedIndexManager.createOrOpen(folder,analyzer);

        // check if flush does nothing when no docs added
        indexManager.flush();
        assertEquals(indexManager.getNumSegments(), 0);

        // check if empty string document is added properly, but with empty inverted index
        Document doc1 = new Document("");
        ArrayList<Document> docs = new ArrayList<>();
        docs.add(doc1);
        indexManager.addDocument(doc1);
        indexManager.flush();
        assertEquals(indexManager.getNumSegments(), 1);
        checkSegment(0, docs);
    }

    // This testcase tests if the system automatically flush when reaching the threshold
    @Test
    public void checkThresoldFlushTest(){
        Tokenizer tokenizer = new PunctuationTokenizer();
        Stemmer stemmer = new PorterStemmer();
        analyzer = new ComposableAnalyzer(tokenizer, stemmer); // create composable analyzer for documents tokenization
        indexManager = InvertedIndexManager.createOrOpen(folder,analyzer);

        // reset the default_flush_threshold
        int old_threshold = indexManager.DEFAULT_FLUSH_THRESHOLD;
        indexManager.DEFAULT_FLUSH_THRESHOLD = 5;
        int new_threshold = indexManager.DEFAULT_FLUSH_THRESHOLD;
        int numDocs = 23;
        ArrayList<Document> docs = new ArrayList<>(); // ground true docs
        for (int i = 0; i < numDocs; i++){
            Document doc = new Document(String.valueOf(i));
            indexManager.addDocument(doc);
            docs.add(doc);
            // reset ground true docs when reach threshold
            if (i % new_threshold == new_threshold - 1){
                checkSegment(indexManager.getNumSegments()-1, docs);
                docs = new ArrayList<>();
            }
        }
        assertEquals(indexManager.getNumSegments(), numDocs/new_threshold);
        indexManager.DEFAULT_FLUSH_THRESHOLD = old_threshold; // change back to original threshold
    }


    // check if read/writer counter updated properly
    // we check whether the pages take the space at least the length of the keywords

    @Test
    public void checkPageIOTest(){
        Tokenizer tokenizer = new PunctuationTokenizer();
        Stemmer stemmer = new PorterStemmer();
        analyzer = new ComposableAnalyzer(tokenizer, stemmer); // create composable analyzer for documents tokenization
        indexManager = InvertedIndexManager.createOrOpen(folder,analyzer);

        // create documents with 4096 characters
        char[] chars = new char[PageFileChannel.PAGE_SIZE];
        Arrays.fill(chars, 'a');
        String text = new String(chars);
        Document doc1 = new Document(text);

        chars = new char[PageFileChannel.PAGE_SIZE];
        Arrays.fill(chars, 'b');
        text = new String(chars);
        Document doc2 = new Document(text);

        chars = new char[PageFileChannel.PAGE_SIZE];
        Arrays.fill(chars, 'c');
        text = new String(chars);
        Document doc3 = new Document(text);

        // set up ground true documents
        ArrayList<Document> docs = new ArrayList<>();
        docs.add(doc1);
        docs.add(doc2);
        docs.add(doc3);

        // add documents
        indexManager.addDocument(doc1);
        indexManager.addDocument(doc2);
        indexManager.addDocument(doc3);
        indexManager.flush();

        // check segment number and write counter
        assertEquals(indexManager.getNumSegments(), 1);
        assertTrue(PageFileChannel.writeCounter >= 3);

        // check segment context and read counter
        checkSegment(0, docs);
        assertTrue(PageFileChannel.readCounter >= 3);
    }



    /*
    This function create the ground truth of "one segments" of given documents lists
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
        indexManagerTest = indexManager.getIndexSegment(segmentNum);

        // get inverted list and documents of the segment
        Map<String, List<Integer>> segIndex = indexManagerTest.getInvertedLists();
        Map<Integer, Document> segDoc = indexManagerTest.getDocuments();

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
}
