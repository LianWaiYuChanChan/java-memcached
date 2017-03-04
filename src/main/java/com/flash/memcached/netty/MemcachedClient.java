package com.flash.memcached.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by sherman on 2017/3/4.
 */
public class MemcachedClient {
    private String host;
    private int port;
    private MemcachedConnector connector;

    public MemcachedClient(String host, int port) {
        this.host = host;
        this.port = port;
        init();
    }

    public void init() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        connector = new MemcachedConnector(host, port, countDownLatch);
        new Thread(connector).start();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public MemcachedConnector getMemcachedConnector() {
        return connector;
    }

    public class MemcachedConnector implements Runnable {
        private String host;
        private int port;
        private StringBuilder syncResponse = new StringBuilder();
        private SendCommandHandler sendCommandHandler = new SendCommandHandler(syncResponse);
        private EventLoopGroup connectionWorkerGroup;
        private ChannelFuture connectionFuture;
        private CountDownLatch connectCountDownLatch;

        public MemcachedConnector(String host, int port) {
            this(host, port, null);
        }

        public MemcachedConnector(String host, int port, CountDownLatch connectCountDownLatch) {
            this.host = host;
            this.port = port;
            this.connectCountDownLatch = connectCountDownLatch;
        }

        private void connect() {
            connectionWorkerGroup = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(connectionWorkerGroup);
                b.channel(NioSocketChannel.class);
                b.option(ChannelOption.SO_KEEPALIVE, true);
                b.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("SendCommandHandler", sendCommandHandler);
                    }
                });
                connectionFuture = b.connect(host, port).sync();
                if (connectCountDownLatch != null) {
                    connectCountDownLatch.countDown();
                }
                connectionFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }

        }

        @Override
        public void run() {
            connect();
        }

        public String runCommand(String command) throws Exception {
            if (isConnected()) {
                sendCommandHandler.sendCommand(command);
                return syncResponse.toString();
            } else {
                throw new IOException("Not connected yet!");
            }
        }

        public void runCommand(String command, AsyncResponseCallback callback) throws Exception {
            if (isConnected()) {
                sendCommandHandler.sendCommand(command, callback);
            } else {
                throw new IOException("Not connected yet!");
            }
        }

        public boolean disconnect() {
            if (connectionWorkerGroup != null) {
                connectionWorkerGroup.shutdownGracefully();
            }
            return true;
        }

        public boolean isConnected() {
           return connectionFuture != null && connectionFuture.channel().isActive();
        }
    }

    private class SendCommandHandler extends ChannelHandlerAdapter {
        private ChannelHandlerContext ctx;
        // for sync syncResponse
        private StringBuilder syncResponse;
        private CountDownLatch syncResponseDownLatch;
        private AsyncResponseCallback asyncResponseCallback;
        private CountDownLatch asyncResponseDownLatch;

        SendCommandHandler(StringBuilder syncResponse) {
            this.syncResponse = syncResponse;
        }

        public void sendCommand(String command) throws InterruptedException {
            syncResponseDownLatch = new CountDownLatch(1);
            syncResponse.delete(0, syncResponse.length());
            ctx.writeAndFlush(Unpooled.wrappedBuffer(command.getBytes(CharsetUtil.UTF_8))).sync();
            syncResponseDownLatch.await();
            syncResponseDownLatch = null;
        }

        public void sendCommand(String command, AsyncResponseCallback asyncResponseCallback) throws InterruptedException {
            if (asyncResponseDownLatch != null && asyncResponseCallback != null) {
                asyncResponseDownLatch.await();
                asyncResponseDownLatch = null;
            }

            this.asyncResponseCallback = asyncResponseCallback;
            if (asyncResponseCallback != null) {
                asyncResponseDownLatch = new CountDownLatch(1);
            }

            ctx.writeAndFlush(Unpooled.wrappedBuffer(command.getBytes(CharsetUtil.UTF_8))).sync();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf byteBuf = (ByteBuf)msg;
            String response = byteBuf.toString(CharsetUtil.UTF_8);
            if (syncResponseDownLatch != null) {
                syncResponse.append(response);
                syncResponseDownLatch.countDown();
            }
            if (asyncResponseCallback != null) {
                asyncResponseCallback.asyncResponse(response);
                asyncResponseCallback = null;
                asyncResponseDownLatch.countDown();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
         }
    }
}
