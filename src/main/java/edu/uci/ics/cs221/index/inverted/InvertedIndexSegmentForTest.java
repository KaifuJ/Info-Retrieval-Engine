package edu.uci.ics.cs221.index.inverted;

import edu.uci.ics.cs221.storage.Document;

import java.util.List;
import java.util.Map;

/**
 * An in-memory representation of an inverted index segment, used *only* for testing purposes.
 */
public class InvertedIndexSegmentForTest {

    private final Map<String, List<Integer>> invertedLists;
    private final Map<Integer, Document> documents;

    public InvertedIndexSegmentForTest(Map<String, List<Integer>> invertedLists, Map<Integer, Document> documents) {
        this.invertedLists = invertedLists;
        this.documents = documents;
    }

    public Map<String, List<Integer>> getInvertedLists() {
        return invertedLists;
    }

    public Map<Integer, Document> getDocuments() {
        return documents;
    }
}
