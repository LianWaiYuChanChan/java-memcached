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
        Command cmd = Command.instanceOf("set mykey 0 60 4\r\ndata\r\n");
        assertEquals("set", cmd.getName());
        assertEquals("mykey", cmd.getKey());
        assertEquals(0, cmd.getFlags());
        assertEquals(60, cmd.getTtl());
        assertEquals(4, cmd.getLength());
        assertEquals("data", cmd.getValue());

        //Basic get.
        cmd = Command.instanceOf("get mykey\r\n");
        assertEquals("get", cmd.getName());
        assertEquals("mykey", cmd.getKey());
    }
}
