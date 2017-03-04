package com.flash.memcached.core.slab;

import static com.flash.memcached.core.slab.SlabClass.MAX_NUMBER_OF_SLAB_CLASSES;

/**
 * https://github.com/memcached/memcached/blob/master/memcached.c
 * <p>
 * Creation Date: 3/4/2017 <br>
 * Creation Time: 1:52 AM <br>
 * </p>
 *
 * @author Jichao Zhang
 */

public class SlabsImpl implements Slabs {
    /* Slab sizing definitions. */
    private static long POWER_SMALLEST = 1;
    /* actual cap is 255 */
    private static long POWER_LARGEST = 256;
    /* magic slab class for storing pages for reassignment */
    private static long SLAB_GLOBAL_PAGE_POOL = 0;
    private static long CHUNK_ALIGN_BYTES = 8;

    private SlabClass[] slabClass = null;

    private long memLimit;

    @Override
    public void init(long limit, double factor, boolean preAlloc, long[] slabSizes) {
        //unsigned int size = sizeof(item) + settings.chunk_size;
        Settings settings = new Settings();//TODO, data source
        long size = 100L;//TODO.
        this.memLimit = limit;
        this.slabClass = new SlabClass[(int) MAX_NUMBER_OF_SLAB_CLASSES];
        int i = (int) POWER_SMALLEST - 1;

        while (++i < MAX_NUMBER_OF_SLAB_CLASSES - 1) {

            if (slabSizes != null) {
                if (slabSizes[i - 1] == 0) {
                    break;
                }
                size = slabSizes[i - 1];
            } else if (size >= settings.getSlabChunkSizeMax() / factor) {
                break;
            }

            /* Make sure items are always n-byte aligned. make sure size%CHUN_ALIGN_BYTES = 0 */
            if (size % CHUNK_ALIGN_BYTES != 0) {
                size += CHUNK_ALIGN_BYTES - (size % CHUNK_ALIGN_BYTES);
            }

            SlabClass currentSlabClass = slabClass[i];
            currentSlabClass.setSize(size);
            long perSlab = settings.getSlabPageSize() / currentSlabClass.getSize();
            currentSlabClass.setPerSlab(perSlab);

            if (slabSizes == null) {
                size *= factor;
            }

            if (settings.verbose > 1) {
                // memcached -vv will print this.
                System.err.println("slab class " + i + ": chunk size " + currentSlabClass.getSize()
                        + "perslab " + currentSlabClass.getPerSlab() + "\n");
            }
            //TODO: Continue.

        }

    }
}
