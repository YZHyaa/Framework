package com.xupt.yzh.consumer;

import com.xupt.yzh.api.TestService;
import com.xupt.yzh.rpc.framework.consumer.RpcProxyClient;

public class Consumer {

    public static void main(String[] args) {

        // 创建代理对象
        RpcProxyClient rpcProxyClient = new RpcProxyClient();

        // 创建一个代理对象
        TestService service = rpcProxyClient.clientProxy(TestService.class, "localhost", 8080);
        // test() 会进行远程调用，阻塞式...
        String json = service.test("老五");
        System.out.println(json);
    }
}
