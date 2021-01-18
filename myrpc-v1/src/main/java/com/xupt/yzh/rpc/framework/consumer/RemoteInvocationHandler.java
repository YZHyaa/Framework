package com.xupt.yzh.rpc.framework.consumer;


import com.xupt.yzh.rpc.framework.common.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 具体的代理逻辑
 */
public class RemoteInvocationHandler implements InvocationHandler {

    private String host;
    private int port;

    public RemoteInvocationHandler(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    // 所有请求都会进入这里
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 构建调用Provider的请求参数
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setClassName(method.getDeclaringClass().getName());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParameters(args);

        // 进行远程调用，并返回执行结果
        RpcNetTransport netTransport = new RpcNetTransport(host, port);
        Object res = netTransport.send(rpcRequest);

        return res;
    }
}
