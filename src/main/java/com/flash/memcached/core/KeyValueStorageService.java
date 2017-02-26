package com.flash.memcached.core;

/**
 * <p>
 * Creation Date: 2/26/2017 <br>
 * Creation Time: 1:43 AM <br>
 * </p>
 *
 * @author Jichao Zhang
 */

public interface KeyValueStorageService {
    String get(String key);

    String executeCmd(String cmd);
}
