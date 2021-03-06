package com.flash.memcached.core;

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

    public void addItem(String key, Object value, Options options) {
        int expireTimeout = options.getExpireTimeout();
        long currentTime = System.currentTimeMillis()/1000L;
        long expireTime = currentTime + expireTimeout;
        Item item = new Item(key, value, expireTime);
        addItem(item);
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

    public void updateItem(Item item) {
        String key = item.getKey();
        if (!checkItemExisted(key)) {
            throw new IllegalArgumentException("Item cannot be found with key: " + key);
        }

        ItemLocation itemLoc = keyToItemLoc.get(key);
        SlabClass slabClass = slabClasses[itemLoc.getSlabClassIdx()];

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(item);
            byteArrayOutputStream.close();
            byte[] objBytes = byteArrayOutputStream.toByteArray();
            if (objBytes.length > slabClass.getChunkSize()) {
                throw new IllegalArgumentException("Item is too big with key: " + key);
            }

            slabClass.updateItemBytes(objBytes, itemLoc.getChunkLoc());
            itemLocToLength.put(itemLoc, objBytes.length);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private boolean checkItemExisted(String key) {
        ItemLocation itemLoc = keyToItemLoc.get(key);
        if (itemLoc == null) {
            return false;
        }
        Item itemInSlab = doGetItem(key);
        boolean expired = itemInSlab.isExpired();
        if (expired) {
            //Clear it.
            clearItemByKey(key);
        }
        return !expired;
    }

    private void clearItemByKey(String key) {
        ItemLocation itemLoc = keyToItemLoc.get(key);
        int slabClassIdx = itemLoc.getSlabClassIdx();
        SlabClass.ChunkLocation chunkLoc = itemLoc.getChunkLoc();

        //FreeChunkMap and KeyToItemLoc are coupled. can be constructed as one class?
        slabClasses[slabClassIdx].freeChunk(chunkLoc);
        keyToItemLoc.remove(key);
    }

    public void deleteItem(String key) {
        if (!checkItemExisted(key)) {
            //Keep silent for deleting non-exist key.
            return;
        }

        ItemLocation itemLoc = keyToItemLoc.get(key);
        int slabClassIdx = itemLoc.getSlabClassIdx();
        SlabClass.ChunkLocation chunkLoc = itemLoc.getChunkLoc();

        //FreeChunkMap and KeyToItemLoc are coupled. can be constructed as one class?
        slabClasses[slabClassIdx].freeChunk(chunkLoc);
        keyToItemLoc.remove(key);
    }

    public Item getItem(String key) {
        ItemLocation itemLoc = keyToItemLoc.get(key);
        if (itemLoc == null) {
            return null;
        }
        Item itemInSlab = doGetItem(key);
        boolean expired = itemInSlab.isExpired();
        if (expired) {
            //Clear it.
            clearItemByKey(key);
            return null;
        } else {
            return itemInSlab;
        }
    }

    public Item doGetItem(String key) {

        ItemLocation itemLocation = keyToItemLoc.get(key);
        if (itemLocation == null) {
            return null;
        }
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
