package com.flash.memcached.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Created by zhangj52 on 3/18/2017.
 */
public class SlabClass {


    private static class ChunkLocation {

        public int getSlabIdx() {
            return slabIdx;
        }

        public void setSlabIdx(int slabIdx) {
            this.slabIdx = slabIdx;
        }

        public int getChunkIdx() {
            return chunkIdx;
        }

        public void setChunkIdx(int chunkIdx) {
            this.chunkIdx = chunkIdx;
        }

        public ChunkLocation(int slabIdx, int chunkIdx) {
            this.slabIdx = slabIdx;
            this.chunkIdx = chunkIdx;
        }

        private int slabIdx;
        private int chunkIdx;
    }

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

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public boolean isFull() {
        //TODO:
        return false;
    }

    public void addItemBytes(byte[] objBytes) {
        ChunkLocation chunkLoc = pickProperChunk();
        putChunk(objBytes, chunkLoc.getSlabIdx(), chunkLoc.getChunkIdx());
    }

    private ChunkLocation pickProperChunk() {
        //TODO
        return new ChunkLocation(0, 1);
    }


}
