package com.flash.memcached.client;

import com.flash.memcached.cmd.Command;
import com.flash.memcached.netty.NettyServer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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

    private static class NettyThread implements Runnable {
        private int port;
        public NettyThread(int port) {

            this.port = port;
        }

        @Override
        public void run() {
            System.out.println("NettyThread started.");
            NettyServer.start(port);
            System.out.println("NettyThread stopped.");
        }
    }

    @Before
    public void startMemcachedServer() throws Exception {
        NettyThread nettyThread = new NettyThread(port);
        new Thread(nettyThread).start();
        //wait for server startup completely.
        Thread.sleep(10 * 1000);

    }

    @Test
    public void testCommand() {
        //Basic set.
        CommandService cmdService = new CommandServiceImpl(host, port);
        String ret = cmdService.executeCmd("set mykey 0 60 4\r\ndata\r\n");
        //TODO: test will hang here.
        System.err.println(ret);
    }
}
