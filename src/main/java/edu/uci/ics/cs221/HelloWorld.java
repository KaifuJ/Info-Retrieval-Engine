package edu.uci.ics.cs221;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.NaiveAnalyzer;
import edu.uci.ics.cs221.search.FullScanSearcher;
import edu.uci.ics.cs221.storage.Document;
import edu.uci.ics.cs221.storage.DocumentStore;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static edu.uci.ics.cs221.storage.MapdbDocStore.createOrOpen;

/**
 * This is a Hello World program of our CS221 Peterman Search Engine.
 * It shows how to use the skeleton API of the search engine:
 *  1. use the provided DocumentStore to add and save documents
 *  2. use the provided FullScanSearcher and NaiveAnalyzer to do a search
 *
 * Over this quarter, you will be implementing various analyzers, indexes, query types, and ranking
 *  to make the search engine more efficient, powerful, and user-friendly :)
 */
public class HelloWorld {

    public static void main(String[] args) throws Exception {
        String docStorePath = "./docs.db";
        Files.deleteIfExists(Paths.get(docStorePath));

        // create the document store, add a few documents, and close() to flush them to disk
        DocumentStore documentStore = createOrOpen(docStorePath);

        documentStore.addDocument(0, new Document("UCI CS221 Information Retrieval"));
        documentStore.addDocument(1, new Document("Donald Bren School of Information and Computer Sciences"));
        documentStore.addDocument(2, new Document("UCI School of ICS "));

        documentStore.close();

        // open the existing document store again. use FullScanSearcher to search a query
        documentStore = createOrOpen(docStorePath);

        String query = "information";

        Analyzer analyzer = new NaiveAnalyzer();
        FullScanSearcher fullScanSearcher = new FullScanSearcher(documentStore, analyzer);
        List<Integer> searchResult = fullScanSearcher.search(query);

        System.out.println("query: " + query);
        System.out.println("search results: ");
        for (int docID : searchResult) {
            System.out.println(docID + ": " + documentStore.getDocument(docID));
        }

        fullScanSearcher.close();

    }

}
