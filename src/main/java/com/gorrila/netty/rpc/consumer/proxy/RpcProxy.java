package com.gorrila.netty.rpc.consumer.proxy;

import com.gorrila.netty.rpc.protocol.InvokerProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author xuan
 * @date 2020/09/05
 **/
public class RpcProxy {

    public static <T> T create(Class<?> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                clazz.isInterface() ? new Class[]{clazz} : clazz.getInterfaces(),
                new MethodProxy(clazz)
        );
    }

    private static class MethodProxy implements InvocationHandler {

        private Class<?> clazz;

        public MethodProxy(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            } else {
                return rpcInvoker(proxy, method, args);
            }
        }

        private Object rpcInvoker(Object proxy, Method method, Object[] args) {
            InvokerProtocol msg = new InvokerProtocol();
            msg.setClassName(this.clazz.getName());
            msg.setMethodName(method.getName());
            msg.setParams(method.getParameterTypes());
            msg.setValues(args);

            final RpcProxyHandler consumerHandler = new RpcProxyHandler();
            // EventLoopGroup 内部维护了线程数默认为 核心数 * 2 的线程池
            // 同时引用 provider 对象创建 selector
            NioEventLoopGroup group = new NioEventLoopGroup();

            try {
                // Bootstrap 便利工厂类，用于来完成客户端 netty 的初始化
                ChannelFuture future = new Bootstrap().group(group)
                        // channel 相当于对 socket 的抽象，netty 为支持的每种协议及对应的网络模型配置不同的 channel
                        // NioSocketChannel 表示异步非阻塞的客户端 TCP Socket
                        // NioServerSocketChannel 表示异步非阻塞的服务端 TCP Socket
                        // 使用 ChannelFactory#newChannel 实例化指定类型的 channel
                        .channel(NioSocketChannel.class)
                        // 关闭 Nagle 算法，是能够发送较小的报文，使传输延时最小化
                        .option(ChannelOption.TCP_NODELAY, true)
                        // 设置处理数据的 handler
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {

                                // 自定义协议解码器
                                ChannelPipeline pipeline = socketChannel.pipeline();
                                // 框架的最大长度，长度属性的偏移量，长度字段的长度，要添加到长度属性的补偿值，从解码帧中去除的第一个字节数
                                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                                // 自定义协议解码器
                                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                                // 对象参数类型解码器
                                pipeline.addLast("encoder", new ObjectEncoder());
                                // 对象参数类型解码器
                                pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                                pipeline.addLast("handler", consumerHandler);
                            }
                        })
                        // 指定连接服务端的地址和端口
                        // 关联 channel 和 EventLoop
                        // 将 channel 注册到 selector
                        // 将 java nio 底层的 SocketChannel 注册到指定的 Selector
                        .connect("localhost", 18080)
                        .sync();
                future.channel().writeAndFlush(msg).sync();
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 关闭资源，释放线程资源
                group.shutdownGracefully();
            }

            return consumerHandler.getResponse();
        }
    }
}
