package edu.uci.ics.cs221.search;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.NaiveAnalyzer;
import edu.uci.ics.cs221.storage.Document;
import edu.uci.ics.cs221.storage.DocumentStore;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static edu.uci.ics.cs221.storage.MapdbDocStore.createOrOpen;
import static org.junit.Assert.assertEquals;

public class FullScanSearcherTest {

    @Test
    public void testFullScanSearch() {
        DocumentStore documentStore = createOrOpen("./docs.db");

        documentStore.addDocument(0, new Document("UCI CS221 Information Retrieval"));
        documentStore.addDocument(1, new Document("Information Systems"));
        documentStore.addDocument(2, new Document("UCI ICS"));

        documentStore.close();

        documentStore = createOrOpen("./docs.db");

        String query = "uci";
        Analyzer analyzer = new NaiveAnalyzer();
        FullScanSearcher fullScanSearcher = new FullScanSearcher(documentStore, analyzer);
        List<Integer> searchResult = fullScanSearcher.search(query);
        fullScanSearcher.close();

        assertEquals(new HashSet<>(searchResult), new HashSet<>(Arrays.asList(0, 2)));

    }


}
