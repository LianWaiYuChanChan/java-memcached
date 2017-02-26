package com.flash.memcached.cmd;

import com.flash.memcached.logging.Logger;

/**
 * <p>
 * Creation Date: 2/26/2017 <br>
 * Creation Time: 1:50 AM <br>
 * </p>
 *
 * @author Jichao Zhang
 */

public class Command {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getFlags() {
        return flags;
    }

    public void setFlags(long flags) {
        this.flags = flags;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private String key;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private String name;
    private long flags;
    private long ttl;
    private long length;
    private String value;

    public static Command instanceOf(String cmd) {
        Logger.log(cmd);

        String cmdTemp = cmd.replace("\r\n", " ").trim();

        String[] tokens = cmdTemp.split(" ");
        Command command = null;
        String firstToken = tokens[0];
        if ("set".equals(firstToken)) {
            // "set mykey 0 60 4\r\ndata\r\n"
            command = new Command("set");
            command.setKey(tokens[1]);
            command.setFlags(Long.parseLong(tokens[2]));
            command.setTtl(Long.parseLong(tokens[3]));
            command.setLength(Long.parseLong(tokens[4]));
            command.setValue(tokens[5]);
        } else if ("get".equals(firstToken)) {
            command = new Command("get");
            command.setKey(tokens[1]);

        }

        return command;
    }

    private Command(String name) {
        this.name = name;

    }
}
