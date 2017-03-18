package com.flash.memcached.core;

import java.io.Serializable;

/**
 * Created by zhangj52 on 3/18/2017.
 */
public class Item implements Serializable {

    public Item(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    private String key;
    private Object value;

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
