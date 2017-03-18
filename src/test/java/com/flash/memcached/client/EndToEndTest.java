package com.flash.memcached.client;

import com.flash.memcached.netty.NettyServer;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>
 * Creation Date: 2/27/2017 <br>
 * Creation Time: 7:33 AM <br>
 * </p>
 *
 * @author Jichao Zhang
 */

public class EndToEndTest {
    private int port = 8000;

    private String host = "127.0.0.1";

    //TODO: @Test
    public void testCommand() {
        //Basic set.
        CommandService cmdService = new CommandServiceImpl(host, port);
        String ret = cmdService.executeCmd("set mykey 0 60 4\r\ndata\r\n");
        //TODO: test will hang here.
        System.err.println(ret);
        System.out.println("Try to interrupt memcached trhead");
    }
}
