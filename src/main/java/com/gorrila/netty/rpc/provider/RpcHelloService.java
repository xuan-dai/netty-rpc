package com.gorrila.netty.rpc.provider;

import com.gorrila.netty.rpc.api.IRpcHelloService;

/**
 * @author xuan
 * @date 2020/09/04
 **/
public class RpcHelloService implements IRpcHelloService {
    public String hello(String name) {
        return String.format("Say hello to %s", name);
    }
}
