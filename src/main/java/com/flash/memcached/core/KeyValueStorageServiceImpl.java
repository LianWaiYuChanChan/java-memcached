package com.flash.memcached.core;

import com.flash.memcached.cmd.Command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Creation Date: 2/26/2017 <br>
 * Creation Time: 1:48 AM <br>
 * </p>
 *
 * @author Jichao Zhang
 */

public class KeyValueStorageServiceImpl implements KeyValueStorageService {
    //Temp
    private Map<String, String> storage = new ConcurrentHashMap<>();

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public String executeCmd(String cmd) {
        Command command = Command.instanceOf(cmd);
        String cmdName = command.getName();
        String result = "";
        if (cmdName.equals("set")) {
            storage.put(command.getKey(), command.getValue());
            result = "STORE";
        } else if (cmdName.equals("get")) {
            String value = storage.get(command.getKey());
            result = value == null ? "" : value;
        }
        return result;
    }

}
