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
        StringBuilder result = new StringBuilder();
        if (command != null) {
            String cmdName = command.getName();
            if (cmdName.equals("set")) {
                storage.put(command.getKey(), command.getValue());
                result.append("STORE");
            } else if (cmdName.equals("get")) {
                String value = storage.get(command.getKey());
                if (value != null) {
                    result.append(value);
                }
            }
            if (command.getCallingkey() > 0) {
                result.append(Command.TOKEN_SPLITTER).append(command.getCallingkey());
            }
        }
        return result.toString();
    }

}
