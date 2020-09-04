package com.gorrila.netty.rpc.api;

/**
 * @author xuan
 * @date 2020/09/04
 **/
public interface IRpcService {
    int add(int a, int b);

    int sub(int a, int b);

    int mult(int a, int b);

    int div(int a, int b);
}
