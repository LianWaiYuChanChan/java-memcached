package com.flash.memcached.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by zhangj52 on 3/18/2017.
 */
public class SlabMemoryTest {
    @Test
    public void testAddItem() {

        SlabMemory slabMemory = new SlabMemory();
        Item item = new Item("One", "Two");
        slabMemory.addItem(item);
        item = slabMemory.getItem("One");
        System.out.println(item);
        assertEquals("Two", item.getValue());

        item = new Item("Two", "Three");
        slabMemory.addItem(item);
        item = slabMemory.getItem("Two");
        System.out.println(item);
        assertEquals("Three", item.getValue());

        item = slabMemory.getItem("Four");
        assertEquals(null, item);
    }

    @Test
    public void testDeleteItem(){
        SlabMemory slabMemory = new SlabMemory();
        Item item = new Item("One", "Two");
        slabMemory.addItem(item);
        item = slabMemory.getItem("One");
        assertEquals("Two", item.getValue());

        slabMemory.deleteItem("One");
        assertEquals(null, slabMemory.getItem("One"));
    }


    @Test
    public void testUpdateItem(){
        SlabMemory slabMemory = new SlabMemory();
        Item item = new Item("One", "Two");
        slabMemory.addItem(item);
        item = slabMemory.getItem("One");
        assertEquals("Two", item.getValue());

        item = new Item("One", "NewValue");
        slabMemory.updateItem(item);
        item = slabMemory.getItem("One");
        assertEquals("NewValue", item.getValue());
    }

    @Test
    public void testExpireFunction() throws InterruptedException {
        SlabMemory slabMemory = new SlabMemory();
        Options options = new Options(5);
        slabMemory.addItem("One", "Two", options);
        Item item = slabMemory.getItem("One");
        assertEquals("Two", item.getValue());
        Thread.sleep(10 * 1000);
        item = slabMemory.getItem("One");
        assertEquals(null, item);
    }
}
