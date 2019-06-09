package edu.uci.ics.cs221.storage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class MapdbDocStoreTest {

    String file = "MapdbDocStoreTest.db";
    DocumentStore documentStore;

    @Before
    public void setup() throws Exception {
    }

    @After
    public void cleanup() throws Exception {
        if (documentStore != null) {
            documentStore.close();
        }
        Files.deleteIfExists(Paths.get(file));
    }


    @Test
    public void testAddDocument() throws Exception {
        documentStore = MapdbDocStore.createOrOpen(file);
        documentStore.addDocument(1, new Document("test1"));
        documentStore.addDocument(2, new Document("test2"));
        documentStore.addDocument(3, new Document("test3"));
        documentStore.close();

        assertTrue(Files.size(Paths.get(file)) > 0);
    }


    @Test
    public void testGetDocument() throws Exception {
        documentStore = MapdbDocStore.createOrOpen(file);
        documentStore.addDocument(1, new Document("test1"));
        documentStore.addDocument(2, new Document("test2"));
        documentStore.close();

        documentStore = MapdbDocStore.createOrOpen(file);
        assertEquals(documentStore.getDocument(1), new Document("test1"));
        assertEquals(documentStore.getDocument(2), new Document("test2"));
        assertNull(documentStore.getDocument(3));
        documentStore.close();

    }

}
