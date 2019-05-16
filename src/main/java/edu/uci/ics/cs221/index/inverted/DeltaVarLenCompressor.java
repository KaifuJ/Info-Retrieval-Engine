package edu.uci.ics.cs221.index.inverted;

import java.util.List;

/**
 * Implement this compressor with Delta Encoding and Variable-Length Encoding.
 * See Project 3 description for details.
 */
public class DeltaVarLenCompressor implements Compressor {

    @Override
    public byte[] encode(List<Integer> integers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> decode(byte[] bytes, int start, int length) {
        throw new UnsupportedOperationException();
    }
}
