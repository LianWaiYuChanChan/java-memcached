package com.flash.memcached.core;

/**
 * Created by zhangj52 on 3/19/2017.
 */
public class Options {

    public int getExpireTimeout() {
        return expireTimeout;
    }

    public void setExpireTimeout(int expireTimeout) {
        this.expireTimeout = expireTimeout;
    }

    public Options(int expireTimeout) {
        this.expireTimeout = expireTimeout;
    }

    //in seconds
    private int expireTimeout;
}
