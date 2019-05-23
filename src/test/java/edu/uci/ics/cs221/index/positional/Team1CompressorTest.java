package edu.uci.ics.cs221.index.positional;

import edu.uci.ics.cs221.index.inverted.Compressor;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Test cases for DeltaVarLenCompressor correctness by team 1.
 *
 * @author Zixu Wang
 */
public class Team1CompressorTest {

    /**
     * Test whether the compressor is self-consistent.
     * i.e.: decode(encode(l)) == l and encode(decode(b)) == b
     */
    @Test
    public void selfConsistencyTest() {
        Compressor compressor = new DeltaVarLenCompressor();

        Function<List<Integer>, byte[]> encodeFunc = compressor::encode;
        Function<byte[], List<Integer>> decodeFunc = compressor::decode;

        Stream.of(
                Arrays.asList(0),
                Arrays.asList(Integer.MAX_VALUE),
                Arrays.asList(Integer.MAX_VALUE, Integer.MAX_VALUE),
                Arrays.asList(0, Integer.MAX_VALUE),
                Arrays.asList(1, 1, 1),
                Arrays.asList(1, 2, 3),
                Arrays.asList(3, 3, 7, 23, 23, 65, 294, 2190, 238923)
        ).forEach(l -> assertEquals(
                "Function composition (decode . encode) should be equivalent to id",
                Function.identity().apply(l),
                decodeFunc.compose(encodeFunc).apply(l)
        ));

        Stream.of(
                // [ 0 ]
                new byte[] { 0x00 },
                // [ Integer.MAX_VALUE ]
                new byte[] { (byte)0x87, (byte)0xff, (byte)0xff, (byte)0xff, 0x7f },
                // [ Integer.MAX_VALUE, Integer.MAX_VALUE ]
                new byte[] { (byte)0x87, (byte)0xff, (byte)0xff, (byte)0xff, 0x7f, 0x00 },
                // [ 0, Integer.MAX_VALUE ]
                new byte[] { 0x00, (byte)0x87, (byte)0xff, (byte)0xff, (byte)0xff, 0x7f },
                // [ 1, 1, 1 ]
                new byte[] { 0x01, 0x00, 0x00 },
                // [ 1, 2, 3 ]
                new byte[] { 0x01, 0x01, 0x01 },
                // [ 3, 3, 7, 23, 23, 65, 294, 2190, 238923 ]
                new byte[] {
                        0x03,
                        0x00,
                        0x04,
                        0x10,
                        0x00,
                        0x2a,
                        (byte)0x81, 0x65,
                        (byte)0x8e, 0x68,
                        (byte)0x8e, (byte)0xb9, 0x3d
                }
        ).forEach(b -> assertArrayEquals(
                "Function composition (encode . decode) should be equivalent to id",
                (byte[]) Function.identity().apply(b),
                encodeFunc.compose(decodeFunc).apply(b)
        ));
    }

    /**
     * Test whether the compressor encodes single-byte integers correctly.
     */
    @Test
    public void encodeSingleByteTest() {
        Compressor compressor = new DeltaVarLenCompressor();

        for (int i = 0; i <= 127; i++)
            assertArrayEquals(
                    new byte[] { (byte)i },
                    compressor.encode(Arrays.asList(i))
            );
    }

    /**
     * Test whether the compressor uses variable-length encoding.
     */
    @Test
    public void encodeMultipleBytesTest() {
        Compressor compressor = new DeltaVarLenCompressor();

        // Test encode 128
        assertArrayEquals(
                new byte[] { (byte)0b10000001, 0b00000000 },
                compressor.encode(Arrays.asList(128))
        );

        // Test encode 16384
        assertArrayEquals(
                new byte[] { (byte)0b10000001, (byte)0b10000000, 0b00000000 },
                compressor.encode(Arrays.asList(16384))
        );

        // Test encode Integer.MAX_VALUE
        assertArrayEquals(
                new byte[] {
                        (byte)0b10000111, (byte)0b11111111, (byte)0b11111111,
                        (byte)0b11111111, 0b01111111
                },
                compressor.encode(Arrays.asList(Integer.MAX_VALUE))
        );
    }

    /**
     * Test whether the compressor encodes gaps correctly.
     */
    @Test
    public void encodeGapTest() {
        Compressor compressor = new DeltaVarLenCompressor();

        // Test encode [ 1, 1, 1 ]
        assertArrayEquals(
                new byte[] { 1, 0, 0 },
                compressor.encode(Arrays.asList(1, 1, 1))
        );

        // Test encode [ 1, 2, 3 ]
        assertArrayEquals(
                new byte[] { 1, 1, 1 },
                compressor.encode(Arrays.asList(1, 2, 3))
        );

        // Test encode triangular sequence [ 0, 1, 3, 6, ..., 8128 ]
        List<Integer> integers = new ArrayList<>();
        byte[] expected = new byte[128];

        for (int i = 0; i <= 127; i++) {
            integers.add(i * (i + 1) / 2);
            expected[i] = (byte)i;
        }

        assertArrayEquals(expected, compressor.encode(integers));

        // Test encode [ 128, 2 * 128, 3 * 128 ]
        assertArrayEquals(
                new byte[] {
                        (byte)0b10000001, 0b00000000,
                        (byte)0b10000001, 0b00000000,
                        (byte)0b10000001, 0b00000000
                },
                compressor.encode(Arrays.asList(128, 2 * 128, 3 * 128))
        );
    }

    /**
     * Test whether the compressor decodes single-byte integers correctly.
     */
    @Test
    public void decodeSingleByteTest() {
        Compressor compressor = new DeltaVarLenCompressor();

        for (int i = 0; i <= 127; i++)
            assertEquals(
                    Arrays.asList(i),
                    compressor.decode(new byte[] { (byte)i })
            );
    }

    /**
     * Test whether the compressor decodes variable-length codes correctly.
     */
    @Test
    public void decodeMultipleBytesTest() {
        Compressor compressor = new DeltaVarLenCompressor();

        // Test decode 1000'0001'0000'0000 (128)
        assertEquals(
                Arrays.asList(128),
                compressor.decode(new byte[] { (byte)0b10000001, 0b00000000 })
        );

        // Test decode 1000'0001'1000'0000'0000'0000 (16384)
        assertEquals(
                Arrays.asList(16384),
                compressor.decode(new byte[] { (byte)0b10000001, (byte)0b10000000, 0b00000000 })
        );

        // Test decode 1000'0111'1111'1111'1111'1111'1111'1111'0111'1111 (Integer.MAX_VALUE)
        assertEquals(
                Arrays.asList(Integer.MAX_VALUE),
                compressor.decode(new byte[] {
                        (byte)0b10000111, (byte)0b11111111, (byte)0b11111111,
                        (byte)0b11111111, 0b01111111
                })
        );
    }

    /**
     * Test whether the compressor decodes gaps correctly.
     */
    @Test
    public void decodeGapTest() {
        Compressor compressor = new DeltaVarLenCompressor();

        // Test decode [ 0x1, 0x0, 0x0 ] ([1, 1, 1])
        assertEquals(
                Arrays.asList(1, 1, 1),
                compressor.decode(new byte[] { 1, 0, 0 })
        );

        // Test decode [ 0x1, 0x1, 0x1 ] ([1, 2, 3])
        assertEquals(
                Arrays.asList(1, 2, 3),
                compressor.decode(new byte[] { 1, 1, 1 })
        );

        // Test decode [ 0x0, 0x1, 0x2, ..., 0x7f ]
        // (triangular sequence [ 0, 1, 3, 6, ..., 8128 ])
        byte[] code = new byte[128];
        List<Integer> expected = new ArrayList<>();

        for (int i = 0; i <= 127; i++) {
            code[i] = (byte)i;
            expected.add(i * (i + 1) / 2);
        }

        assertEquals(expected, compressor.decode(code));

        // Test decode [ 0x81, 0x00, 0x81, 0x00, 0x81, 0x00 ]
        // ([ 128, 2 * 128, 3 * 128 ])
        assertEquals(
                Arrays.asList(128, 2 * 128, 3 * 128),
                compressor.decode(new byte[] {
                        (byte)0b10000001, 0b00000000,
                        (byte)0b10000001, 0b00000000,
                        (byte)0b10000001, 0b00000000
                })
        );
    }
}
