package com.flash.memcached.core.slab;

import java.math.BigInteger;

/**
 * settings struct at https://github.com/memcached/memcached/blob/master/memcached.h
 * <p>
 * Creation Date: 3/4/2017 <br>
 * Creation Time: 2:28 AM <br>
 * </p>
 *
 * @author Jichao Zhang
 */

public class Settings {
    /* When adding a setting, be sure to update process_stat_settings */
    /**
     * Globally accessible settings as derived from the commandline.
     */

    long maxbytes;
    int maxconns;
    int port;
    int udpport;
    String inter;
    int verbose;
    long oldest_live; /* ignore existing items older than this */
    BigInteger oldest_cas; /* ignore existing items with CAS values lower than this */
    int evict_to_free;
    String socketpath;   /* path to unix socket if using local socket */
    int access;  /* access mask (a la chmod) for unix domain socket */
    double factor;          /* chunk size growth factor */
    int chunk_size;
    int num_threads;        /* number of worker (without dispatcher) libevent threads to run */
    int num_threads_per_udp; /* number of worker threads serving each udp socket */
    char prefix_delimiter;  /* character that marks a key prefix (for stats) */
    int detail_enabled;     /* nonzero if we're collecting detailed stats */
    int reqs_per_event;     /* Maximum number of io to process on each
                               io-event. */
    boolean use_cas;
    //enum protocol binding_protocol;
    int backlog;
    int item_size_max;        /* Maximum item size */

    public int getSlabChunkSizeMax() {
        return slabChunkSizeMax;
    }

    public void setSlabChunkSizeMax(int slabChunkSizeMax) {
        this.slabChunkSizeMax = slabChunkSizeMax;
    }

    int slabChunkSizeMax;  /* Upper end for chunks within slab pages. */

    public int getSlabPageSize() {
        return slabPageSize;
    }

    public void setSlabPageSize(int slabPageSize) {
        this.slabPageSize = slabPageSize;
    }

    int slabPageSize;     /* Slab's page units. */

    boolean sasl;              /* SASL on/off */
    boolean maxconns_fast;     /* Whether or not to early close connections */
    boolean lru_crawler;        /* Whether or not to enable the autocrawler thread */
    boolean lru_maintainer_thread; /* LRU maintainer background thread */
    boolean lru_segmented;     /* Use split or flat LRU's */
    boolean slab_reassign;     /* Whether or not slab reassignment is allowed */
    int slab_automove;     /* Whether or not to automatically move slabs */
    int hashpower_init;     /* Starting hash power level */
    boolean shutdown_command; /* allow shutdown command */
    int tail_repair_time;   /* LRU tail refcount leak repair time */
    boolean flush_enabled;     /* flush_all enabled */
    boolean dump_enabled;      /* whether cachedump/metadump commands work */
    String hash_algorithm;     /* Hash algorithm in use */
    int lru_crawler_sleep;  /* Microsecond sleep between items */
    long lru_crawler_tocrawl; /* Number of items to crawl per run */
    int hot_lru_pct; /* percentage of slab space for HOT_LRU */
    int warm_lru_pct; /* percentage of slab space for WARM_LRU */
    long hot_max_age; /* max idle time before move from HOT_LRU */
    double warm_max_factor; /* WARM tail age relative to COLD tail */
    int crawls_persleep; /* Number of LRU crawls to run before sleeping */
    boolean inline_ascii_response; /* pre-format the VALUE line for ASCII responses */
    boolean temp_lru; /* TTL < temporary_ttl uses TEMP_LRU */
    boolean temporary_ttl; /* temporary LRU threshold */
    int idle_timeout;       /* Number of seconds to let connections idle */
    long logger_watcher_buf_size; /* size of logger's per-watcher buffer */
    long logger_buf_size; /* size of per-thread logger buffer */
}
