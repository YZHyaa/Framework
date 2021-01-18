package com.xupt.yzh.provider;

import com.xupt.yzh.api.IHelloService;
import com.xupt.yzh.rpc.framework.provider.RpcServerProxyServer;

public class Provider {

    public static void main(String[] args) {
        IHelloService helloService = new HelloServiceImpl();

        RpcServerProxyServer proxyServer = new RpcServerProxyServer();
        proxyServer.publisher(helloService, 8080);
    }
}
