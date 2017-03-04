package com.flash.memcached.core.slab;

/**
 * Corresponding the slabclass_t struct in slabs.c: https://github.com/memcached/memcached/blob/master/slabs.c
 * <p>
 * Creation Date: 3/4/2017 <br>
 * Creation Time: 1:14 AM <br>
 * </p>
 *
 * @author Jichao Zhang
 */

public class SlabClass {
    /* slab class max is a 6-bit number, -1. */
    public static long MAX_NUMBER_OF_SLAB_CLASSES = (63 + 1);

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    //size of items
    private long size;

    public long getPerSlab() {
        return perSlab;
    }

    public void setPerSlab(long perSlab) {
        this.perSlab = perSlab;
    }

    //how many items per slab
    private long perSlab;

    //list of times ptrs
    //TODO void *slots

    //Total free items in list
    private long sl_curr;

    //how many slabs were allocated for this class
    private long slabs;

    //Array of slab pointers
    //TODO void **slab_list;

    //size of prev array.
    private long list_size;

    //size_t requested The number of requested bytes
    private long requested;

}
