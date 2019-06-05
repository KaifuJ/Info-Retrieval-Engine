package edu.uci.ics.cs221.index.ranking;

import edu.uci.ics.cs221.analysis.ComposableAnalyzer;
import edu.uci.ics.cs221.analysis.PorterStemmer;
import edu.uci.ics.cs221.analysis.PunctuationTokenizer;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.index.inverted.InvertedIndexManager;
import edu.uci.ics.cs221.storage.Document;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class Team3TfIdfTest {
    private  static InvertedIndexManager iim;
    private static String path = "./index/Team3TfIdfTest/";
    private  static Document[] documents;
    private final static int frequencyNum = 20;
    @BeforeClass
    public static void init(){
        iim = InvertedIndexManager.createOrOpenPositional(path,
                new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()),
                new DeltaVarLenCompressor());

        documents = new Document[] {
                new Document("Winter is coming."),
                new Document("You know nothing Jon Snow."),
                new Document("A lion doesn’t concern himself with the opinions of a sheep."),
                new Document("When you play the game of thrones, you win or you die."),
                new Document("A Lannister always pays his debts."),
                new Document("All men must die, but we are not men."),
                new Document("There is only one god and his name is Death, and there is only one thing we say to Death: Not today.")
        };
    }

    @Test
    public void testFrequencyGet(){
        for(int i=0;i<documents.length;i++){ //for each document in the document array
            for(int j = 0; j<frequencyNum;j++){  //Set a frequencyNum
                iim.addDocument(documents[i]);   //add documents which have same string into a segment
            }
            iim.flush();
        }
        //test if the number of frequency match the frequency number we previously set
        assertEquals(frequencyNum,iim.getDocumentFrequency(0,"winter"));
        assertEquals(frequencyNum,iim.getDocumentFrequency(1,"snow"));
        assertEquals(frequencyNum,iim.getDocumentFrequency(2,"lion"));
        assertEquals(frequencyNum,iim.getDocumentFrequency(3,"plai"));
        assertEquals(frequencyNum,iim.getDocumentFrequency(4,"pai"));
        assertEquals(frequencyNum,iim.getDocumentFrequency(5,"men"));
        assertEquals(frequencyNum,iim.getDocumentFrequency(6,"death"));

    }

    @Test
    public void testNoResult(){
        for(int i=0;i<documents.length;i++){
            iim.addDocument(documents[i]);
        }
        iim.flush();
        //test one non-exist word in our documents
        assertEquals(0,iim.getDocumentFrequency(0,"cs221"));
    }

    @After
    public void clean() {

        try {
            File folder = new File(path);
            String[] entries = folder.list();
            for(String s: entries) {
                File currentFile = new File(folder.getPath(),s);
                currentFile.delete();
            }

            if (folder.delete()) {
                System.out.println("Folder deleted successfully");
            } else {
                System.out.println("Failed to delete the folder");
            }
        } catch (Exception e) {
            System.out.println("Something went wrong when deleting file");
        }
    }
}
