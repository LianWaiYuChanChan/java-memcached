package com.flash.memcached.core;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangj52 on 3/18/2017.
 */
public class SlabMemory {
    private static class ItemLocation {
        public ItemLocation(int slabClassIdx, SlabClass.ChunkLocation chunkLocation) {
            this.slabClassIdx = slabClassIdx;
            this.chunkLoc = chunkLocation;
        }

        /**
         * Generated by IntelliJ.
         * @param o
         * @return
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ItemLocation that = (ItemLocation) o;

            if (slabClassIdx != that.slabClassIdx) return false;
            return chunkLoc != null ? chunkLoc.equals(that.chunkLoc) : that.chunkLoc == null;

        }

        @Override
        public int hashCode() {
            int result = slabClassIdx;
            result = 31 * result + (chunkLoc != null ? chunkLoc.hashCode() : 0);
            return result;
        }

        public int getSlabClassIdx() {
            return slabClassIdx;
        }

        public void setSlabClassIdx(int slabClassIdx) {
            this.slabClassIdx = slabClassIdx;
        }

        public SlabClass.ChunkLocation getChunkLoc() {
            return chunkLoc;
        }

        public void setChunkLoc(SlabClass.ChunkLocation chunkLoc) {
            this.chunkLoc = chunkLoc;
        }

        private int slabClassIdx;
        private SlabClass.ChunkLocation chunkLoc;

    }

    private Map<ItemLocation, Integer> itemLocToLength;
    private Map<String, ItemLocation> keyToItemLoc;

    private SlabClass[] slabClasses;

    public SlabMemory() {
        //TODO: init
        slabClasses = new SlabClass[10];
        for(int i=0; i < slabClasses.length; i++) {
            SlabClass slabClass = new SlabClass(300, 3);
            slabClasses[i] = slabClass;
        }
        this.itemLocToLength = new HashMap<>();
        this.keyToItemLoc = new HashMap<>();
    }

    public void addItem(Item item) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(item);
            byteArrayOutputStream.close();
            byte[] objBytes = byteArrayOutputStream.toByteArray();
            if (objBytes.length > 1024 * 1024) {
                throw new IllegalArgumentException("Exceed 1MB limit!");
            }
            //Find proper slab class.
            int i = 0;
            for(; i < slabClasses.length; i++) {
                if (slabClasses[i].getChunkSize() >= objBytes.length) {
                    break;
                }
            }

            if (i >= slabClasses.length) {
                throw new IllegalArgumentException("Object is too big: " + objBytes.length);
            }

            SlabClass targetSlabClass = slabClasses[i];
            if (targetSlabClass.isFull()) {
                //TODO: should LRU
                throw new IllegalStateException("Is Full.");
            }

            SlabClass.ChunkLocation chunkLocation = targetSlabClass.addItemBytes(objBytes);
            ItemLocation itemLoc = new ItemLocation(i, chunkLocation);
            itemLocToLength.put(itemLoc, objBytes.length);
            keyToItemLoc.put(item.getKey(), itemLoc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Item getItem(String key) {
        //Based on key, I have to find item location (which slab class, which slab, which chunk)
        // and chunk boundary (how?) Need a map chunk: length.
        ItemLocation itemLocation = keyToItemLoc.get(key);
        int slabClassIdx = itemLocation.getSlabClassIdx();
        byte[] chunkBytes = slabClasses[slabClassIdx].getChunk(itemLocation.getChunkLoc());
        int itemBytesLength = itemLocToLength.get(itemLocation);//Need a map to get this info from key.

        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(chunkBytes, 0, itemBytesLength);
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(byteInputStream);
            Item item = (Item)in.readObject();
            return item;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }



}
