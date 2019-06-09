package edu.uci.ics.cs221.search;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.Pair;
import edu.uci.ics.cs221.storage.Document;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This test is for Project 4 Task 2 - PageRank.
 * This test case only tests computing Page Rank scores,
 * `writeIndex` is not called so you don't have to write the documents to index.
 *
 * Please DO NOT commit the ICS webpage files into your Git repository. When running the tests,
 * the TA will copy the files to the "webpages" folder in root directory of your project.
 *
 */
public class IcsSearchEnginePageRankTest {

    static Path webPagesPath = Paths.get("./webpages");
    static Path indexPath = Paths.get("./index/IcsSearchEngineTest");
    static Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());

    static InvertedIndexManager invertedIndexManager;
    static IcsSearchEngine icsSearchEngine;

    static BiMap<Integer, String> idUrlMap;

    @BeforeClass
    public static void setup() throws Exception {
        invertedIndexManager = InvertedIndexManager.createOrOpen(indexPath.toString(), analyzer);
        icsSearchEngine = IcsSearchEngine.createSearchEngine(webPagesPath, invertedIndexManager);

//        icsSearchEngine.computePageRank(100);

        idUrlMap = HashBiMap.create();
        Files.readAllLines(webPagesPath.resolve("url.tsv")).stream().map(line -> line.split("\\s")).forEach(line -> {
            idUrlMap.put(Integer.parseInt(line[0].trim()), line[1].trim());
        });
    }

    @AfterClass
    public static void cleanup() throws Exception {
        deleteDirectory(indexPath.toFile());
    }

    static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    @Test
    public void testPageRankTop5() {
        List<Pair<Integer, Double>> pageRankScores = icsSearchEngine.getPageRankScores();

        // wics.ics.uci.edu should have the highest page rank score, check it must be in the top 5
        Assert.assertTrue(pageRankScores.stream().limit(5).map(p -> p.getLeft()).map(id -> idUrlMap.get(id))
                .anyMatch(url -> url.contains("wics.ics.uci.edu")));
    }

    @Test
    public void testPageRankTop20() {
        List<Pair<Integer, Double>> pageRankScores = icsSearchEngine.getPageRankScores();

        // www.ics.uci.edu - the ICS homepage should have the very high page rank score, check it must be in the top
        Assert.assertTrue(pageRankScores.stream().limit(20).map(p -> p.getLeft()).map(id -> idUrlMap.get(id))
                .anyMatch(url -> url.equalsIgnoreCase("www.ics.uci.edu")));
    }

    @Test
    public void testPageRankTop100() {
        List<Pair<Integer, Double>> pageRankScores = icsSearchEngine.getPageRankScores();

        // isg.ics.uci.edu - home page of the ISG group should appear in top 100
        Assert.assertTrue(pageRankScores.stream().limit(100).map(p -> p.getLeft()).map(id -> idUrlMap.get(id))
                .anyMatch(url -> url.equalsIgnoreCase("isg.ics.uci.edu")));
    }

    @Test
    public void testPageRankTop1000() {
        List<Pair<Integer, Double>> pageRankScores = icsSearchEngine.getPageRankScores();

        // in top 1000, there are many urls include grape.ics.uci.edu
        Assert.assertTrue(pageRankScores.stream().limit(1000).map(p -> p.getLeft()).map(id -> idUrlMap.get(id))
                .filter(url -> url.contains("grape.ics.uci.edu")).count() > 5);
    }

}
