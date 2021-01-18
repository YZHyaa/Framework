package com.xupt.yzh.rpc.framework.provider;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 代理对象，封装了具体对Provider的调用逻辑
 */
public class RpcServerProxyServer {
    // 用线程池实现给所有连接都分配一个线程
    ExecutorService executorServic = Executors.newCachedThreadPool();

    // 发布服务
    public void publisher(Object service, int port) {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                // 每一个socket，交给一个processorHandler去处理
                executorServic.execute(new ProcessorHandler(socket, service));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
