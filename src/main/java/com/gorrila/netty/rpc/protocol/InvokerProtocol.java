package com.gorrila.netty.rpc.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xuan
 * @date 2020/09/04
 **/
@Data
public class InvokerProtocol implements Serializable {
    private String className;
    private String methodName;
    private Class<?>[] params;
    private Object[] values;
}
