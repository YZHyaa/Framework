package com.xupt.yzh.rpc.framework.consumer;

import java.lang.reflect.Proxy;

public class RpcProxyClient {

    // 创建代理对象，代理的就是指定服务
    // 注：因为 Provider 发布时就是一个端口一个服务，所以这里代理的唯一标识就是 host:port
    public <T>T clientProxy(final Class<T> interfaceCls, final String host, final int port) {
        return (T)Proxy.newProxyInstance(interfaceCls.getClassLoader(), new Class[]{interfaceCls},
                new RemoteInvocationHandler(host, port));
    }

}
