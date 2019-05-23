package edu.uci.ics.cs221.index.inverted;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * NaiveCompressor is a compressor with no compression at all.
 * This compressor can be used to compare the compression ratio with/without compression in test cases.
 */
public class NaiveCompressor implements Compressor {

    @Override
    public byte[] encode(List<Integer> integers) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(integers.size() * 4);
        integers.forEach(i -> byteBuffer.putInt(i));
        return byteBuffer.array();
    }

    @Override
    public List<Integer> decode(byte[] bytes, int startOffset, int length) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, startOffset, length);
        List<Integer> integers = new ArrayList<>();
        while (byteBuffer.hasRemaining()) {
            integers.add(byteBuffer.getInt());
        }
        return integers;
    }

}
