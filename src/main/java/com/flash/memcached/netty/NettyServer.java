package com.flash.memcached.netty;

/**
 * <p>
 * Creation Date: 2/26/2017 <br>
 * Creation Time: 1:11 AM <br>
 * </p>
 *
 * @author Jichao Zhang
 */

import com.flash.memcached.core.KeyValueStorageService;
import com.flash.memcached.core.KeyValueStorageServiceImpl;
import com.flash.memcached.logging.Logger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;


/**
 * 处理数据
 */
public class NettyServer {

    /**
     * 服务端处理通道.这里只是打印一下请求的内容，并不对请求进行任何的响应
     * DiscardServerHandler 继承自 ChannelHandlerAdapter，
     * 这个类实现了ChannelHandler接口，
     * ChannelHandler提供了许多事件处理的接口方法，
     * 然后你可以覆盖这些方法。
     * 现在仅仅只需要继承ChannelHandlerAdapter类而不是你自己去实现接口方法。
     */
    public class DiscardServerHandler extends ChannelHandlerAdapter {

        /***
         * 这里我们覆盖了chanelRead()事件处理方法。
         * 每当从客户端收到新的数据时，
         * 这个方法会在收到消息时被调用，
         * 这个例子中，收到的消息的类型是ByteBuf
         * @param ctx 通道处理的上下文信息
         * @param msg 接收的消息
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
                ByteBuf in = (ByteBuf) msg;
          /* while (in.isReadable()) {
                System.out.print((char) in.readByte());
                System.out.flush();
            }*/
                //这一句和上面注释的的效果都是打印输入的字符
                System.out.println(in.toString(CharsetUtil.UTF_8));
            } finally {
                /**
                 * ByteBuf是一个引用计数对象，这个对象必须显示地调用release()方法来释放。
                 * 请记住处理器的职责是释放所有传递到处理器的引用计数对象。
                 */
                ReferenceCountUtil.release(msg);
            }
        }

        /***
         * 这个方法会在发生异常时触发
         * @param ctx
         * @param cause
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            /***
             * 发生异常后，关闭连接
             */
            cause.printStackTrace();
            ctx.close();
        }

    }


    /**
     * 服务端处理通道.
     * ResponseServerHandler 继承自 ChannelHandlerAdapter，
     * 这个类实现了ChannelHandler接口，
     * ChannelHandler提供了许多事件处理的接口方法，
     * 然后你可以覆盖这些方法。
     * 现在仅仅只需要继承ChannelHandlerAdapter类而不是你自己去实现接口方法。
     * 用来对请求响应
     */
    public class ReceiveCommandHandler extends ChannelHandlerAdapter {
        private KeyValueStorageService kvStorageSrv;

        public ReceiveCommandHandler(KeyValueStorageService kvStorageSrv) {
            this.kvStorageSrv = kvStorageSrv;
        }

        /**
         * 这里我们覆盖了chanelRead()事件处理方法。
         * 每当从客户端收到新的数据时，
         * 这个方法会在收到消息时被调用，
         * ChannelHandlerContext对象提供了许多操作，
         * 使你能够触发各种各样的I/O事件和操作。
         * 这里我们调用了write(Object)方法来逐字地把接受到的消息写入
         *
         * @param ctx 通道处理的上下文信息
         * @param msg 接收的消息
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf byteBuf = (ByteBuf)msg;
            String cmd = byteBuf.toString(CharsetUtil.UTF_8);
            String ret = kvStorageSrv.executeCmd(cmd);
            ByteBuf returnByteBuff = Unpooled.wrappedBuffer(ret.getBytes(CharsetUtil.UTF_8));
            ctx.write(returnByteBuff);
            //cxt.writeAndFlush(msg)

            //请注意，这里我并不需要显式的释放，因为在定入的时候netty已经自动释放
            // ReferenceCountUtil.release(msg);
        }

        /**
         * ctx.write(Object)方法不会使消息写入到通道上，
         * 他被缓冲在了内部，你需要调用ctx.flush()方法来把缓冲区中数据强行输出。
         * 或者你可以在channelRead方法中用更简洁的cxt.writeAndFlush(msg)以达到同样的目的
         *
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        /**
         * 这个方法会在发生异常时触发
         *
         * @param ctx
         * @param cause
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            /***
             * 发生异常后，关闭连接
             */
            cause.printStackTrace();
            ctx.close();
        }

    }


    private int port;
    private KeyValueStorageService kvStorageSrv;

    private EventLoopGroup serverBossGroup;
    private EventLoopGroup connectionWorkerGroup;
    private ChannelFuture serverStartFuture;

    public NettyServer(int port, KeyValueStorageService kvStorageSrv) {
        this.port = port;
        this.kvStorageSrv = kvStorageSrv;
    }

    public void run() throws Exception {
        /***
         * NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器，
         * Netty提供了许多不同的EventLoopGroup的实现用来处理不同传输协议。
         * 在这个例子中我们实现了一个服务端的应用，
         * 因此会有2个NioEventLoopGroup会被使用。
         * 第一个经常被叫做‘boss’，用来接收进来的连接。
         * 第二个经常被叫做‘worker’，用来处理已经被接收的连接，
         * 一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上。
         * 如何知道多少个线程已经被使用，如何映射到已经创建的Channels上都需要依赖于EventLoopGroup的实现，
         * 并且可以通过构造函数来配置他们的关系。
         */
        serverBossGroup = new NioEventLoopGroup();
        connectionWorkerGroup = new NioEventLoopGroup();
        System.out.println("Listen on: " + port);
        try {
            /**
             * ServerBootstrap 是一个启动NIO服务的辅助启动类
             * 你可以在这个服务中直接使用Channel
             */
            ServerBootstrap b = new ServerBootstrap();
            /**
             * 这一步是必须的，如果没有设置group将会报java.lang.IllegalStateException: group not set异常
             */
            b = b.group(serverBossGroup, connectionWorkerGroup);
            /***
             * ServerSocketChannel以NIO的selctor为基础进行实现的，用来接收新的连接
             * 这里告诉Channel如何获取新的连接.
             */
            b = b.channel(NioServerSocketChannel.class);
            /***
             * 这里的事件处理类经常会被用来处理一个最近的已经接收的Channel。
             * ChannelInitializer是一个特殊的处理类，
             * 他的目的是帮助使用者配置一个新的Channel。
             * 也许你想通过增加一些处理类比如NettyServerHandler来配置一个新的Channel
             * 或者其对应的ChannelPipeline来实现你的网络程序。
             * 当你的程序变的复杂时，可能你会增加更多的处理类到pipline上，
             * 然后提取这些匿名类到最顶层的类上。
             */
            b = b.childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast("LoggingHandler", new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast("ReceivedCommandHandler", new ReceiveCommandHandler(kvStorageSrv));
                    // ch.pipeline().addLast();
                }
            });
            /***
             * 你可以设置这里指定的通道实现的配置参数。
             * ﻿ServerChannel允许的最大队列长度
             * 我们正在写一个TCP/IP的服务端，
             * 因此我们被允许设置socket的参数选项比如tcpNoDelay和keepAlive。
             * 请参考ChannelOption和详细的ChannelConfig实现的接口文档以此可以对ChannelOptions的有一个大概的认识。
             */
            b = b.option(ChannelOption.SO_BACKLOG, 128);
            /***
             * option()是提供给NioServerSocketChannel用来接收进来的连接。
             * childOption()是提供给由父管道ServerChannel接收到的连接，
             * 在这个例子中也是NioServerSocketChannel。
             */
            b = b.childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind the port and accept incoming connecting.
            serverStartFuture = b.bind(port).sync();

            System.out.println("Server started on " + port);

            // Will block until socket was closed.
            serverStartFuture.channel().closeFuture().sync();
        } finally {
            shutdown();
        }
    }

    public boolean shutdown() {
        if (connectionWorkerGroup != null) {
            connectionWorkerGroup.shutdownGracefully();
        }
        if (serverBossGroup != null) {
            serverBossGroup.shutdownGracefully();
        }
        if (!isRunning()) {
            System.out.println("Server was shutdown.");
        }
        return true;
    }


    public boolean isRunning() {
        return serverStartFuture != null && serverStartFuture.channel().isOpen();
    }

    public void start()  {
        try {
            run();
        } catch (Exception e) {
            Logger.log(e.getMessage());
        }
    }

    public static NettyServer getInstance(int port) {
        KeyValueStorageService keyValueStorageService = new KeyValueStorageServiceImpl();
        return new NettyServer(port, keyValueStorageService);
    }


    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8000;
        }
        final NettyServer server = getInstance(port);
        new Thread(new Runnable() {
            @Override
            public void run() {
                server.start();
            }
        }).start();
    }
}
