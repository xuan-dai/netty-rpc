package com.gorrila.netty.rpc.register;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * @author xuan
 * @date 2020/09/04
 **/
public class RpcRegister {
    private int port;

    public RpcRegister(int port) {
        this.port = port;
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // Bootstrap 便利工厂类，用于来完成服务端 netty 的初始化
            ChannelFuture future = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    // NioServerSocketChannel 表示异步非阻塞的服务端 TCP Socket
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel serverChannel) throws Exception {
                            // 自定义协议解码器
                            ChannelPipeline pipeline = serverChannel.pipeline();
                            // 框架的最大长度，长度属性的偏移量，长度字段的长度，要添加到长度属性的补偿值，从解码帧中去除的第一个字节数
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            // 自定义协议解码器
                            pipeline.addLast(new LengthFieldPrepender(4));
                            // 对象参数类型解码器
                            pipeline.addLast("encoder", new ObjectEncoder());
                            // 对象参数类型解码器
                            pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast(new RegisterHandler());
                        }
                    })
                    // ChannelOption.SO_BACKLOG 表示服务端接受连接的队列长度，如果队列长度已满，客户端连接将会被拒绝
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 连接保活，tcp 会主动探测空闲连接的有效性，时间间隔为 2h
                    .childOption(ChannelOption.SO_KEEPALIVE, true).bind(port).sync();
            System.out.println("GP RPC Register start listen at " + port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new RpcRegister(18080).start();
    }
}
