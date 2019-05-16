package edu.uci.ics.cs221.index.inverted;

import com.google.common.collect.Table;
import edu.uci.ics.cs221.storage.Document;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An in-memory representation of a positional index segment, used *only* for testing purposes.
 * Only for this testing class, you could load everything into memory.
 */
public class PositionalIndexSegmentForTest {

    private final Map<String, List<Integer>> invertedLists;
    private final Map<Integer, Document> documents;
    private final Table<String, Integer, List<Integer>> positions;

    public PositionalIndexSegmentForTest(
            Map<String, List<Integer>> invertedLists, Map<Integer, Document> documents,
            Table<String, Integer, List<Integer>> positions) {
        this.invertedLists = invertedLists;
        this.documents = documents;
        this.positions = positions;
    }

    public Map<String, List<Integer>> getInvertedLists() {
        return invertedLists;
    }

    public Map<Integer, Document> getDocuments() {
        return documents;
    }

    public Table<String, Integer, List<Integer>> getPositions() {
        return positions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PositionalIndexSegmentForTest that = (PositionalIndexSegmentForTest) o;
        return Objects.equals(invertedLists, that.invertedLists) &&
                Objects.equals(documents, that.documents) &&
                Objects.equals(positions, that.positions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invertedLists, documents, positions);
    }

    @Override
    public String toString() {
        return "PositionalIndexSegmentForTest{" +
                "invertedLists=" + invertedLists +
                ", documents=" + documents +
                ", positions=" + positions +
                '}';
    }
}
