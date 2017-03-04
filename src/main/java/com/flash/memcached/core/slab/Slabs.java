package com.flash.memcached.core.slab;

/**
 * https://github.com/memcached/memcached/blob/master/memcached.h
 * <p>
 * Creation Date: 3/4/2017 <br>
 * Creation Time: 1:52 AM <br>
 * </p>
 *
 * @author Jichao Zhang
 */

public interface Slabs {
    /**
     * slabs_init
     * Init the subsystem.
     * @param limit the limit on no. of bytes to allocate, 0 if no limit.
     * @param factor growth factor; each slab will use a chunk size equal to the previous slab's chunk size times this factor.
     * @param preAlloc specifies if the slab allocator should allocate all memory up front (if true),
     *                 or allocate memory in chunks as it is needed (if false)
     * @param slabSizes the expected chunk size for each slab class.
     */
    void init(long limit, double factor, boolean preAlloc, long[] slabSizes);
}
