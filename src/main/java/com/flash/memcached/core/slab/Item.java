package com.flash.memcached.core.slab;

/**
 * item struct at https://github.com/memcached/memcached/blob/master/memcached.h
 * Structure for storing items within memcached.
 * <p>
 * Creation Date: 3/4/2017 <br>
 * Creation Time: 1:32 AM <br>
 * </p>
 *
 * @author Jichao Zhang
 */

public class Item {
    public final static short ITEM_LINKED = 1;
    public final static short ITEM_CAS = 2;
    /* temp */
    public final static short ITEM_SLABBED = 4;
    /* Item was fetched at least once in its lifetime */
    public final static short ITEM_FETCHED = 8;
    /* Appended on fetch, removed on LRU shuffling */
    public final static short ITEM_ACTIVE = 16;
    /* If an item's storage are chained chunks. */
    public final static short ITEM_CHUNKED = 32;
    public final static short ITEM_CHUNK = 64;

    /* Protected by LRU locks */
    private Item next;
    private Item prev;
    /* Rest are protected by an item lock */
    /* hash chain next */
    private Item hNext;
    /* least recent access */
    private long time;
    /* expire time */
    private long expTime;
    /* size of data */
    int nBytes;
    int refCount;
    /* length of flags-and-length string */
    short nSuffix;
    /* ITEM_* above */
    short itFlags;
    /* which slab class we're in */
    short slabsClsId;

    /* key length, w/terminating null and padding */
    short nKey;
    /* TODO: this odd type prevents type-punning issues when we do
     the little shuffle to save space when not using CAS.
        union {
            uint64_t cas;
            char end;
        } data[];
    if it_flags & ITEM_CAS we have 8 bytes CAS
     then null-terminated key
    then " flags length\r\n" (no terminating null)
    then data with terminating \r\n (no terminating null; it's binary!)
     */
}
