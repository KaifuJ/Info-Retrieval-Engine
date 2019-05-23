package edu.uci.ics.cs221.index.positional;

import edu.uci.ics.cs221.analysis.Analyzer;
import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.index.inverted.NaiveCompressor;
import edu.uci.ics.cs221.index.inverted.PageFileChannel;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class Team5IndexCompressionTest {
  private DeltaVarLenCompressor compressor = new DeltaVarLenCompressor();
  private NaiveCompressor naivecompressor = new NaiveCompressor();
  private String path = "./index/Team5IndexCompressionTest";
  private String path1 = "./index/Team5IndexCompressionTest/naive_compress";
  private String path2 = "./index/Team5IndexCompressionTest/compress";
  private Analyzer analyzer =
          new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
  private InvertedIndexManager positional_list_naive_compressor;
  private InvertedIndexManager positional_list_compressor;

  @Before
  public void setup() {
    File directory1 = new File(path1);
    if (!directory1.exists()) {
      directory1.mkdirs();
    }
    File directory2 = new File(path2);
    if (!directory2.exists()) {
      directory2.mkdirs();
    }
    positional_list_naive_compressor =
            InvertedIndexManager.createOrOpenPositional(path1, analyzer, naivecompressor);
    positional_list_compressor =
            InvertedIndexManager.createOrOpenPositional(path2, analyzer, compressor);
    PageFileChannel.resetCounters();
  }

  // test simple documents with same text, each key word show only one time each document
  // mainly test inverted list since inverted list is long but positional list is short
  @Test
  public void Test1() {
    Assert.assertEquals(0, PageFileChannel.readCounter);
    Assert.assertEquals(0, PageFileChannel.writeCounter);
    for (int i = 0; i < 10000; i++)
      positional_list_naive_compressor.addDocument(new Document("cat Dot"));
    positional_list_naive_compressor.flush();
    for (int i = 0; i < positional_list_naive_compressor.getNumSegments(); i++) {
      positional_list_naive_compressor.getIndexSegmentPositional(i);
    }
    int naive_wc = PageFileChannel.writeCounter;
    int naive_rc = PageFileChannel.readCounter;
    PageFileChannel.resetCounters();

    for (int i = 0; i < 10000; i++) positional_list_compressor.addDocument(new Document("cat Dot"));
    positional_list_compressor.flush();
    for (int i = 0; i < positional_list_compressor.getNumSegments(); i++) {
      positional_list_compressor.getIndexSegmentPositional(i);
    }
    int compress_wc = PageFileChannel.writeCounter;
    int compress_rc = PageFileChannel.readCounter;

    System.out.println();
    Assert.assertTrue(naive_wc > 1.5 * compress_wc);
    System.out.println("\033[0;32m");
    System.out.println("Naive compress write: " + naive_wc + " pages");
    System.out.println("Naive compress read: " + naive_rc + " pages");

    System.out.println("Your compress write: " + compress_wc + " pages");
    System.out.println("Your compress read: " + compress_rc + " pages");
    System.out.println("\033[0m");
  }

  // test docs with different text and each key word show multiple times in multiple document
  // mainly test inverted list since inverted list is long but positional list is short
  @Test
  public void Test2() {
    Assert.assertEquals(0, PageFileChannel.readCounter);
    Assert.assertEquals(0, PageFileChannel.writeCounter);

    for (int i = 0; i < 3000; i++) {
      positional_list_naive_compressor.addDocument(
              new Document("cat Dot cat Dog I can not tell the difference between cat and Dog"));
      positional_list_naive_compressor.addDocument(
              new Document("cat and dog have a lot of difference"));
      positional_list_naive_compressor.addDocument(
              new Document("Dog can be very different from cat"));
    }
    positional_list_naive_compressor.flush();
    for (int i = 0; i < positional_list_naive_compressor.getNumSegments(); i++) {
      positional_list_naive_compressor.getIndexSegmentPositional(i);
    }
    int naive_wc = PageFileChannel.writeCounter;
    int naive_rc = PageFileChannel.readCounter;
    PageFileChannel.resetCounters();

    for (int i = 0; i < 3000; i++) {
      positional_list_compressor.addDocument(
              new Document("cat Dot cat Dog I can not tell the difference between cat and Dog"));
      positional_list_compressor.addDocument(new Document("cat and dog have a lot of difference"));
      positional_list_compressor.addDocument(new Document("Dog can be very different from cat"));
    }
    positional_list_compressor.flush();
    for (int i = 0; i < positional_list_compressor.getNumSegments(); i++) {
      positional_list_compressor.getIndexSegmentPositional(i);
    }
    int compress_wc = PageFileChannel.writeCounter;
    int compress_rc = PageFileChannel.readCounter;

    Assert.assertTrue(naive_wc > 1.5 * compress_wc);

    System.out.println("\033[0;32m");
    System.out.println("Naive compress write: " + naive_wc + " pages");
    System.out.println("Naive compress read: " + naive_rc + " pages");

    System.out.println("Your compress write: " + compress_wc + " pages");
    System.out.println("Your compress read: " + compress_rc + " pages");
    System.out.println("\033[0m");
  }

  // test docs with different text and each key word show multiple times only in a document
  // mainly test positional  list since inverted  since  positional list is long
  @Test
  public void Test3() {

    Assert.assertEquals(0, PageFileChannel.readCounter);
    Assert.assertEquals(0, PageFileChannel.writeCounter);
    for (int i = 0; i < 3000; i++) {
      positional_list_naive_compressor.addDocument(
              new Document("cat" + " cat" + " cat" + " and dog" + " dog" + " dog"));
      positional_list_naive_compressor.addDocument(
              new Document("pepsi" + " pepsi" + " pepsi" + " or coke" + " coke" + " coke"));
      positional_list_naive_compressor.addDocument(
              new Document(
                      "microsoft"
                              + " microsoft"
                              + i
                              + " microsoft"
                              + " vs apple"
                              + " apple"
                              + " apple"
                              + i));
    }
    positional_list_naive_compressor.flush();
    for (int i = 0; i < positional_list_naive_compressor.getNumSegments(); i++) {
      positional_list_naive_compressor.getIndexSegmentPositional(i);
    }
    int naive_wc = PageFileChannel.writeCounter;
    int naive_rc = PageFileChannel.readCounter;
    PageFileChannel.resetCounters();

    for (int i = 0; i < 3000; i++) {
      positional_list_compressor.addDocument(
              new Document("cat" + " cat" + " cat" + " and dog" + " dog" + " dog"));
      positional_list_compressor.addDocument(
              new Document("pepsi" + " pepsi" + " pepsi" + " or coke" + " coke" + " coke"));
      positional_list_compressor.addDocument(
              new Document(
                      "microsoft"
                              + " microsoft"
                              + i
                              + " microsoft"
                              + " vs apple"
                              + " apple"
                              + " apple"
                              + i));
    }
    positional_list_compressor.flush();
    for (int i = 0; i < positional_list_compressor.getNumSegments(); i++) {
      positional_list_compressor.getIndexSegmentPositional(i);
    }
    int compress_wc = PageFileChannel.writeCounter;
    int compress_rc = PageFileChannel.readCounter;
    Assert.assertTrue(
            "naive write counter > 1.5 delta compress write count  \n Actual  naive write: "
                    + naive_wc
                    + " delta write count: "
                    + compress_wc,
            naive_wc > 1.5 * compress_wc);

    System.out.println("\033[0;32m");
    System.out.println("Naive compress write: " + naive_wc + " pages");
    System.out.println("Naive compress read: " + naive_rc + " pages");

    System.out.println("Your compress write: " + compress_wc + " pages");
    System.out.println("Your compress read: " + compress_rc + " pages");
    System.out.println("\033[0m");
  }

  // test add really long document, testing the positional list works or not
  // mainly test positional  list since inverted  since  positional list is long
  @Test
  public void Test4() {
    Assert.assertEquals(0, PageFileChannel.readCounter);
    Assert.assertEquals(0, PageFileChannel.writeCounter);
    String doc1 = "cat Dot cat Dog I can not tell the difference between cat and Dog";
    String doc2 = "cat and dog have a lot of difference";
    String doc3 = "Dog can be very different from cat";
    for (int i = 0; i < 1000; i++) {
      doc1 = doc1 + " cat Dot cat Dog I can not tell the difference between cat and Dog";
      doc2 = doc2 + " cat and dog have a lot of difference";
      doc3 = doc3 + " Dog can be very different from cat";
    }

    Document document1 = new Document(doc1);
    Document document2 = new Document(doc2);
    Document document3 = new Document(doc3);

    for (int i = 0; i < 30; i++) {
      positional_list_naive_compressor.addDocument(document1);
      positional_list_naive_compressor.addDocument(document2);
      positional_list_naive_compressor.addDocument(document3);
    }
    positional_list_naive_compressor.flush();
    for (int i = 0; i < positional_list_naive_compressor.getNumSegments(); i++) {
      positional_list_naive_compressor.getIndexSegmentPositional(i);
    }
    int naive_wc = PageFileChannel.writeCounter;
    int naive_rc = PageFileChannel.readCounter;
    PageFileChannel.resetCounters();

    for (int i = 0; i < 30; i++) {
      positional_list_compressor.addDocument(document1);
      positional_list_compressor.addDocument(document2);
      positional_list_compressor.addDocument(document3);
    }
    positional_list_compressor.flush();
    for (int i = 0; i < positional_list_compressor.getNumSegments(); i++) {
      positional_list_compressor.getIndexSegmentPositional(i);
    }
    int compress_wc = PageFileChannel.writeCounter;
    int compress_rc = PageFileChannel.readCounter;

    Assert.assertTrue(naive_wc > 1.5 * compress_wc);
    System.out.println("\033[0;32m");
    System.out.println("Naive compress write: " + naive_wc + " pages");
    System.out.println("Naive compress read: " + naive_rc + " pages");

    System.out.println("Your compress write: " + compress_wc + " pages");
    System.out.println("Your compress read: " + compress_rc + " pages");
    System.out.println("\033[0m");
  }

  @After
  public void cleanup() throws Exception {
    PageFileChannel.resetCounters();
    Path rootPath = Paths.get("./index/Team5IndexCompressionTest");
    Files.walk(rootPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    Files.deleteIfExists(rootPath);
  }
}