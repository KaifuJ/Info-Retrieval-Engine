package edu.uci.ics.cs221.index.inverted;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Implement this compressor with Delta Encoding and Variable-Length Encoding.
 * See Project 3 description for details.
 */
public class DeltaVarLenCompressor implements Compressor {

    @Override
    public byte[] encode(List<Integer> integers) {
        int pre = 0;
        List<Integer> bytes = new ArrayList<>();
        for(int i = 0; i < integers.size(); i++){
            int tmp = integers.get(i) - pre;
            if(tmp == 0){
                bytes.add(0);
                continue;
            }
            List<Integer> bytelist = new ArrayList<>();
            int indexcount = 0;
            while(tmp > 0){
                byte a = (byte) (tmp % 128) ;
                if(indexcount > 0) bytelist.add(a | (byte)0b10000000);
                else bytelist.add((int)a);
                indexcount += 1;
                tmp = tmp / 128;
            }
            Collections.reverse(bytelist);
            bytes.addAll(bytelist);
            pre = integers.get(i) ;
        }
        byte[] res = new byte[bytes.size()];
        for(int i = 0; i < bytes.size(); i++){
            res[i] = (byte) (int) bytes.get(i);
        }
        return res ;
    }

    @Override
    public List<Integer> decode(byte[] bytes, int start, int length) {
        int index = start;
        int total = 0;
        List<Integer> res = new ArrayList<>();
        while(index < start + length){
            int count = 0;
            while(bytes[index]  < 0 && index < start + length - 1){
                count += bytes[index] & (byte)0b01111111;
                count *= 128 ;
                index += 1;
            }
            count += bytes[index] & (byte)0xff;
            index += 1;
            res.add(count+total);
            total += count;
        }
        return res;
    }
}
