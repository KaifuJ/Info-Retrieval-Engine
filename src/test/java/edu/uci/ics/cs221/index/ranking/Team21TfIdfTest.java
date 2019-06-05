package edu.uci.ics.cs221.index.ranking;

import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.NaiveCompressor;
import edu.uci.ics.cs221.index.inverted.Pair;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class Team21TfIdfTest {
    String indexFolder = "./index/Team21TfIdfTest/";
    String[]  str = new String[]{
            "dog cat bird whale",
            "fish bird whale penguin",
            "snake bird cat dog",
            "anteater fish snake dog",
    };

    @After
    public void deleteWrittenFiles() throws IOException {
        File file = new File(indexFolder);
        if (file.exists() && file.isDirectory()) {
            String[] fileList = file.list();
            File temp = null;
            for (int i = 0; i < fileList.length; i++) {
                if (indexFolder.endsWith(File.separator)) {
                    temp = new File(indexFolder + fileList[i]);
                } else {
                    temp = new File(indexFolder + File.separator + fileList[i]);
                }
                if (temp.isFile()) {
                    temp.delete();
                }
            }
        }
        Files.deleteIfExists(Paths.get(indexFolder));
    }

    @Test
    public void test1() {
        /*
        test searching documents with comparing TF-IDF scores.
         */
        ComposableAnalyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), token -> token);
        InvertedIndexManager manager = InvertedIndexManager.createOrOpenPositional(indexFolder, analyzer, new NaiveCompressor());

        manager.addDocument(new Document(str[0]));
        manager.addDocument(new Document(str[1]));
        manager.flush();
        manager.addDocument(new Document(str[2]));
        manager.addDocument(new Document(str[3]));
        manager.flush();

        List<String> searchPhrase = new ArrayList<>(Arrays.asList("dog","cat","bird"));
        Iterator<Pair<Document, Double>> searchResults = manager.searchTfIdf(searchPhrase,2);

        int count = 0;
        List<String> expected = new ArrayList<>(Arrays.asList(str[0],str[2]));

        while(searchResults.hasNext()){
            assertFalse(count>=expected.size());
            Pair<Document, Double> result = searchResults.next();
            assertEquals(result.getLeft().getText(),expected.get(count));
            count++;
        }
        assertEquals(count, 2);
    }
}
