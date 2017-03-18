package com.flash.memcached.core;

/**
 * Created by zhangj52 on 3/18/2017.
 */
public class SlabClass {
    private Slab[] slabs;

    //chunk size
    private int chunkSize;
    //How many chunks per slab.
    private long perSlab;

    public SlabClass(int chunkSize, int slabCount) {
        slabs = new Slab[slabCount];
        for (int i = 0; i < slabs.length; i++) {
            slabs[i] = new Slab(chunkSize);
        }
        this.chunkSize = chunkSize;
    }

    public byte[] getChunk(int slabOffset, int chunkOffset) {
        byte[] bytes = new byte[chunkSize];
        slabs[slabOffset].getChunk(bytes, chunkOffset);
        return bytes;
    }

    public void putChunk(byte[] src, int slabOffset, int chunkOffset) {
        slabs[slabOffset].putChunk(src, chunkOffset);
    }
}
