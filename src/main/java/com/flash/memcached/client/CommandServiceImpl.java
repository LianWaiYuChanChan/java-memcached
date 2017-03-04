package com.flash.memcached.client;

import com.flash.memcached.netty.AsyncResponseCallback;
import com.flash.memcached.netty.MemcachedClient;

import java.util.concurrent.CountDownLatch;

/**
 * <p>
 * Creation Date: 2/27/2017 <br>
 * Creation Time: 7:32 AM <br>
 * </p>
 *
 * @author Jichao Zhang
 */

public class CommandServiceImpl implements CommandService {
    MemcachedClient client;
    public CommandServiceImpl(String host, int port) {
        client = new MemcachedClient(host, port);
    }

    @Override
    public boolean isAlive() {
        return client != null && client.getMemcachedConnector().isConnected();
    }

    @Override
    public String executeCmd(String cmd) {
        final CountDownLatch syncCountDownLatch = new CountDownLatch(1);
        final StringBuilder ret = new StringBuilder();
        executeCmd(cmd, new AsyncResponseCallback() {
            @Override
            public void asyncResponse(String result) {
                if (result != null) {
                    ret.append(result);
                }
                syncCountDownLatch.countDown();
            }
        });
        try {
            syncCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return ret.toString();
    }

    public void executeCmd(String cmd, AsyncResponseCallback callback) {
        try {
             client.getMemcachedConnector().runCommand(cmd, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
