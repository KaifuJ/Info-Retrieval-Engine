package edu.uci.ics.cs221.index.inverted;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.NaiveAnalyzer;
import edu.uci.ics.cs221.storage.Document;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Stress test cases for inverted index by team 1.
 *
 * @author Zixu Wang
 */
public class Team1StressTest {
    private InvertedIndexManager iim;
    private Analyzer analyzer = new NaiveAnalyzer();
    private String indexDir = "index/Team1StressTest";

    private Document[] largeDocs = new Document[] {
            generateDoc(5),
            generateDoc(5)
    };

    private Document[] manyDocs = new Document[] {
            generateDoc(1),
            generateDoc(1),
            generateDoc(1),
            generateDoc(1),
            generateDoc(1),
            generateDoc(1),
            generateDoc(1),
            generateDoc(1),
            generateDoc(1),
            generateDoc(1)
    };

    @Before
    public void initialize() {
        iim = InvertedIndexManager.createOrOpen(indexDir, analyzer);
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        PageFileChannel.resetCounters();
    }

    @After
    public void clean() throws IOException {
        deleteDirectory(Paths.get(indexDir));
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
    }

    /**
     * Stress test with two large documents.
     * @see #largeDocs
     */
    @Test
    public void test1() {
        for (Document doc: largeDocs)
            iim.addDocument(doc);

        assertEquals(iim.getNumSegments(), 2);

        iim.mergeAllSegments();

        assertEquals(iim.getNumSegments(), 1);

        assertTrue(PageFileChannel.writeCounter >= 3);
        assertTrue(PageFileChannel.readCounter >= 2);

        InvertedIndexSegmentForTest segment = iim.getIndexSegment(0);
        Map<Integer, Document> docs = segment.getDocuments();

        assertEquals(docs.size(), largeDocs.length);

        Iterator<Document> itr = iim.searchQuery("university");

        int count = 0;
        while(itr.hasNext()){
            itr.next();
            count++;
        }

        assertEquals(count, largeDocs.length);
    }

    /**
     * Stress test with 10 smaller documents.
     * @see #manyDocs
     */
    @Test
    public void test2() {
        for (Document doc: manyDocs)
            iim.addDocument(doc);

        assertTrue(PageFileChannel.writeCounter >= 11);
        assertTrue(PageFileChannel.readCounter >= 2);

        Iterator<Document> itr =
                iim.searchAndQuery(Arrays.asList("GibberishThatNotInDoc", "university"));
        assertTrue(!itr.hasNext());
    }

    /**
     * Delete a directory recursively.
     *
     * @param path path to the directory to be deleted.
     * @throws IOException
     */
    private void deleteDirectory(Path path) throws IOException {
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteDirectory(entry);
                }
            }
        }
        Files.delete(path);
    }

    /**
     * Generate large documents from the dictionary.
     *
     * @param n the number of repeats.
     * @return a new {@link Document} containing {@code n} repeats of all words in the dictionary.
     */
    private Document generateDoc(int n) {
        try {
            URL dictResource = Team1StressTest.class.getClassLoader()
                    .getResource("cs221_frequency_dictionary_en.txt");
            String text = Files.readAllLines(Paths.get(dictResource.toURI())).stream()
                    .map(line -> line.startsWith("\uFEFF") ? line.substring(1) : line)
                    .map(line -> line.split(" ")[0])
                    .collect(Collectors.joining(" "));
            return new Document(String.join(" ", Collections.nCopies(n, text)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
