package com.xupt.yzh.provider;

import com.xupt.yzh.rpc.framework.provider.RpcServer;

public class Provider {

    public static void main(String[] args) throws InterruptedException {

        RpcServer proxyServer = new RpcServer();
        proxyServer.publisher(8080);
    }
}
