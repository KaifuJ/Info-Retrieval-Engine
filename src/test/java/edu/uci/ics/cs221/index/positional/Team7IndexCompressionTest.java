package edu.uci.ics.cs221.index.positional;

import edu.uci.ics.cs221.analysis.*;
import edu.uci.ics.cs221.storage.Document;
import edu.uci.ics.cs221.index.inverted.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static junit.framework.TestCase.assertTrue;


public class Team7IndexCompressionTest {
    private InvertedIndexManager manager_no_compress;
    private InvertedIndexManager manager_compress;
    private List<String> dictLines;
    private int dict_size;

    String PATH = "./index/Team7IndexCompressionTest/";

    @Before
    public void before() {
        try {
            URL dictResource = WordBreakTokenizer.class.getClassLoader().getResource("cs221_frequency_dictionary_en.txt");
            dictLines = Files.readAllLines(Paths.get(dictResource.toURI()));
            dict_size = dictLines.size()-1;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 50;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 4;

        Analyzer analyzer = new NaiveAnalyzer();
        Compressor compressor = new NaiveCompressor();
        Compressor deltaVarLenCompressor = new DeltaVarLenCompressor();
        manager_no_compress = InvertedIndexManager.createOrOpenPositional(PATH+"non_compress", analyzer,compressor);
        manager_compress = InvertedIndexManager.createOrOpenPositional(PATH+"compress", analyzer,deltaVarLenCompressor);

        PageFileChannel.resetCounters();
    }

    @After
    public void after(){

        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 8;

        try{
            File index = new File(PATH+"non_compress");
            String[]entries = index.list();
            for(String s: entries){
                File currentFile = new File(index.getPath(),s);
                currentFile.delete();
            }
            index.delete();

            index = new File(PATH+"compress");
            String[]entries1 = index.list();
            for(String s: entries1){
                File currentFile = new File(index.getPath(),s);
                currentFile.delete();
            }
            index.delete();

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void addFewDocument(){
        for(int i = 0; i < 500; i++){
            Document doc = new Document(getRandString(20));
            manager_no_compress.addDocument(doc);
        }

        int count_no_compress = PageFileChannel.readCounter+PageFileChannel.writeCounter;
        PageFileChannel.resetCounters();

        for(int i = 0; i < 500; i++){
            Document doc = new Document(getRandString(20));
            manager_compress.addDocument(doc);
        }
        int count_compress = PageFileChannel.readCounter+PageFileChannel.writeCounter;

        assertTrue(count_no_compress>1.4*count_compress);
    }


    @Test
    public void addLargeDocument(){
        for(int i = 0; i < 5000; i++){
            Document doc = new Document(getRandString(20));
            manager_no_compress.addDocument(doc);
        }

        int count_no_compress = PageFileChannel.readCounter+PageFileChannel.writeCounter;
        PageFileChannel.resetCounters();

        for(int i = 0; i < 5000; i++){
            Document doc = new Document(getRandString(20));
            manager_compress.addDocument(doc);
        }
        int count_compress = PageFileChannel.readCounter+PageFileChannel.writeCounter;

        assertTrue(count_no_compress>1.4*count_compress);
    }


    public String getRandString(int size) {
        StringBuilder builder = new StringBuilder();
        while (size != 0) {
            builder.append(this.dictLines.get((int)(Math.random()*dict_size)+1).split(" ")[0]+" ");
            size--;
        }
        return builder.toString();
    }
}
