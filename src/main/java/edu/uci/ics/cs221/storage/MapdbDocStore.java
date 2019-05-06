package edu.uci.ics.cs221.storage;

import com.google.common.collect.Iterators;
import org.mapdb.*;

import java.util.Iterator;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.immutableEntry;

/**
 * MapdbDocStore implements the Document Store using a Library `MapDB` http://www.mapdb.org/
 *
 */
public class MapdbDocStore implements DocumentStore {

    private static final String mapName = "docStore";

    /**
     * Opens a document store at the given file location if it already exists.
     * Otherwise, creates a new document store at the give location and opens it.
     *
     * @param docStoreFile file location of the document store
     * @return handle to an opened Document Store
     */
    public static DocumentStore createOrOpen(String docStoreFile) {
        return new MapdbDocStore(docStoreFile, false);
    }

    /**
     * Opens (or creates if not exists) a document store at the given file location in readOnly mode.
     * Opening multiple DocumentStore handles in ReadOnly mode won't conflict with each other.
     *
     * @param docStoreFile file location of the document store
     * @return handle to an opened Document Store
     */
    public static DocumentStore createOrOpenReadOnly(String docStoreFile) {
        return new MapdbDocStore(docStoreFile, true);
    }

    /**
     * Creates a Document Store and bulk load all documents in the iterator.
     * The documents in the iterator *MUST* be *sorted* by key.
     * For example, you could use an iterator from any `SortedMap`, such as `TreeMap` in java.
     *
     * It is *highly recommended* to use this constructor in order to pass stress tests,
     * bulk loading is much faster than calling `addDocument` many times.
     *
     */
    public static DocumentStore createWithBulkLoad(String docStoreFile, Iterator<Map.Entry<Integer, Document>> documents) {
        return new MapdbDocStore(docStoreFile, documents);
    }


    private DB db;
    private BTreeMap<Integer, String> map;

    private MapdbDocStore(String docStoreFile, boolean readOnly) {
        if (readOnly) {
            this.db = DBMaker.fileDB(docStoreFile).readOnly().make();
        } else {
            this.db = DBMaker.fileDB(docStoreFile).make();
        }
        this.map = this.db.treeMap(mapName)
                .keySerializer(Serializer.INTEGER)
                .valueSerializer(Serializer.STRING)
                .counterEnable()
                .createOrOpen();
    }

    private MapdbDocStore(String docStoreFile, Iterator<Map.Entry<Integer, Document>> documents) {
        this.db = DBMaker.fileDB(docStoreFile).make();

        DB.TreeMapSink<Integer,String> sink = this.db.treeMap(mapName)
                .keySerializer(Serializer.INTEGER)
                .valueSerializer(Serializer.STRING)
                .counterEnable()
                .createFromSink();

        documents.forEachRemaining(e -> sink.put(e.getKey(), e.getValue().getText()));
        this.map = sink.create();
    }


    @Override
    public void close() {
        if (this.map != null) {
            this.map.close();
        }
        if (this.db != null) {
            this.db.close();
        }
    }

    @Override
    public void addDocument(int docID, Document document) {
        checkNotNull(document);
        this.map.put(docID, document.getText());
    }

    @Override
    public Document getDocument(int docID) {
        String docText = this.map.get(docID);
        if (docText == null) {
            return null;
        }
        return new Document(docText);
    }

    @Override
    public Iterator<Map.Entry<Integer, Document>> iterator() {
        return Iterators.transform(this.map.getEntries().iterator(),
                entry -> immutableEntry(entry.getKey(), new Document(entry.getValue())));
    }



    @Override
    public long size() {
        return this.map.sizeLong();
    }

}