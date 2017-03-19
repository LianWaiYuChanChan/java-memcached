package com.flash.memcached.core;

import java.nio.ByteBuffer;

/**
 * Created by zhangj52 on 3/18/2017.
 */
public class Slab {
    private ByteBuffer byteBuffer = null;
    private int chunkSize;
    private int totalSize;
    private static int MB = 1024*1024;

    public Slab(int chunkSize) {
        //Default 1 MB.
        byteBuffer = ByteBuffer.allocateDirect(MB);
        this.totalSize = MB;
        this.chunkSize = chunkSize;
    }

    public int getTotalSize(){
        return this.totalSize;
    }

    public void getChunk(byte[] dest, int chunkOffSet) {
        byteBuffer.position(chunkOffSet * chunkSize);
        byteBuffer.get(dest, 0, dest.length);
    }

    public void putChunk(byte[] src, int chunkOffSet) {
        int startIdx = chunkOffSet * chunkSize;
        for(int i = 0; i < src.length; i++) {
            byteBuffer.put(startIdx++, src[i]);
        }
    }
}

