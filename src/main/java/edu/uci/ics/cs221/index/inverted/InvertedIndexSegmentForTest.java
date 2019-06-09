
package edu.uci.ics.cs221.index.inverted;

import edu.uci.ics.cs221.storage.Document;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvertedIndexSegmentForTest that = (InvertedIndexSegmentForTest) o;
        return Objects.equals(invertedLists, that.invertedLists) &&
                Objects.equals(documents, that.documents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invertedLists, documents);
    }

    @Override
    public String toString() {
        return "InvertedIndexSegmentForTest{" +
                "invertedLists=" + invertedLists +
                ", documents=" + documents +
                '}';
    }
}