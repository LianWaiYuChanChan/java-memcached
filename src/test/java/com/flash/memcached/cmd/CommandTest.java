package com.flash.memcached.cmd;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <p>
 * Creation Date: 2/26/2017 <br>
 * Creation Time: 1:53 AM <br>
 * </p>
 *
 * @author Jichao Zhang
 */

public class CommandTest {

    @Test
    public void testCommand() {

        //Basic set.
        Command cmd = Command.instanceOf("set mykey 0 60 4\r\ndata\r\n 1283838747");
        assertEquals("set", cmd.getName());
        assertEquals("mykey", cmd.getKey());
        assertEquals(0, cmd.getFlags());
        assertEquals(60, cmd.getTtl());
        assertEquals(4, cmd.getLength());
        assertEquals("data", cmd.getValue());
        assertEquals("1283838747", cmd.getCallingkey());

        //Basic get.
        cmd = Command.instanceOf("get mykey\r\n 1283838747");
        assertEquals("get", cmd.getName());
        assertEquals("mykey", cmd.getKey());
        assertEquals("1283838747", cmd.getCallingkey());
    }
}
