package com.flash.memcached.core;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import java.io.*;

/**
 * Created by zhangj52 on 3/18/2017.
 */
public class SlabMemory {
    private SlabClass[] slabClasses;

    public SlabMemory() {
        //TODO: init
        slabClasses = new SlabClass[10];
        for(int i=0; i < slabClasses.length; i++) {
            SlabClass slabClass = new SlabClass(300, 3);
            slabClasses[i] = slabClass;
        }
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

            targetSlabClass.addItemBytes(objBytes);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Item getItem(String key) {
        //Based on key, I have to find item location (which slab class, which slab, which chunk)
        // and chunk boundary (how?) Need a map chunk: length.
        //TODO
        int slabClassIdx = 0;
        byte[] chunkBYtes = slabClasses[0].getChunk(0, 1);
        int itemBytesLengh = 118;//Need a map to get this info from key.
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(chunkBYtes, 0, itemBytesLengh);
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
