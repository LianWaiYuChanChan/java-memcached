package com.flash.memcached.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by zhangj52 on 3/18/2017.
 */
public class SlabClass {




    static class ChunkLocation {

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

        @Override
        public boolean equals(Object o) {
            if (this == o){
                return true;
            }
            if (o == null || getClass() != o.getClass()){
                return false;
            }

            ChunkLocation that = (ChunkLocation) o;

            return slabIdx == that.slabIdx && chunkIdx == that.chunkIdx;
        }

        /**
         * Just generate hashCode by IntelliJ. TODO: need improve?
         * @return
         */
        @Override
        public int hashCode() {
            int result = slabIdx;
            result = 31 * result + chunkIdx;
            return result;
        }

        private int slabIdx;
        private int chunkIdx;
    }


    Set<ChunkLocation> freeChunkSet = new HashSet<>();
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

        //Compute perSlab
        this.perSlab = slabs[0].getTotalSize()/chunkSize;

        //Construct freeChunkSet
        for(int i =0; i < slabCount; i++) {
            for(int j = 0; j < perSlab; j++) {
                ChunkLocation chunkLoc = new ChunkLocation(i, j);
                freeChunkSet.add(chunkLoc);
            }
        }

    }

    public byte[] getChunk(int slabOffset, int chunkOffset) {
        byte[] bytes = new byte[chunkSize];
        slabs[slabOffset].getChunk(bytes, chunkOffset);
        return bytes;
    }

    public byte[] getChunk(ChunkLocation chunkLoc) {
        return getChunk(chunkLoc.getSlabIdx(), chunkLoc.getChunkIdx());
    }

    public void freeChunk(ChunkLocation chunkLoc) {
        freeChunkSet.add(chunkLoc);
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
        return freeChunkSet.isEmpty();
    }

    public ChunkLocation addItemBytes(byte[] objBytes) {
        ChunkLocation chunkLoc = pickProperChunk();
        putChunk(objBytes, chunkLoc.getSlabIdx(), chunkLoc.getChunkIdx());
        markChunkAsOccupied(chunkLoc);
        return chunkLoc;
    }

    public void updateItemBytes(byte[] objBytes, ChunkLocation chunkLoc) {
        putChunk(objBytes, chunkLoc.getSlabIdx(), chunkLoc.getChunkIdx());
    }

    private void markChunkAsOccupied(ChunkLocation chunkLoc) {
        freeChunkSet.remove(chunkLoc);
    }

    private ChunkLocation pickProperChunk() {
        if (isFull()) {
            //TODO
            throw new IllegalStateException("Empty free chunk set. May need implement LRU.");
        }
        Iterator<ChunkLocation> iter = freeChunkSet.iterator();
        ChunkLocation freeChunk = iter.next();
        return freeChunk;
    }

}
