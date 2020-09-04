package com.gorrila.netty.rpc.provider;

import com.gorrila.netty.rpc.api.IRpcService;

/**
 * @author xuan
 * @date 2020/09/04
 **/
public class RpcService implements IRpcService {
    public int add(int a, int b) {
        return a + b;
    }

    public int sub(int a, int b) {
        return a - b;
    }

    public int mult(int a, int b) {
        return a * b;
    }

    public int div(int a, int b) {
        return a / b;
    }
}
