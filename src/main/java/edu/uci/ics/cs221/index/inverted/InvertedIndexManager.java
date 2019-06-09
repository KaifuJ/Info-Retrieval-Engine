package edu.uci.ics.cs221.index.inverted;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import edu.uci.ics.cs221.analysis.*;
import edu.uci.ics.cs221.storage.Document;
import edu.uci.ics.cs221.storage.DocumentStore;
import edu.uci.ics.cs221.storage.MapdbDocStore;

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
    public Table<String, Integer, List<Integer>> positions;

    private int segmentCounter;

    public List<Document> SearchDoc;

    private Analyzer analyzer;
    private Compressor compressor;

    private String path;

    public static int DEFAULT_FLUSH_THRESHOLD = 1000;
    public static int DEFAULT_MERGE_THRESHOLD = 8;




    private InvertedIndexManager(String indexFolder, Analyzer analyzer, Compressor compressor) {
        this.docs = new ArrayList<>();
        this.segment = new HashMap<>();
        this.positions = HashBasedTable.create();
        this.segmentCounter = 0;

        this.analyzer = analyzer;
        this.compressor = compressor;

        this.path = indexFolder;
        this.SearchDoc = new ArrayList<>();
    }

    public static InvertedIndexManager createOrOpen(String indexFolder, Analyzer analyzer) {
        try {
            Path indexFolderPath = Paths.get(indexFolder);
            if (Files.exists(indexFolderPath) && Files.isDirectory(indexFolderPath)) {
                if (Files.isDirectory(indexFolderPath)) {
                    return new InvertedIndexManager(indexFolder, analyzer, new NaiveCompressor());
                } else {
                    throw new RuntimeException(indexFolderPath + " already exists and is not a directory");
                }
            } else {
                Files.createDirectories(indexFolderPath);
                return new InvertedIndexManager(indexFolder, analyzer, new NaiveCompressor());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static InvertedIndexManager createOrOpenPositional(String indexFolder, Analyzer analyzer, Compressor compressor){
        try {
            Path indexFolderPath = Paths.get(indexFolder);
            if (Files.exists(indexFolderPath) && Files.isDirectory(indexFolderPath)) {
                if (Files.isDirectory(indexFolderPath)) {
                    return new InvertedIndexManager(indexFolder, analyzer, compressor);
                } else {
                    throw new RuntimeException(indexFolderPath + " already exists and is not a directory");
                }
            } else {
                Files.createDirectories(indexFolderPath);
                return new InvertedIndexManager(indexFolder, analyzer, compressor);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }





    public void addDocument(Document document) {
        List<String> wordList = this.analyzer.analyze(document.getText());
        this.docs.add(document);

        for(int i = 0; i < wordList.size(); i++){
            if(this.segment.containsKey(wordList.get(i))){
                List<Integer> postingList = segment.get(wordList.get(i));
                if(postingList.get(postingList.size()-1)!=docs.size()-1)
                    postingList.add(docs.size()-1);
            }
            else {
                segment.put(wordList.get(i),new ArrayList<>(Arrays.asList(docs.size()-1)));
            }

            if(positions.contains(wordList.get(i),docs.size()-1)){
                positions.get(wordList.get(i),docs.size()-1).add(i);
            }else{
                positions.put(wordList.get(i),docs.size()-1,new ArrayList(Arrays.asList(i)));
            }
        }

        if(this.docs.size() == this.DEFAULT_FLUSH_THRESHOLD){
            flush();
        }

    }





    public void flush() {
        // flush docs
        if(docs.size() == 0) return;
        int docCount = this.docs.size();
        DocumentStore docStore = MapdbDocStore.createOrOpen(path+"/docStore_" + this.segmentCounter);
        for(int i = 0; i < this.docs.size(); i++){
            docStore.addDocument(i, docs.get(i));
        }
        docStore.close();


        // flush keywords, posting lists, and position lists
        PageFileChannel posiPfc = PageFileChannel.createOrOpen(Paths.get(path + "/positions_" + segmentCounter));
        ByteBuffer posiBf = ByteBuffer.allocate(PAGE_SIZE);
        int posiOffset = 0;

        PageFileChannel postPfc = PageFileChannel.createOrOpen(Paths.get(path + "/postings_" + segmentCounter));
        ByteBuffer postBf = ByteBuffer.allocate(PAGE_SIZE);
        List<Integer> offInPost = new ArrayList<>();
        List<Integer> tfList = new ArrayList<>();
        int postOffset = 0;

        PageFileChannel keyPfc = PageFileChannel.createOrOpen(Paths.get(path + "/keywords_" + segmentCounter));
        ByteBuffer keyBf = ByteBuffer.allocate(PAGE_SIZE);

        List<String> keywords = new ArrayList<>(segment.keySet());
        keyBf.putInt(docCount);
        keyBf.putInt(keywords.size());

        for(String keyword : keywords){
            List<Integer> postList = segment.get(keyword);
            offInPost.add(posiOffset);

            for(int docId : postList){
                List<Integer> posiList = positions.get(keyword, docId);
                posiOffset = flushPosiList(posiPfc, posiBf, posiList, posiOffset);
                offInPost.add(posiOffset);
                tfList.add(posiList.size());
            }

            int[] offInKey = flushPostList(postPfc, postBf, postList, offInPost, tfList, postOffset);
            postOffset = offInKey[3];
            offInPost.clear();
            tfList.clear();

            flushKeyword(keyPfc, keyBf, keyword, offInKey);
        }

        if(posiBf.position() != 0) posiPfc.appendPage(posiBf);
        if(postBf.position() != 0) postPfc.appendPage(postBf);
        if(keyBf.position() != 0) keyPfc.appendPage(keyBf);

        posiPfc.close();
        postPfc.close();
        keyPfc.close();

        this.segmentCounter++;
        this.docs.clear();
        this.segment.clear();
        this.positions.clear();

        if(segmentCounter == DEFAULT_MERGE_THRESHOLD) this.mergeAllSegments();
    }

    private void flushKeyword(PageFileChannel keyPfc, ByteBuffer keyBf, String keyword, int[] offInKey){
        byte[] kwBytes = keyword.getBytes();

        // put length and 3 offsets
        if(keyBf.remaining() < 16){
            keyPfc.appendPage(keyBf);
            keyBf.clear();
        }
        keyBf.putInt(kwBytes.length);
        keyBf.putInt(offInKey[0]);
        keyBf.putInt(offInKey[1]);
        keyBf.putInt(offInKey[2]);

        // put keywords
        int start = 0;
        while(keyBf.remaining() < kwBytes.length - start){
            int putLength = keyBf.remaining();
            keyBf.put(kwBytes, start, putLength);
            keyPfc.appendPage(keyBf);
            keyBf.clear();

            start += putLength;
        }
        keyBf.put(kwBytes, start, kwBytes.length - start);
    }

    private int[] flushPostList(PageFileChannel postPfc, ByteBuffer postBf,
                                List<Integer> postList, List<Integer> offInPost, List<Integer> tfList,
                                int postOffset){
        int[] offsets = new int[4];
        offsets[0] = postOffset;

        byte[] encodedPostList = compressor.encode(postList);
        byte[] encodedOffList = compressor.encode(offInPost);
        int start = 0;

        while(postBf.remaining() < encodedPostList.length - start){
            int putLength = postBf.remaining();
            postBf.put(encodedPostList, start, putLength);
            postPfc.appendPage(postBf);
            postBf.clear();

            start += putLength;
            postOffset += putLength;
        }
        postBf.put(encodedPostList, start, encodedPostList.length - start);
        postOffset += encodedPostList.length - start;

        offsets[1] = postOffset;

        start = 0;

        while(postBf.remaining() < encodedOffList.length - start){
            int putLength = postBf.remaining();
            postBf.put(encodedOffList, start, putLength);
            postPfc.appendPage(postBf);
            postBf.clear();

            start += putLength;
            postOffset += putLength;
        }
        postBf.put(encodedOffList, start, encodedOffList.length - start);
        postOffset += encodedOffList.length - start;

        offsets[2] = postOffset;

        // flush tfList
        for(int tf : tfList){
            if(postBf.remaining() < 4){
                postOffset += postBf.remaining();

                postPfc.appendPage(postBf);
                postBf.clear();
            }
            postBf.putInt(tf);
            postOffset += 4;
        }
        offsets[3] = postOffset;

        return offsets;
    }

    private int flushPosiList(PageFileChannel posiPfc, ByteBuffer posiBf, List<Integer> posiList, int posiOffset){
        byte[] encodedPosiList = compressor.encode(posiList);
        int start = 0;

        while(posiBf.remaining() < encodedPosiList.length - start){
            int putLength = posiBf.remaining();
            posiBf.put(encodedPosiList, start, putLength);
            posiPfc.appendPage(posiBf);
            posiBf.clear();

            start += putLength;
            posiOffset += putLength;
        }
        posiBf.put(encodedPosiList, start, encodedPosiList.length - start);
        posiOffset += encodedPosiList.length - start;
        return posiOffset;
    }






    private Map<String, int[]> getKeywordsMap(int segNum){
        Map<String, int[]> keywordsMap = new HashMap<>();

        PageFileChannel keyPfc = PageFileChannel.createOrOpen(Paths.get(path + "/keywords_" + segNum));
        int pages = keyPfc.getNumPages();
        int pageNum = 0;

        ByteBuffer keyBf = keyPfc.readPage(pageNum);
        pageNum++;
        keyBf.position(4); // skip docCount
        int entries = keyBf.getInt();

        for(int i = 0; i < entries; i++){
            if(keyBf.remaining() < 16){
                keyBf = keyPfc.readPage(pageNum);
                pageNum++;
            }
            int length = keyBf.getInt();
            int[] offsets = new int[3];
            offsets[0] = keyBf.getInt();
            offsets[1] = keyBf.getInt();
            offsets[2] = keyBf.getInt();


            byte[] kwBytes = new byte[length];
            int start = 0;

            while(keyBf.remaining() < length - start){
                int getLength = keyBf.remaining();
                keyBf.get(kwBytes, start, getLength);
                start += getLength;
            }
            keyBf.get(kwBytes, start, length - start);

            String keyword = new String(kwBytes);
            keywordsMap.put(keyword, offsets);
        }
        keyPfc.close();
        return keywordsMap;
    }

    public Set<String> getKeywordsSet(int segNum){
        Map<String, int[]> keywordsMap = getKeywordsMap(segNum);
        return new HashSet<>(keywordsMap.keySet());
    }



    public List<Integer> getPostOffTf(String keyword, int segNum){
        PageFileChannel postPfc = PageFileChannel.createOrOpen(Paths.get(path + "/postings_" + segNum));
        Map<String, int[]> keywordsMap = getKeywordsMap(segNum);

        int[] offsets = null;
        if(keywordsMap.containsKey(keyword)){
            offsets = keywordsMap.get(keyword);
        }else{
            return new ArrayList<>();
        }


        int pageNum = offsets[0] / PAGE_SIZE;
        int pageOffset = offsets[0] % PAGE_SIZE;
        ByteBuffer postBf = postPfc.readPage(pageNum);
        pageNum++;
        postBf.position(pageOffset);

        int totalSize = offsets[2] - offsets[0];
        byte[] idsAndOffs = new byte[totalSize];

        for(int i = 0; i < totalSize; i++){
            if(!postBf.hasRemaining()){
                postBf = postPfc.readPage(pageNum);
                pageNum++;
            }
            idsAndOffs[i] = postBf.get();
        }

        List<Integer> postAndOffs = new ArrayList<>();
        postAndOffs.addAll(compressor.decode(idsAndOffs, 0, offsets[1] - offsets[0]));
        postAndOffs.addAll(compressor.decode(idsAndOffs, offsets[1] - offsets[0], offsets[2] - offsets[1]));

        int tfCount = postAndOffs.size() / 2;
        for(int i = 0; i < tfCount; i++){
            if(postBf.remaining() < 4){
                postBf = postPfc.readPage(pageNum);
                pageNum++;
            }
            postAndOffs.add(postBf.getInt());
        }

        postPfc.close();
        return postAndOffs;
    }

    public List<Integer> getPostList(List<Integer> postOffTf){
        List<Integer> postList = new ArrayList<>(postOffTf.size() / 3);

        for(int i = 0; i < postOffTf.size() / 3; i++){
            postList.add(postOffTf.get(i));
        }
        return postList;
    }

    private List<Integer> getPostOffsets(List<Integer> postOffTf){
        List<Integer> offsets = new ArrayList<>(postOffTf.size() / 3 + 1);

        for(int i = postOffTf.size() / 3; i < postOffTf.size() - postOffTf.size() / 3; i++){
            offsets.add(postOffTf.get(i));
        }
        return offsets;
    }

    private List<Integer> getPostTf(List<Integer> postOffTf){
        List<Integer> tfList = new ArrayList<>(postOffTf.size() / 3);

        for(int i = postOffTf.size() - postOffTf.size() / 3; i < postOffTf.size(); i++){
            tfList.add(postOffTf.get(i));
        }
        return tfList;
    }



    public List<Integer> getPositionList(String keyword, int docId, int segNum){
        List<Integer> postOffTf = getPostOffTf(keyword, segNum);
        List<Integer> postList = getPostList(postOffTf);
        List<Integer> offsets = getPostOffsets(postOffTf);

        int start = -1;
        int end = -1;
        for(int i = 0; i < postList.size(); i++){
            if(postList.get(i) == docId){
                start = offsets.get(i);
                end = offsets.get(i + 1);
                break;
            }
        }

        PageFileChannel posiPfc = PageFileChannel.createOrOpen(Paths.get(path + "/positions_" + segNum));

        int pageNum = start / PAGE_SIZE;
        ByteBuffer posiBf = posiPfc.readPage(pageNum);
        pageNum++;

        int pageOffset = start % PAGE_SIZE;
        posiBf.position(pageOffset);

        int length = end - start;

        byte[] posiBytes = new byte[length];

        for(int i = 0; i < length; i++){
            if(!posiBf.hasRemaining()){
                posiBf = posiPfc.readPage(pageNum);
                pageNum++;
            }
            posiBytes[i] = posiBf.get();
        }

        posiPfc.close();
        return compressor.decode(posiBytes);
    }






    public void mergeAllSegments() {
        // merge only happens at even number of segments
        Preconditions.checkArgument(getNumSegments() % 2 == 0);


        for(int i = 0; i < segmentCounter ; i += 2){
            int segNum0 = i;
            int segNum1 = i + 1;
            int newSegNum = i / 2;

            // merge docStore
            DocumentStore docStore0 = MapdbDocStore.createOrOpen(path+"/docStore_" + segNum0);
            DocumentStore docStore1 = MapdbDocStore.createOrOpen(path+"/docStore_" + segNum1);

            DocumentStore newDocStore = MapdbDocStore.createOrOpen(path+"/new_docStore_"+newSegNum);

            Iterator<Map.Entry<Integer, Document>> itera0 = docStore0.iterator();
            int count = 0;
            while(itera0.hasNext()){
                Map.Entry<Integer, Document> entry = itera0.next();
                newDocStore.addDocument(entry.getKey(), entry.getValue());
                count++;
            }

            Iterator<Map.Entry<Integer, Document>> itera1 = docStore1.iterator();
            while(itera1.hasNext()){
                Map.Entry<Integer, Document> entry = itera1.next();
                newDocStore.addDocument(entry.getKey() + count, entry.getValue());
            }

            docStore0.close();
            docStore1.close();
            newDocStore.close();



            // merge keywords, posting lists, and position lists
            PageFileChannel posiPfc = PageFileChannel.createOrOpen(Paths.get(path + "/new_positions_" + newSegNum));
            ByteBuffer posiBf = ByteBuffer.allocate(PAGE_SIZE);
            int posiOffset = 0;

            PageFileChannel postPfc = PageFileChannel.createOrOpen(Paths.get(path + "/new_postings_" + newSegNum));
            ByteBuffer postBf = ByteBuffer.allocate(PAGE_SIZE);
            List<Integer> offInPost = new ArrayList<>();
            List<Integer> tfList = new ArrayList<>();
            int postOffset = 0;

            PageFileChannel keyPfc = PageFileChannel.createOrOpen(Paths.get(path + "/new_keywords_" + newSegNum));
            ByteBuffer keyBf = ByteBuffer.allocate(PAGE_SIZE);


            Set<String> keywordSet = getKeywordsSet(segNum0);
            keywordSet.addAll(getKeywordsSet(segNum1));
            keyBf.putInt(getNumDocuments(segNum0) + getNumDocuments(segNum1));
            keyBf.putInt(keywordSet.size());

            for(String keyword : keywordSet){
                List<Integer> postList = getPostList(getPostOffTf(keyword, segNum0));
                List<Integer> postList1 = getPostList(getPostOffTf(keyword, segNum1));
                for(int j = 0; j < postList1.size(); j++){
                    postList1.set(j, postList1.get(j) + count);
                }
                postList.addAll(postList1);

                offInPost.add(posiOffset);
                for(int docId : postList){
                    List<Integer> posiList = getPositionList(keyword,
                                                            docId >= count ? docId - count : docId,
                                                            docId >= count ? segNum1 : segNum0);
                    posiOffset = flushPosiList(posiPfc, posiBf, posiList, posiOffset);
                    tfList.add(posiList.size());
                    offInPost.add(posiOffset);
                }

                int[] offInKey = flushPostList(postPfc, postBf, postList, offInPost, tfList, postOffset);
                postOffset = offInKey[3];
                tfList.clear();
                offInPost.clear();

                flushKeyword(keyPfc, keyBf, keyword, offInKey);
            }
            if(posiBf.position() != 0) posiPfc.appendPage(posiBf);
            if(postBf.position() != 0) postPfc.appendPage(postBf);
            if(keyBf.position() != 0) keyPfc.appendPage(keyBf);

            posiPfc.close();
            postPfc.close();
            keyPfc.close();


            // clean up
            cleanUpAfterMerge(segNum0, segNum1, newSegNum);
        }

        segmentCounter /= 2;
    }






    public Iterator<Document> searchQuery(String keyword) {
        Preconditions.checkNotNull(keyword);

        SearchDoc.clear();
        keyword = this.analyzer.analyze(keyword).get(0);
        for(int i = 0; i < getNumSegments(); i++){
            if(getKeywordsSet(i).contains(keyword)){
                List<Integer> list = getPostList(getPostOffTf(keyword,i));
                DocumentStore documentStore = MapdbDocStore.createOrOpen(path+"/docStore_" + i);
                for(int index : list){
                    SearchDoc.add(documentStore.getDocument(index));
                }
                documentStore.close();
            }
        }
        return SearchDoc.iterator();
    }

    public List<String> processKeywords(List<String> keywords){
        List<String> keyword = new ArrayList<>();
        for(String keys : keywords){
            keyword.addAll(this.analyzer.analyze(keys));
        }
        return keyword;
    }

    public List<Integer> getCommonDoc(List<String> keywords, int segNum ){
        List<Integer> predoc = getPostList(getPostOffTf(keywords.get(0),segNum));
        if(keywords.size()==1) return predoc;
        List<Integer> doc = new ArrayList<>();
        for(int j = 1; j < keywords.size(); j++) {
            doc.clear();
            List<Integer> temp = getPostList(getPostOffTf(keywords.get(j),segNum));
            int m = 0, n = 0;
            while (m < predoc.size() && n < temp.size()) {
                if (predoc.get(m) > temp.get(n))
                    n++;
                else if (predoc.get(m) < temp.get(n))
                    m++;
                else {
                    doc.add(predoc.get(m));
                    m++;
                    n++;
                }
            }
            predoc.clear();
            predoc.addAll(doc);
        }
        return doc;
    }

    public Iterator<Document> searchAndQuery(List<String> keywords) {
        Preconditions.checkNotNull(keywords);
        SearchDoc.clear();

        List<String> keyword = processKeywords(keywords);
        if(keyword.size() == 0) return SearchDoc.iterator();
        for(int i = 0; i < getNumSegments(); i++){
            Set<String> segKeys = getKeywordsSet(i);
            boolean flag = true;
            for(String keys : keyword){
                flag = segKeys.contains(keys);
            }
            if(flag == false) continue;

            List<Integer> res = getCommonDoc(keyword,i);
            DocumentStore documentStore = MapdbDocStore.createOrOpen(path+"/docStore_" + i);
            for(int index : res){
                SearchDoc.add(documentStore.getDocument(index));
            }
            documentStore.close();

        }
        return SearchDoc.iterator();
    }

    public Iterator<Document> searchOrQuery(List<String> keywords) {
        Preconditions.checkNotNull(keywords);
        SearchDoc.clear();

        List<String> ankeyword = processKeywords(keywords);

        if(ankeyword.size() == 0) return SearchDoc.iterator();
        for(int i = 0; i < getNumSegments(); i++){
            Set<String> segKeys = getKeywordsSet(i);
            Set<Integer> Index = new HashSet<>();

            for(String keyword : ankeyword){
                if(segKeys.contains((keyword))){
                    List<Integer> indexes = getPostList(getPostOffTf(keyword,i));
                    for(int docId : indexes){
                        if(Index.contains(docId)==false) Index.add(docId);
                    }
                }
            }

            DocumentStore documentStore = MapdbDocStore.createOrOpen(path+"/docStore_" + i);
            for(int index : Index){
                SearchDoc.add(documentStore.getDocument(index));
            }
            documentStore.close();
        }
        return SearchDoc.iterator();
    }

    public Iterator<Document> searchPhraseQuery(List<String> phrase) {
        Preconditions.checkNotNull(phrase);
        SearchDoc.clear();
        if(phrase.size() == 0) return SearchDoc.iterator();

        List<String> keywords = processKeywords(phrase);
        for(int i = 0; i < segmentCounter; i++){
            Set<String> segKeys = getKeywordsSet(i);
            boolean flag = true;
            for(String keys : keywords){
                flag = segKeys.contains(keys);
            }
            if(flag == false) continue;
            List<Integer> doc = getCommonDoc(keywords,i);
            if(doc.size() == 0) continue;

            List<Integer> res = new ArrayList<>();
            for(int index : doc){
                List<Integer> prePosition = getPositionList(keywords.get(0),index,i);
                if(keywords.size() == 1){
                    res.add(index);
                    continue;
                }
                List<Integer> position = new ArrayList<>();
                for(int j = 1; j < keywords.size(); j++){
                    position.clear();
                    List<Integer> tmp = getPositionList(keywords.get(j),index,i);
                    int m = 0, n = 0;
                    while(m < prePosition.size() && n < tmp.size()){
                        if (prePosition.get(m) +1 > tmp.get(n))
                            n++;
                        else if (prePosition.get(m) + 1 < tmp.get(n))
                            m++;
                        else {
                            position.add(tmp.get(n));
                            m++;
                            n++;
                        }
                    }
                    prePosition.clear();
                    prePosition.addAll(position);
                }
                if(position.size() > 0) res.add(index);
            }
            DocumentStore documentStore = MapdbDocStore.createOrOpen(path+"/docStore_" + i);
            for(int index : res){
                SearchDoc.add(documentStore.getDocument(index));
            }
            documentStore.close();
        }
        return SearchDoc.iterator();
    }











    /**
     * Performs top-K ranked search using TF-IDF.
     * Returns an iterator that returns the top K documents with highest TF-IDF scores.
     *
     * Each element is a pair of <Document, Double (TF-IDF Score)>.
     *
     * If parameter `topK` is null, then returns all the matching documents.
     *
     * Unlike Boolean Query and Phrase Query where order of the documents doesn't matter,
     * for ranked search, order of the document returned by the iterator matters.
     *
     * @param keywords, a list of keywords in the query
     * @param topK, number of top documents weighted by TF-IDF, all documents if topK is null
     * @return a iterator of top-k ordered documents matching the query
     */
    public Iterator<Pair<Document, Double>> searchTfIdf(List<String> keywords, Integer topK) {
//        throw new UnsupportedOperationException();
        String str = String.join(" ", keywords);
        keywords = this.analyzer.analyze(str);

        Map<String, Integer> queryTf = new HashMap<>();
        for(String keyword : keywords){
            queryTf.put(keyword, queryTf.getOrDefault(keyword, 0) + 1);
        }

        // first pass to calculate idf all query keywords
        // idf = log10(N/df)
        int N = 0;
        Map<String, Double> idfs = new HashMap<>();
        Map<String, Integer> dfs = new HashMap<>();

        for(int i = 0; i < segmentCounter; i++){
            N += getNumDocuments(i);

            for(String keyword : queryTf.keySet()){
                dfs.put(keyword, dfs.getOrDefault(keyword, 0) + getDocumentFrequency(i, keyword));
            }
        }

        for(String keyword : dfs.keySet()){
            idfs.put(keyword, Math.log10((double)N / dfs.get(keyword)));
        }


        // calculate tfidf for query
        Map<String, Double> queryTfIdf = new HashMap<>();
        for(String keyword : queryTf.keySet()){
            queryTfIdf.put(keyword, queryTf.get(keyword) * idfs.get(keyword));
        }


        // second pass to calculate tfidf for each document containing keywords
        PriorityQueue<Pair<DocId, Double>> pq = new PriorityQueue<>(new Comparator<Pair<DocId, Double>>(){
            @Override
            public int compare(Pair<DocId, Double> p1, Pair<DocId, Double> p2){
                return p1.getRight() < p2.getRight() ? -1 : 1;
            } // min-heap
        });

        for(int segNum = 0; segNum < segmentCounter; segNum++){
            Map<DocId, Double> dotProduct = new HashMap<>();
            Map<DocId, Double> vectorLength = new HashMap<>();

            for(String keyword : queryTfIdf.keySet()){

                List<Integer> postOffTf = getPostOffTf(keyword, segNum);
                List<Integer> postList = getPostList(postOffTf);
                List<Integer> tfList = getPostTf(postOffTf);

                for(int i = 0; i < postList.size(); i++){

                    DocId id = new DocId(segNum, postList.get(i));
                    double tfidf = tfList.get(i) * idfs.get(keyword);

                    dotProduct.put(id, dotProduct.getOrDefault(id, 0.0) + tfidf * queryTfIdf.get(keyword));
                    vectorLength.put(id, vectorLength.getOrDefault(id, 0.0) + tfidf * tfidf);
                }

            }

            for(DocId id : dotProduct.keySet()){
                pq.add(new Pair<DocId, Double>(id,
                        vectorLength.get(id) != 0 ?
                        dotProduct.get(id) / Math.sqrt(vectorLength.get(id))
                        : 0));
                if(topK == null ? false : pq.size() > topK){
                    pq.poll();
                }
            }

        }
        // now we have got all the result docIds


        // map docIds to documents
        List<Pair<Document, Double>> res = new ArrayList<>(pq.size());
        int size = pq.size();
        for(int i = 0; i < size; i++){
            Pair<DocId, Double> pair = pq.poll();
            DocumentStore docStore = MapdbDocStore.createOrOpen(path+"/docStore_" + pair.getLeft().segNum);
            res.add(new Pair<Document, Double>(docStore.getDocument(pair.getLeft().docId), pair.getRight()));
            docStore.close();
        }
        int left = 0;
        int right = res.size() - 1;
        while(left < right){
            Pair<Document, Double> temp = res.get(left);
            res.set(left, res.get(right));
            res.set(right, temp);
            left++;
            right--;
        }
        return res.iterator();
    }


    /**
     * Returns the number of documents containing the token within the given segment.
     * The token should be already analyzed by the analyzer. The analyzer shouldn't be applied again.
     */
    public int getDocumentFrequency(int segNum, String token) {
//        throw new UnsupportedOperationException();
        return getPostList(getPostOffTf(token, segNum)).size();
    }

    public int getNumDocuments(int segNum) {
//        throw new UnsupportedOperationException();
        PageFileChannel keyPfc = PageFileChannel.createOrOpen(Paths.get(path + "/keywords_" + segNum));
        ByteBuffer keyBf = keyPfc.readPage(0);
        keyPfc.close();
        return keyBf.getInt();
    }








    private void cleanUpAfterMerge(int segNum0, int segNum1, int newSegNum){
        File temp = null;
        temp = new File(path+"/docStore_"+segNum0);
        temp.delete();
        temp = new File(path+"/docStore_"+segNum1);
        temp.delete();
        temp = new File(path+"/new_docStore_"+newSegNum);
        temp.renameTo(new File(path+"/docStore_"+newSegNum));

        temp = new File(path+"/keywords_"+segNum0);
        temp.delete();
        temp = new File(path+"/keywords_"+segNum1);
        temp.delete();
        temp = new File(path+"/new_keywords_" + newSegNum);
        temp.renameTo(new File(path+"/keywords_" + newSegNum));

        temp = new File(path + "/postings_" + segNum0);
        temp.delete();
        temp = new File(path + "/postings_" + segNum1);
        temp.delete();
        temp = new File(path + "/new_postings_" + newSegNum);
        temp.renameTo(new File(path + "/postings_" + newSegNum));

        temp = new File(path + "/positions_" + segNum0);
        temp.delete();
        temp = new File(path + "/positions_" + segNum1);
        temp.delete();
        temp = new File(path + "/new_positions_" + newSegNum);
        temp.renameTo(new File(path + "/positions_" + newSegNum));
    }

    /**
     * Iterates through all the documents in all disk segments.
     */
    public Iterator<Document> documentIterator() {
        SearchDoc.clear();
        for(int i = 0; i < segmentCounter; i++){
            DocumentStore tmp = MapdbDocStore.createOrOpen(path+"/docStore_" + i);
            System.out.println(tmp.size());
            for(int j = 0; j < tmp.size() ;j++){
                System.out.println(tmp.getDocument(j));
                SearchDoc.add(tmp.getDocument(j));
            }
            tmp.close();
        }
        return SearchDoc.iterator();
    }

    public int getNumSegments() {
        return segmentCounter;
    }

    public InvertedIndexSegmentForTest getIndexSegment(int segmentNum) {
        if(getNumSegments()==0) return null;
        Map<String, List<Integer>> invertedLists = new HashMap<>();
        Map<Integer, Document> documents = new HashMap<>();

        DocumentStore documentStore = MapdbDocStore.createOrOpen(path+"/docStore_" + segmentNum);
        for(int i = 0; i < documentStore.size(); i++){
            documents.put(i,documentStore.getDocument(i));
        }
        documentStore.close();

        Set<String> keyList = getKeywordsSet(segmentNum);
        for(String keyword : keyList){
            invertedLists.put(keyword,getPostList(getPostOffTf(keyword,segmentNum)));
        }

        InvertedIndexSegmentForTest invertedIndexSegmentForTest = new InvertedIndexSegmentForTest(invertedLists,documents);
        return invertedIndexSegmentForTest;
    }

    public PositionalIndexSegmentForTest getIndexSegmentPositional(int segmentNum) {
        if(getNumSegments()==0) return null;
        Map<String, List<Integer>> invertedLists = new HashMap<>();
        Map<Integer, Document> documents = new HashMap<>();
        Table<String, Integer, List<Integer>> positions = HashBasedTable.create();

        DocumentStore documentStore = MapdbDocStore.createOrOpen(path+"/docStore_" + segmentNum);
        for(int i = 0; i < documentStore.size(); i++){
            documents.put(i,documentStore.getDocument(i));

        }

        Set<String> keyList = getKeywordsSet(segmentNum);
        for(String keyword : keyList){
            List<Integer> postAndOffs = getPostOffTf(keyword, segmentNum);
            invertedLists.put(keyword,getPostList(postAndOffs));

            for(int docID : getPostList(postAndOffs)){
                positions.put(keyword,docID,getPositionList(keyword,docID,segmentNum));
            }
        }
        documentStore.close();

        PositionalIndexSegmentForTest positionalIndexSegmentForTest = new PositionalIndexSegmentForTest(invertedLists,documents,positions);
        return positionalIndexSegmentForTest;
    }

}



class Test{
    public static void main(String[] args){
        Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        InvertedIndexManager iim = InvertedIndexManager.createOrOpenPositional("./test", analyzer, new DeltaVarLenCompressor());

        iim.segment.put("key0", new ArrayList<>(Arrays.asList(0, 1, 2)));
        iim.segment.put("key1", new ArrayList<>(Arrays.asList(5, 6, 7)));

        iim.positions.put("key0", 0, Arrays.asList(0, 2, 3));
        iim.positions.put("key0", 1, Arrays.asList(1, 4));
        iim.positions.put("key0", 2, Arrays.asList(5));
        iim.positions.put("key1", 5, Arrays.asList(7, 8));
        iim.positions.put("key1", 6, Arrays.asList(3));
        iim.positions.put("key1", 7, Arrays.asList(0));

        iim.flush();

        Set<String> set = iim.getKeywordsSet(0);
        System.out.println(set.size());

        for(String s : set) System.out.println(s);

        System.out.println("--------------");

        List<Integer> postList = iim.getPostList(iim.getPostOffTf("key1", 0));

        for(int id : postList){
            System.out.println(id);
        }

        System.out.println("--------------");
        List<Integer> posiList = iim.getPositionList("key1", 5, 0);
        for(int posi : posiList) System.out.println(posi);
    }
}



















