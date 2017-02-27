package com.flash.memcached.client;

/**
 * <p>
 * Creation Date: 2/27/2017 <br>
 * Creation Time: 7:30 AM <br>
 * </p>
 *
 * @author Jichao Zhang
 */

public interface CommandService {
    String executeCmd(String cmd);
}
