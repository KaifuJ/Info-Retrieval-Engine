package edu.uci.ics.cs221.index.inverted;

import com.google.common.base.Preconditions;
import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.storage.Document;
import edu.uci.ics.cs221.storage.DocumentStore;
import edu.uci.ics.cs221.storage.MapdbDocStore;
import sun.jvm.hotspot.debugger.Page;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class manages an disk-based inverted index and all the documents in the inverted index.
 *
 * Please refer to the project 2 wiki page for implementation guidelines.
 */
public class InvertedIndexManager {
    private List<Document> docs;
    private Map<String, List<Integer>> segment;
    private int segmentCounter;

    /**
     * The default flush threshold, in terms of number of documents.
     * For example, a new Segment should be automatically created whenever there's 1000 documents in the buffer.
     *
     * In test cases, the default flush threshold could possibly be set to any number.
     */
    public static int DEFAULT_FLUSH_THRESHOLD = 1000;

    /**
     * The default merge threshold, in terms of number of segments in the inverted index.
     * When the number of segments reaches the threshold, a merge should be automatically triggered.
     *
     * In test cases, the default merge threshold could possibly be set to any number.
     */
    public static int DEFAULT_MERGE_THRESHOLD = 8;


    private InvertedIndexManager(String indexFolder, Analyzer analyzer) {
        this.docs = new ArrayList<>();
        this.segment = new HashMap<>();
        this.segmentCounter = 0;
    }

    /**
     * Creates an inverted index manager with the folder and an analyzer
     */
    public static InvertedIndexManager createOrOpen(String indexFolder, Analyzer analyzer) {
        try {
            Path indexFolderPath = Paths.get(indexFolder);
            if (Files.exists(indexFolderPath) && Files.isDirectory(indexFolderPath)) {
                if (Files.isDirectory(indexFolderPath)) {
                    return new InvertedIndexManager(indexFolder, analyzer);
                } else {
                    throw new RuntimeException(indexFolderPath + " already exists and is not a directory");
                }
            } else {
                Files.createDirectories(indexFolderPath);
                return new InvertedIndexManager(indexFolder, analyzer);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Adds a document to the inverted index.
     * Document should live in a in-memory buffer until `flush()` is called to write the segment to disk.
     * @param document
     */
    public void addDocument(Document document) {
        throw new UnsupportedOperationException();
    }

    /**
     * Flushes all the documents in the in-memory segment buffer to disk. If the buffer is empty, it should not do anything.
     * flush() writes the segment to disk containing the posting list and the corresponding document store.
     */
    public void flush() {
        // flush docs
        DocumentStore docStore = MapdbDocStore.createOrOpen("./docStore_" + this.segmentCounter);
        for(int i = 0; i < this.docs.size(); i++){
            docStore.addDocument(i, docs.get(i));
        }
        docStore.close();

        // flush segment (keywords and lists)
        PageFileChannel pfc = PageFileChannel.createOrOpen(Paths.get("./segment_" + this.segmentCounter));

        int BUFFER_SIZE = 4096;
        ByteBuffer bf = ByteBuffer.allocate(BUFFER_SIZE);
        List<List<Integer>> lists = new ArrayList<>(segment.size());


        // flush keywords
        for(String keyword : segment.keySet()){
            byte[] kwBytes = keyword.getBytes();

            if(bf.remaining() < 4 + kwBytes.length) flushAndClearBufferContent(bf, pfc);
            bf.putInt(kwBytes.length);
            bf.put(kwBytes);

            lists.add(segment.get(keyword));
        }
        if(bf.remaining() < 4) flushAndClearBufferContent(bf, pfc);
        bf.putInt(-1);
        flushAndClearBufferContent(bf, pfc);


        // flush posting lists
        for(List<Integer> list : lists){
            if(bf.remaining() < 4) flushAndClearBufferContent(bf, pfc);
            bf.putInt(list.size());

            for(int id : list){
                if(bf.remaining() < 4) flushAndClearBufferContent(bf, pfc);
                bf.putInt(id);
            }
        }
        flushAndClearBufferContent(bf, pfc);

        segmentCounter++;

//        throw new UnsupportedOperationException();
    }

    private void flushAndClearBufferContent(ByteBuffer bf, PageFileChannel pfc){
        byte[] content = Arrays.copyOf(bf.array(), bf.position());
        ByteBuffer contentBf = ByteBuffer.wrap(content);
        pfc.appendAllBytes(contentBf);
        bf.clear();
    }

    /**
     * Merges all the disk segments of the inverted index pair-wise.
     */
    public void mergeAllSegments() {
        // merge only happens at even number of segments
        Preconditions.checkArgument(getNumSegments() % 2 == 0);
        throw new UnsupportedOperationException();
    }

    /**
     * Performs a single keyword search on the inverted index.
     * You could assume the analyzer won't convert the keyword into multiple tokens.
     * If the keyword is empty, it should not return anything.
     *
     * @param keyword keyword, cannot be null.
     * @return a iterator of documents matching the query
     */
    public Iterator<Document> searchQuery(String keyword) {
        Preconditions.checkNotNull(keyword);

        throw new UnsupportedOperationException();
    }

    /**
     * Performs an AND boolean search on the inverted index.
     *
     * @param keywords a list of keywords in the AND query
     * @return a iterator of documents matching the query
     */
    public Iterator<Document> searchAndQuery(List<String> keywords) {
        Preconditions.checkNotNull(keywords);

        throw new UnsupportedOperationException();
    }

    /**
     * Performs an OR boolean search on the inverted index.
     *
     * @param keywords a list of keywords in the OR query
     * @return a iterator of documents matching the query
     */
    public Iterator<Document> searchOrQuery(List<String> keywords) {
        Preconditions.checkNotNull(keywords);

        throw new UnsupportedOperationException();
    }

    /**
     * Iterates through all the documents in all disk segments.
     */
    public Iterator<Document> documentIterator() {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes all documents in all disk segments of the inverted index that match the query.
     * @param keyword
     */
    public void deleteDocuments(String keyword) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the total number of segments in the inverted index.
     * This function is used for checking correctness in test cases.
     *
     * @return number of index segments.
     */
    public int getNumSegments() {
        throw new UnsupportedOperationException();
    }

    /**
     * Reads a disk segment into memory based on segmentNum.
     * This function is mainly used for checking correctness in test cases.
     *
     * @param segmentNum n-th segment in the inverted index (start from 0).
     * @return in-memory data structure with all contents in the index segment, null if segmentNum don't exist.
     */
    public InvertedIndexSegmentForTest getIndexSegment(int segmentNum) {
        throw new UnsupportedOperationException();
    }


}
