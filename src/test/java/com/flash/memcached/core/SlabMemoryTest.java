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
    }
}
