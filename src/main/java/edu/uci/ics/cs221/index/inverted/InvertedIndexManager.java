package edu.uci.ics.cs221.index.inverted;

import com.google.common.base.Preconditions;
import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.storage.Document;
import edu.uci.ics.cs221.storage.DocumentStore;
import edu.uci.ics.cs221.storage.MapdbDocStore;
//import sun.jvm.hotspot.debugger.Page;

import javax.print.Doc;
import java.io.File;
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
    private static final int PAGE_SIZE = 4096;

    public List<Document> docs;
    public Map<String, List<Integer>> segment;
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
        this.segmentCounter = new File("./inverted_index").list().length / 3;
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
        DocumentStore docStore = MapdbDocStore.createOrOpen("./inverted_index/docStore_" + this.segmentCounter);
        for(int i = 0; i < this.docs.size(); i++){
            docStore.addDocument(i, docs.get(i));
        }
        docStore.close();

        // flush segment (keywords and postingLists)
        List<String> keywordsList = new ArrayList<>(segment.keySet());
        flushKeywords(keywordsList);
        flushPostingLists(keywordsList);

        segmentCounter++;
        docs.clear();
        segment.clear();

//        throw new UnsupportedOperationException();
    }

    private void flushKeywords(List<String> keywords){
        ByteBuffer bf = ByteBuffer.allocate(PAGE_SIZE);
        PageFileChannel pfc = PageFileChannel.createOrOpen(Paths.get("./inverted_index/keywords_" + segmentCounter));

        bf.putInt(0);
        int count = 0;
        for(String keyword : keywords) {
            byte[] kwBytes = keyword.getBytes();

            if(bf.remaining() < 4 + kwBytes.length +4){
                bf.rewind();
                bf.putInt(count);
                count = 0;
                pfc.appendPage(bf);
                bf.clear();
                bf.putInt(0);
            }

            bf.putInt(kwBytes.length);
            bf.put(kwBytes);
            bf.putInt(segment.get(keyword).size());
            count++;
        }
        bf.rewind();
        bf.putInt(count);
        pfc.appendPage(bf);

        pfc.close();
    }

    private void mergeKeywords(int segNum0, int segNum1){
        Set<String> keywordSet0 = getKeywordSet(segNum0);
        Set<String> keywordSet1 = getKeywordSet(segNum1);
        keywordSet0.addAll(keywordSet1);
        List<String> keywords = new ArrayList<>(keywordSet0);

        ByteBuffer bf = ByteBuffer.allocate(PAGE_SIZE);
        PageFileChannel pfc = PageFileChannel.createOrOpen(Paths.get("./inverted_index_new/keywords_" + (segNum0 / 2)));

        bf.putInt(0);
        int count = 0;
        for(String keyword : keywords) {
            byte[] kwBytes = keyword.getBytes();

            if(bf.remaining() < 4 + kwBytes.length +4){
                bf.rewind();
                bf.putInt(count);
                count = 0;
                pfc.appendPage(bf);
                bf.clear();
                bf.putInt(0);
            }
            int size = getPostingList(segNum0, keyword).size() + getPostingList(segNum1, keyword).size();

            bf.putInt(kwBytes.length);
            bf.put(kwBytes);
            bf.putInt(size);
            count++;
        }
        bf.rewind();
        bf.putInt(count);
        pfc.appendPage(bf);

        pfc.close();
    }

    private void flushPostingLists(List<String> keywords){
        ByteBuffer bf = ByteBuffer.allocate(PAGE_SIZE);
        PageFileChannel pfc = PageFileChannel.createOrOpen(Paths.get("./inverted_index/postingLists_" + segmentCounter));

        for(String keyword : keywords){
            List<Integer> postingList = segment.get(keyword);

            for(int id : postingList){
                if(!bf.hasRemaining()){
                    pfc.appendPage(bf);
                    bf.clear();
                }
                bf.putInt(id);
            }
        }
        pfc.appendPage(bf);
        pfc.close();
    }

    private void mergePostingLists(int segNum0, int segNum1, int count){
        int newSegNum = segNum0 / 2;
        ByteBuffer bf = ByteBuffer.allocateDirect(PAGE_SIZE);
        PageFileChannel pfc = PageFileChannel.createOrOpen(Paths.get("./inverted_index_new/docStore" + newSegNum));

        List<String> keywords = getKeywordList(newSegNum, true);

        for(String keyword : keywords){
            List<Integer> list0 = getPostingList(segNum0, keyword);
            List<Integer> list1 = getPostingList(segNum1, keyword);

            for(int i = 0; i < list1.size(); i++){
                list1.set(i, list1.get(i) + count);
            }

            list0.addAll(list1);

            for(int id : list0){
                if(!bf.hasRemaining()){
                    pfc.appendPage(bf);
                    bf.clear();
                }
                bf.putInt(id);
            }
        }
        pfc.appendPage(bf);
        pfc.close();
    }






    /**
     * Merges all the disk segments of the inverted index pair-wise.
     */
    public void mergeAllSegments() {
        // merge only happens at even number of segments
        Preconditions.checkArgument(getNumSegments() % 2 == 0);

        for(int i = 0; i < segmentCounter / 2; i += 2){
            int segNum0 = i;
            int segNum1 = i + 1;
            int newSegNum = i / 2;

            // merge docStore
            DocumentStore docStore0 = MapdbDocStore.createOrOpen("./inverted_index/docStore_" + segNum0);
            DocumentStore docStore1 = MapdbDocStore.createOrOpen("./inverted_index/docStore_" + segNum1);

            DocumentStore newDocStore = MapdbDocStore.createOrOpen("./inverted_index_new/docStore_" + newSegNum);

            Iterator<Map.Entry<Integer, Document>> itera0 = docStore0.iterator();
            int count = 0;
            while(itera0.hasNext()){
                Map.Entry<Integer, Document> entry = itera0.next();
                newDocStore.addDocument(entry.getKey(), entry.getValue());
                count++;
            }

            Iterator<Map.Entry<Integer, Document>> itera1 = docStore0.iterator();
            while(itera1.hasNext()){
                Map.Entry<Integer, Document> entry = itera1.next();
                newDocStore.addDocument(entry.getKey() + count, entry.getValue());
            }

            docStore0.close();
            docStore1.close();
            newDocStore.close();


            // merge keywords
            mergeKeywords(segNum0, segNum1);


            // merge posting list
            mergePostingLists(segNum0, segNum1, count);
        }

        segmentCounter /= 2;
        File oldDir = new File("./inverted_index");
        oldDir.renameTo(new File("./inverted_index_old"));

        File newDir = new File("./inverted_index_new");
        newDir.renameTo(new File("./inverted_index"));

//        throw new UnsupportedOperationException();
    }





    private List<String> getKeywordList(int segNum, boolean newDir){
        String keywordsFile = null;
        if(newDir) keywordsFile = "./inverted_index_new/keywords_" + segNum;
        else keywordsFile = "./inverted_index/keywords_" + segNum;

        PageFileChannel pfc = PageFileChannel.createOrOpen(Paths.get(keywordsFile));
        int pages = pfc.getNumPages();
        List<String> keywords = new ArrayList<>();

        for(int i = 0; i < pages; i++){
            ByteBuffer bf = pfc.readPage(i);

            int items = bf.getInt();
            for(int j = 0; j < items; j++){
                int kwBytesLength = bf.getInt();
                byte[] kwBytes = new byte[kwBytesLength];
                bf.get(kwBytes);
                String keyword = new String(kwBytes);
                keywords.add(keyword);
                bf.position(bf.position() + 4);
            }
        }
        return keywords;
    }

    private Set<String> getKeywordSet(int segNum){
        List<String> keywordList = getKeywordList(segNum, false);
        Set<String> keywordSet = new HashSet<>(keywordList);
        return keywordSet;
    }

    private List<Integer> getSizeList(int segNum){
        String keywordsFile = "./inverted_index/keywords_" + segNum;

        PageFileChannel pfc = PageFileChannel.createOrOpen(Paths.get(keywordsFile));
        int pages = pfc.getNumPages();
        List<Integer> sizes = new ArrayList<>();

        for(int i = 0; i < pages; i++){
            ByteBuffer bf = pfc.readPage(i);

            int items = bf.getInt();
            for(int j = 0; j < items; j++){
                int kwBytesLength = bf.getInt();
                bf.position(bf.position() + kwBytesLength);
                sizes.add(bf.getInt());
            }
        }
        return sizes;
    }

    private List<Integer> getPostingList(int segNum, String keyword){
        List<String> keywordList = getKeywordList(segNum, false);
        List<Integer> sizeList = getSizeList(segNum);

        int index = -1;
        for(int i = 0; i < keywordList.size(); i++){
            if(keyword.equals(keywordList.get(i))){
                index = i;
                break;
            }
        }

        List<Integer> postingList = new ArrayList<>();
        if(index == -1) return postingList;

        String postingListFile = "./inverted_index/postingLists_" + segNum;
        PageFileChannel pfc = PageFileChannel.createOrOpen(Paths.get(postingListFile));

        int globalOffset = 0;
        for(int i = 0; i < index; i++) globalOffset += 4 * sizeList.get(i);

        int pageNum = globalOffset / PAGE_SIZE;
        int localOffset = globalOffset % PAGE_SIZE;

        ByteBuffer bf = pfc.readPage(pageNum);
        bf.position(localOffset);

        for(int i = 0; i < sizeList.get(index); i++){
            if(!bf.hasRemaining()){
                pageNum++;
                bf = pfc.readPage(pageNum);
            }
            postingList.add(bf.getInt());
        }

        return postingList;
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



//class Test{
//    public static void main(String[] args){
//        InvertedIndexManager iim = InvertedIndexManager.createOrOpen("./", new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()));
//
//        iim.docs.add(new Document("cat dog"));
//        iim.docs.add(new Document("dog shit"));
//
//        List<Integer> cat = new ArrayList<>();
//        List<Integer> dog = new ArrayList<>();
//        List<Integer> shit = new ArrayList<>();
//
//        cat.add(0);
//        dog.add(0);
//        dog.add(1);
//        shit.add(1);
//
//        iim.segment.put("cat", cat);
//        iim.segment.put("dog", dog);
//        iim.segment.put("shit", shit);
//
//        iim.flush();
//
//        List<String> keywords = iim.getKeywordList(0);
//        System.out.println(keywords.size());
//
//        for(String str : keywords){
//            System.out.println(str);
//        }
//
//        List<Integer> postingList = iim.getPostingList(0, "dog");
//        System.out.println(postingList.size());
//
//        for(int id : postingList){
//            System.out.println(id);
//        }
//
//    }
//}






















