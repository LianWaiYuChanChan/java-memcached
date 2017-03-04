package com.flash.memcached.client;

import com.flash.memcached.netty.NettyServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


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
    private NettyServer nettyServer;

    private class NettyThread implements Runnable {
        private NettyServer nettyServer;

        public NettyThread(NettyServer nettyServer) {
            this.nettyServer = nettyServer;
        }

        @Override
        public void run() {
            System.out.println("NettyThread started.");
            nettyServer.start();
            System.out.println("NettyThread stopped.");
        }
    }

    @Before
    public void startMemcachedServer() throws Exception {
         nettyServer = NettyServer.getInstance(port);
         NettyThread nettyThread = new NettyThread(nettyServer);
         new Thread(nettyThread).start();
         //wait for server startup completely.
         Thread.sleep(10 * 1000);
    }

    @After
    public void closeMemcachedServer() {
        if (nettyServer.isRunning()) {
            nettyServer.shutdown();
        }
    }

    @Test
    public void testCommand() {
        //Basic set.
         CommandService cmdService = new CommandServiceImpl(host, port);
         String ret = cmdService.executeCmd("set mykey 0 60 4\r\ndata\r\n");
         //TODO: test will hang here.
         System.err.println(ret);
         ret = cmdService.executeCmd("get mykey");
         System.err.println(ret);
         assertEquals("data", ret);
    }
}
