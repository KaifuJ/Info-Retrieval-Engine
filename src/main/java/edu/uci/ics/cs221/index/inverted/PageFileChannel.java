package edu.uci.ics.cs221.index.inverted;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Page File Channel provides low level page-oriented read/write operations of a file.
 * Each read/write operation is based on a minimal unit of a page of bytes.
 *
 * Your InvertedIndex implementation MUST use this PageFileChannel class to do read / write operations for *ALL* files your index uses.
 *
 * As an example, if you want to create and write to a file called "segment0":
 *
 * ```
 * Path filePath = Paths.get("segment0");
 * PageFileChannel pageFileChannel = PageFileChannel.createOrOpen(filePath);
 *
 * ByteBuffer byteBuffer = ByteBuffer.allocate(PAGE_SIZE);
 * // fill in the byteBuffer with your data
 * pageFileChannel.appendPage(byteBuffer);
 * pageFileChannel.close();
 * ```
 *
 */
public class PageFileChannel implements AutoCloseable {

    /**
     * Default page size in number of bytes.
     * In test cases, this number could be changed.
     */
    public static int PAGE_SIZE = 4096;

    /**
     * Read and Write counter in number of pages.
     * These counter values will be used to monitor your implementation's disk IO performance.
     * In test cases, read/write counter values will be also checked whether they are in a reasonable range.
     */
    public static int readCounter = 0;
    public static int writeCounter = 0;

    private FileChannel fileChannel;

    private PageFileChannel(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }



    /**
     * Creates (if not exists) or opens (if exists) a file channel of the given file path.
     *
     * @param path path to the file, must not be a directory
     * @return an opened PageFileChannel of the file
     */
    public static PageFileChannel createOrOpen(Path path) {
        try {
            if (! Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
            Verify.verify(! Files.isDirectory(path));
            return new PageFileChannel(FileChannel.open(path, READ, WRITE));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }




    /**
     * Reads a page from file into a byteBuffer (a sequence of bytes).
     *
     * @param pageNum, page number (starts from 0)
     * @return a ByteBuffer including the byte array of the
     */
    public ByteBuffer readPage(int pageNum) {
        try {
            readCounter++;
            ByteBuffer buffer = ByteBuffer.allocate(PAGE_SIZE);
            this.fileChannel.read(buffer, pageNum * PAGE_SIZE);
            buffer.rewind();
            return buffer;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Read all pages in this file into a ByteBuffer.
     *
     * @return byteBuffer containing all bytes of the file, with buffer capacity numPages * PAGE_SIZE.
     */
    public ByteBuffer readAllPages() {
        int numPages = this.getNumPages();
        ByteBuffer buffer = ByteBuffer.allocate(numPages * PAGE_SIZE);
        for (int i = 0; i < numPages; i++) {
            buffer.put(readPage(i).array(), i * PAGE_SIZE, PAGE_SIZE);
        }
        return buffer;
    }




    /**
     * Writes a page of bytes into the given page.
     *
     * @param pageNum, the page should already exist.
     * @param byteBuffer byteBuffer with capacity = PAGE_SIZE
     */
    public void writePage(long pageNum, ByteBuffer byteBuffer) {
        try {
            Preconditions.checkArgument(byteBuffer.capacity() == PAGE_SIZE);
            byteBuffer.rewind();
            this.fileChannel.write(byteBuffer, pageNum * PAGE_SIZE);
            writeCounter++;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Appends a page of bytes to the end of the file.
     *
     * @param byteBuffer byteBuffer with capacity = PAGE_SIZE
     */
    public void appendPage(ByteBuffer byteBuffer) {
        this.writePage(getNumPages(), byteBuffer);
    }

    /**
     * Appends all bytes in the byte buffer to the end of the file.
     *
     * @param byteBuffer byteBuffer with arbitrary long size
     */
    public void appendAllBytes(ByteBuffer byteBuffer) {
        for (int i = 0; i < byteBuffer.capacity(); i += PAGE_SIZE) {
            int length = PAGE_SIZE;
            if (i + PAGE_SIZE > byteBuffer.capacity()) {
                length = byteBuffer.capacity() - i;
            }
            appendPage(ByteBuffer.allocate(PAGE_SIZE).put(byteBuffer.array(), i, length));
        }
    }




    /**
     * Gets the number of pages of the file.
     * @return number of pages of the file
     */
    public int getNumPages() {
        try {
            return (int) (this.fileChannel.size() / PAGE_SIZE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Closes the file.
     */
    @Override
    public void close() {
        try {
            this.fileChannel.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Resets read/write counters, for testing purposes.
     */
    public static void resetCounters() {
        readCounter = 0;
        writeCounter = 0;
    }

}


class Test{
    public static void main(String[] args){
        Path path = Paths.get("./segmentTest.txt");
        PageFileChannel pfc = PageFileChannel.createOrOpen(path);

        ByteBuffer bf = ByteBuffer.allocate(4096);

        byte[] wr = new byte[20];
        for(int i = 0; i < wr.length; i++){
            wr[i] = (byte) (i + 1);
        }

        bf.put(wr);

        pfc.appendAllBytes(bf);


        bf = pfc.readAllPages();
        bf.flip();

        for(int i = 0; i < bf.capacity(); i++){
            System.out.println(bf.get());
        }

    }
}





























