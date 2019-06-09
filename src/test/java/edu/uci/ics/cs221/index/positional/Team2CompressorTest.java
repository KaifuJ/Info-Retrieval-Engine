package edu.uci.ics.cs221.index.positional;

import edu.uci.ics.cs221.index.inverted.Compressor;
import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


public class Team2CompressorTest {

    private Compressor compressor;

    @Before
    public void init() {
        compressor = new DeltaVarLenCompressor();
        assertNotNull(compressor);
    }

    public void fullTest(List<Integer> ints, byte[] bytes) {
        assertArrayEquals(bytes, compressor.encode(ints)); // To test encode function
        assertEquals(ints, compressor.decode(bytes)); // To test decode function with offset 0 and full length of input
    }

    /**
     * In the following test,
     * we test on lists with single element.
     * We test general cases like 131, 127 and 3,
     * and special cases like 16384, 128 and 0.
     */
    @Test
    public void singleTest() {
        fullTest(Arrays.asList(16384), new byte[]{(byte)0b10000001, (byte)0b10000000, (byte)0b00000000});
        fullTest(Arrays.asList(16386), new byte[]{(byte)0b10000001, (byte)0b10000000, (byte)0b00000010});
        fullTest(Arrays.asList(16383), new byte[]{(byte)0b11111111, (byte)0b01111111});
        fullTest(Arrays.asList(131), new byte[]{(byte)0b10000001, (byte)0b00000011});
        fullTest(Arrays.asList(128), new byte[]{(byte)0b10000001, (byte)0b00000000});
        fullTest(Arrays.asList(127), new byte[]{(byte)0b01111111});
        fullTest(Arrays.asList(3), new byte[]{(byte)0b00000011});
        fullTest(Arrays.asList(0), new byte[]{(byte)0b00000000});
    }

    /**
     * In the following test,
     * we test on lists with different combinations of integers.
     */
    @Test
    public void generalFullTest() {
        fullTest(Arrays.asList(3, 5, 8, 13, 20),
                new byte[]{(byte)0b00000011, (byte)0b00000010, (byte)0b00000011, (byte)0b00000101, (byte)0b00000111});
        fullTest(Arrays.asList(128, 16512, 16515, 16520, 16527),
                new byte[]{(byte)0b10000001, (byte)0b00000000, (byte)0b10000001, (byte)0b10000000, (byte)0b00000000, (byte)0b00000011, (byte)0b00000101, (byte)0b00000111});
        fullTest(Arrays.asList(128, 131, 136, 143, 16527),
                new byte[]{(byte)0b10000001, (byte)0b00000000, (byte)0b00000011, (byte)0b00000101, (byte)0b00000111, (byte)0b10000001, (byte)0b10000000, (byte)0b00000000});
        fullTest(Arrays.asList(16384, 32770, 49153),
                new byte[]{(byte)0b10000001, (byte)0b10000000, (byte)0b00000000, (byte)0b10000001, (byte)0b10000000, (byte)0b00000010, (byte)0b11111111, (byte)0b01111111});
        fullTest(Arrays.asList(16386, 16513, 16516),
                new byte[]{(byte)0b10000001, (byte)0b10000000, (byte)0b00000010, (byte)0b01111111, (byte)0b00000011});
        fullTest(Arrays.asList(0, 0, 0, 0),
                new byte[]{(byte)0b00000000, (byte)0b00000000, (byte)0b00000000, (byte)0b00000000});
    }

    /**
     * In the following test,
     * we test if the offset feature in decode function works fine.
     * We try different offsets and lengths on the testing byte array.
     */
    @Test
    public void decodeOffsetTest() {
        byte[] test = new byte[]{(byte)0b10000001, (byte)0b10000000, (byte)0b00000010, (byte)0b01111111, (byte)0b00000011, (byte)0b10000001, (byte)0b00000011};

        List<Integer> result1 = compressor.decode(test, 0, 3);
        List<Integer> result2 = compressor.decode(test, 3, 1);
        List<Integer> result3 = compressor.decode(test, 3, 2);
        List<Integer> result4 = compressor.decode(test, 3, 4);

        List<Integer> expected1 = Arrays.asList(16386);
        List<Integer> expected2 = Arrays.asList(127);
        List<Integer> expected3 = Arrays.asList(127, 130);
        List<Integer> expected4 = Arrays.asList(127, 130, 261);

        assertEquals(expected1, result1);
        assertEquals(expected2, result2);
        assertEquals(expected3, result3);
        assertEquals(expected4, result4);
    }

}