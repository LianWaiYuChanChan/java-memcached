package com.flash.memcached.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by zhangj52 on 3/18/2017.
 */
public class SlabClassTest {

    @Test
    public void testChunkGetPut() {
        SlabClass slabClass = new SlabClass(64, 2);
        byte[] src = new byte[]{0, 1, 2, 3};
        slabClass.putChunk(src, 0, 10);
        byte[] bytesGot = slabClass.getChunk(0, 10);
        for (byte val : bytesGot) {
            System.out.print(Integer.toHexString(val));
        }
        assertEquals(0, bytesGot[0]);
        assertEquals(1, bytesGot[1]);
        assertEquals(2, bytesGot[2]);
        assertEquals(3, bytesGot[3]);
    }

}
