package com.xupt.yzh.consumer;

import com.xupt.yzh.rpc.framework.config.ConsumerConfig;
import com.xupt.yzh.rpc.framework.consumer.RpcProxyClient;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import com.xupt.yzh.api.IHelloService;

public class Consumer {

    public static void main(String[] args) {
        /**
         * 与v1相比，区别是通过IOC容器获取代理Bean
         */
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ConsumerConfig.class);
        RpcProxyClient rpcProxyClient = context.getBean(RpcProxyClient.class);

        IHelloService helloService = rpcProxyClient.clientProxy(IHelloService.class, "localhost", 8080);
        String res = helloService.sayHello("zhangsan");
        System.out.println(res);
    }
}
