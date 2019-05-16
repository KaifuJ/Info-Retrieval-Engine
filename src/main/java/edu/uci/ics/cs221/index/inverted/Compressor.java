package edu.uci.ics.cs221.index.inverted;

import java.util.List;

public interface Compressor {

    /**
     * Encodes a list of integers to a byte array.
     */
    byte[] encode(List<Integer> integers);

    /**
     * Decodes part of a byte array to a list of integers.
     *
     * @param bytes bytes to decode
     * @param startOffset starting position to decode
     * @param length number of bytes to decode from start position
     */
    List<Integer> decode(byte[] bytes, int startOffset, int length);

    /**
     * Decodes a whole byte array to a list of integers.
     */
    default List<Integer> decode(byte[] bytes) {
        return decode(bytes, 0, bytes.length);
    }

}
