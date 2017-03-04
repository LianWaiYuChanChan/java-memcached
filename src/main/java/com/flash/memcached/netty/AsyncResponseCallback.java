package com.flash.memcached.netty;

import java.util.Calendar;

/**
 * Created by sherman on 2017/3/4.
 */
public abstract class AsyncResponseCallback {
    public abstract void asyncResponse(String result);
    public long getCallingKey() {
        return Calendar.getInstance().getTimeInMillis();
    }
}
