package edu.uci.ics.cs221.index.ranking;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.NaiveAnalyzer;
import edu.uci.ics.cs221.index.inverted.Compressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.NaiveCompressor;
import edu.uci.ics.cs221.storage.Document;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * {@link InvertedIndexManager#getNumDocuments(int)} test cases for inverted index by team 1.
 *
 * Parameterized over the type of inverted indices:
 *      1. inverted index created by {@link InvertedIndexManager#createOrOpen(String, Analyzer)};
 *      2. positional index created by {@link InvertedIndexManager#createOrOpenPositional(String, Analyzer, Compressor)}
 *
 * @author Zixu Wang
 */
@RunWith(value = Parameterized.class)
public class Team1TfIdfTest {
    private InvertedIndexManager iim;

    private static String indexDir = "index/Team1TfIdfTest";
    private static Document dummy = new Document("dummy");  // dummy document for testing

    private int oldFlushThreshold;
    private int oldMergeThreshold;

    /**
     * Parameterized test constructor.
     *
     * @param createIndexManager supplier that creates a inverted index for testing
     */
    public Team1TfIdfTest(Supplier<InvertedIndexManager> createIndexManager) {
        iim = createIndexManager.get();

        oldFlushThreshold = InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD;
        oldMergeThreshold = InvertedIndexManager.DEFAULT_MERGE_THRESHOLD;
    }

    /**
     * Defines all parameters (index manager suppliers)
     *
     * Use supplier functions as parameters instead of concrete InvertedIndexManager
     * instances to allow re-instantiation for each test case.
     *
     * @return a collection of inverted index manager suppliers.
     */
    @Parameterized.Parameters
    public static Collection indexManagerSuppliers() {
        return Arrays.<Supplier<InvertedIndexManager>>asList(
                () -> InvertedIndexManager.createOrOpen(
                        indexDir, new NaiveAnalyzer()
                ),
                () -> InvertedIndexManager.createOrOpenPositional(
                        indexDir, new NaiveAnalyzer(), new NaiveCompressor()
                )
        );
    }

    @Before
    public void initialize() {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 10;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 4;
    }

    @After
    public void clean() {
        try {
            deleteDirectory(Paths.get(indexDir));
        } catch (IOException ignored) { }

        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = oldFlushThreshold;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = oldMergeThreshold;
    }

    /**
     * Test whether the numbers of documents in flushed segments are reported correctly
     */
    @Test
    public void flushedTest() {
        for (int i = 0; i < InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD * 2; i++) {
            iim.addDocument(dummy);
        }

        assertEquals(
                InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD,
                iim.getNumDocuments(0)
        );

        assertEquals(
                InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD,
                iim.getNumDocuments(1)
        );
    }

    /**
     * Test whether the numbers of documents in merged segments are reported correctly
     */
    @Test
    public void mergedTest() {
        for (
                int i = 0;
                i < InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD
                        * InvertedIndexManager.DEFAULT_MERGE_THRESHOLD;
                i++
        ) {
            iim.addDocument(dummy);
        }

        for (int i = 0; i < InvertedIndexManager.DEFAULT_MERGE_THRESHOLD / 2; i++) {
            assertEquals(
                    InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD * 2,
                    iim.getNumDocuments(i)
            );
        }
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
}
