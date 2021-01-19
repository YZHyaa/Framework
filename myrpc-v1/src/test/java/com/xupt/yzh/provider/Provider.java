package com.xupt.yzh.provider;

import com.xupt.yzh.rpc.framework.provider.RpcServerProxyServer;

public class Provider {

    public static void main(String[] args) {

        RpcServerProxyServer proxyServer = new RpcServerProxyServer();
        // 创建一个服务实例
        proxyServer.publisher(new TestServiceImpl(), 8080);
    }
}
