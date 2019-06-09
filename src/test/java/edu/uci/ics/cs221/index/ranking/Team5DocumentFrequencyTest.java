package edu.uci.ics.cs221.index.ranking;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.PageFileChannel;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Team5DocumentFrequencyTest {
  private String path = "./index/Team5DocumentFrequencyTest";
  private Analyzer analyzer =
      new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
  private InvertedIndexManager invertedList;

  @Before
  public void setUp() throws Exception {
    File directory = new File(path);
    if (!directory.exists()) {
      directory.mkdirs();
    }

    invertedList = InvertedIndexManager.createOrOpen(path, analyzer);
    invertedList.addDocument(new Document("cat dog toy"));
    invertedList.addDocument(new Document("cat Dot"));
    invertedList.addDocument(new Document("cat dot toy"));
    invertedList.flush();
    invertedList.addDocument(new Document("cat toy Dog"));
    invertedList.addDocument(new Document("toy dog cat"));
    invertedList.flush();
    invertedList.addDocument(new Document("cat Dog")); // docs cannot be null
    invertedList.flush();
    invertedList.addDocument(new Document("cat Dog")); // docs cannot be null
    invertedList.flush();
  }

  // Test document frequency of different keywords in different segments.
  @Test
  public void Test1() throws Exception {
    String words = "cat dog Toy Dot";

    List<String> new_words = analyzer.analyze(words);
    int result = invertedList.getDocumentFrequency(0, new_words.get(0));
    assertEquals(3, result);
    result = invertedList.getDocumentFrequency(1, new_words.get(0));
    assertEquals(2, result);
    result = invertedList.getDocumentFrequency(2, new_words.get(0));
    assertEquals(1, result);

    result = invertedList.getDocumentFrequency(0, new_words.get(1));
    assertEquals(1, result);
    result = invertedList.getDocumentFrequency(1, new_words.get(1));
    assertEquals(2, result);
    result = invertedList.getDocumentFrequency(2, new_words.get(1));
    assertEquals(1, result);

    result = invertedList.getDocumentFrequency(0, new_words.get(2));
    assertEquals(2, result);
    result = invertedList.getDocumentFrequency(1, new_words.get(2));
    assertEquals(2, result);
    result = invertedList.getDocumentFrequency(2, new_words.get(2));
    assertEquals(0, result);

    result = invertedList.getDocumentFrequency(0, new_words.get(3));
    assertEquals(2, result);
    result = invertedList.getDocumentFrequency(1, new_words.get(3));
    assertEquals(0, result);
    result = invertedList.getDocumentFrequency(2, new_words.get(3));
    assertEquals(0, result);
  }
  // Test the case that the key word does not match any file.
  @Test
  public void Test2() throws Exception {
    String words = "sdasjdlslsah";
    List<String> new_words = analyzer.analyze(words);
    int n = invertedList.getNumSegments();
    for (int i = 0; i < new_words.size(); i++) {
      for (int j = 0; j < n; j++) {
        int result = invertedList.getDocumentFrequency(j, new_words.get(i));
        assertEquals(0, result);
      }
    }
  }

  /**
   * Test merge function merge all segment chronologically
   *
   * <p>Seg0<br>
   * Document("cat dog toy")) <br>
   * Document("cat Dot")) <br>
   * Document("cat dot toy")) <br>
   * Document("cat toy Dog")) <br>
   * Document("toy dog cat")) <br>
   *
   * <p>Seg1<br>
   * Document("cat Dog"))<br>
   * Document("cat Dog"))
   */
  @Test
  public void testMerge() {
    invertedList.mergeAllSegments();
    String words = "cat dog Toy Dot";
    List<String> new_words = analyzer.analyze(words);
    int result = invertedList.getDocumentFrequency(0, new_words.get(0));
    assertEquals(5, result);
    result = invertedList.getDocumentFrequency(0, new_words.get(2));
    assertEquals(4, result);
    result = invertedList.getDocumentFrequency(1, new_words.get(2));
    assertEquals(0, result);
    result = invertedList.getDocumentFrequency(1, new_words.get(3));
    assertEquals(0, result);
    result = invertedList.getDocumentFrequency(1, new_words.get(0));
    assertEquals(2, result);
  }

  @After
  public void deleteTmp() throws Exception {

    PageFileChannel.resetCounters();
    File f = new File(path);
    File[] files = f.listFiles();
    for (File file : files) {
      file.delete();
    }
    f.delete();
  }
}
