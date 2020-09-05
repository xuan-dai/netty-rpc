package com.gorrila.netty.rpc.consumer;

import com.gorrila.netty.rpc.api.IRpcHelloService;
import com.gorrila.netty.rpc.api.IRpcService;
import com.gorrila.netty.rpc.consumer.proxy.RpcProxy;

/**
 * @author xuan
 * @date 2020/09/05
 **/
public class RpcConsumer {
    public static void main(String[] args) {
        IRpcHelloService rpcHelloService = RpcProxy.create(IRpcHelloService.class);
        System.out.println("rpcHelloService.hello(\"daixuan\") = " + rpcHelloService.hello("daixuan"));

        IRpcService rpcService = RpcProxy.create(IRpcService.class);
        System.out.println("rpcService.add(8, 2) = " + rpcService.add(8, 2));
        System.out.println("rpcService.sub(8, 2) = " + rpcService.sub(8, 2));
        System.out.println("rpcService.mult(8, 2) = " + rpcService.mult(8, 2));
        System.out.println("rpcService.div(8, 2) = " + rpcService.div(8, 2));
    }
}
