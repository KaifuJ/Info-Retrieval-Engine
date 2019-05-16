package edu.uci.ics.cs221.index.positional;

import edu.uci.ics.cs221.index.inverted.DeltaVarLenCompressor;
import edu.uci.ics.cs221.index.inverted.NaiveCompressor;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class Team3CompressorTest {
    private DeltaVarLenCompressor deltaVarLenCompressor;
    private NaiveCompressor naiveCompressor;
    @Before
    public void initialize() {
        deltaVarLenCompressor = new DeltaVarLenCompressor();
        //naiveCompressor = new NaiveCompressor();
    }

    //Test encoding, should be 1 1 1 1 1 1
    @Test
    public void test1() {
        List<Integer> integers1 = Arrays.asList(1, 2, 3, 4, 5, 6); //after delta encoding 1,1,1,1,1,1
        byte[] expected1 = {(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01};
        assertArrayEquals(expected1, deltaVarLenCompressor.encode(integers1));
    }

    //Test encoding, after delta encoding they should be 128,256,512,1024,2048
    @Test
    public void test2() {
        List<Integer> integers2 = Arrays.asList(128, 384, 896, 1920, 3968);//after delta encoding 128,256,512,1024,2048
        byte[] expected2 = {(byte) 0x81, (byte) 0x00, (byte) 0x82, (byte) 0x00, (byte) 0x84, (byte) 0x00, (byte) 0x88, (byte) 0x00,
                (byte) 0x90, (byte) 0x00};
        assertArrayEquals(expected2, deltaVarLenCompressor.encode(integers2));
    }
    //Test encoding the largest Integer.
    @Test
    public void test3() {
        List<Integer> integers3 = Arrays.asList(Integer.MAX_VALUE);
        byte[] expected3 = {(byte)0x87,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0x7f};
        assertArrayEquals(expected3,deltaVarLenCompressor.encode(integers3));
    }
    //Test decoding of all bytes.
    @Test
    public void test4() {
        List<Integer> expected1 = Arrays.asList(1, 2, 3, 4, 5, 6); //after delta encoding 1,1,1,1,1,1
        byte[] bytes1 = {(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01};
        List<Integer> actual1 = deltaVarLenCompressor.decode(bytes1);
        assertEquals(expected1.size(),actual1.size());
        for(int i=0;i<expected1.size();i++){
            assertEquals(expected1.get(i),actual1.get(i));
        }
    }

    @Test
    public void test5() {
        List<Integer> expected2 = Arrays.asList(128, 384, 896, 1920, 3968);//after delta encoding 128,256,512,1024,2048
        byte[] bytes2 = {(byte) 0x81, (byte) 0x00, (byte) 0x82, (byte) 0x00, (byte) 0x84, (byte) 0x00, (byte) 0x88, (byte) 0x00,
                (byte) 0x90, (byte) 0x00};
        List<Integer> actual2 = deltaVarLenCompressor.decode(bytes2);
        assertEquals(expected2.size(),actual2.size());
        for(int i=0;i<expected2.size();i++){
            assertEquals(expected2.get(i),actual2.get(i));
        }
    }
    @Test
    public void test6() {
        List<Integer> expected3 = Arrays.asList(Integer.MAX_VALUE);
        byte[] bytes3 = {(byte)0x87,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0x7f};
        List<Integer> actual3 = deltaVarLenCompressor.decode(bytes3);
        assertEquals(expected3.size(),actual3.size());
        for(int i=0;i<expected3.size();i++){
            assertEquals(expected3.get(i),actual3.get(i));
        }
    }
    //Test decoding of some of the bytes
    @Test
    public void test7() {
        byte[] bytes1 = {(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01};
        List<Integer> actual1 = deltaVarLenCompressor.decode(bytes1,4,2);
        List<Integer> expected1 = Arrays.asList(1,2);
        assertEquals(expected1.size(),actual1.size());
        for(int i=0;i<expected1.size();i++){
            assertEquals(expected1.get(i),actual1.get(i));
        }
    }

    @Test
    public void test8() {
        byte[] bytes2 = {(byte) 0x81, (byte) 0x00, (byte) 0x82, (byte) 0x00, (byte) 0x84, (byte) 0x00, (byte) 0x88, (byte) 0x00,
                (byte) 0x90, (byte) 0x00};
        List<Integer> actual2 = deltaVarLenCompressor.decode(bytes2,4,6);
        List<Integer> expected2 = Arrays.asList(512,1536,3584);
        assertEquals(expected2.size(),actual2.size());
        for(int i=0;i<expected2.size();i++){
            assertEquals(expected2.get(i),actual2.get(i));
        }
    }
}
