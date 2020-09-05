package com.gorrila.netty.rpc.register;

import com.gorrila.netty.rpc.protocol.InvokerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xuan
 * @date 2020/09/04
 **/
public class RegisterHandler extends ChannelInboundHandlerAdapter {
    // 存放类接口名和类对象实例
    public static ConcurrentHashMap<String, Object> registryMap = new ConcurrentHashMap<String, Object>();

    private List<String> classNames = new ArrayList<String>();

    public RegisterHandler() {
        if (registryMap.isEmpty()) {
            scannerClass("com.gorrila.netty.rpc.provider");
            doRegister();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        InvokerProtocol request = (InvokerProtocol) msg;
        String className = request.getClassName();
        Object clazz = registryMap.get(className);
        Object result = clazz.getClass()
                .getMethod(request.getMethodName(), request.getParams())
                .invoke(clazz, request.getValues());
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

    private void scannerClass(String packageName) {
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                classNames.add(packageName + "." + file.getName().replace(".class", "").trim());
            } else {
                scannerClass(packageName + "." + file.getName());
            }

        }
    }

    private void doRegister() {
        if (classNames.size() == 0) {
            System.out.println("class names is empty");
            return;
        }
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                Class<?> i = clazz.getInterfaces()[0];
                // 请求方是通过接口来访问
                registryMap.put(i.getName(), clazz.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (String key : registryMap.keySet()) {
            System.out.println("registryMap has class for name: " + key);
        }
    }

}
