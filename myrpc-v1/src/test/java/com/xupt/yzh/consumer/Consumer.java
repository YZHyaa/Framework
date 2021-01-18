package com.xupt.yzh.consumer;

import com.xupt.yzh.api.IHelloService;
import com.xupt.yzh.rpc.framework.consumer.RpcProxyClient;

public class Consumer {

    public static void main(String[] args) {
        // 创建代理对象
        RpcProxyClient rpcProxyClient = new RpcProxyClient();
        IHelloService helloService = rpcProxyClient.clientProxy(IHelloService.class, "localhost", 8080);
        // 通过代理对象进行远程调用，获取执行结果
        String res = helloService.sayHello("Zhangsan");
        System.out.println(res);

    }
}
