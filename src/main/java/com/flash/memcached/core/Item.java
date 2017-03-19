package com.flash.memcached.core;

import java.io.Serializable;

/**
 * Created by zhangj52 on 3/18/2017.
 */
public class Item implements Serializable {


    public Item(String key, Object value) {
        this(key, value, Long.MAX_VALUE);
    }

    private String key;
    private Object value;
    private long expireTime;

    public Item(String key, Object value, long expireTime) {
        this.key = key;
        this.value = value;
        this.expireTime = expireTime;
    }

    public boolean isExpired() {
        long currentTime = System.currentTimeMillis()/1000L;
        if (currentTime > expireTime) {
            return true;
        }
        return false;
    }


    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Key: ").append(key).append("\n");
        sb.append("Value: ").append(value).append("\n");
        return sb.toString();
    }

}
