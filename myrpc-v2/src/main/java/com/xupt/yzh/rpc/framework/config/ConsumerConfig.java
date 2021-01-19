package com.xupt.yzh.rpc.framework.config;

import com.xupt.yzh.rpc.framework.consumer.RpcProxyClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsumerConfig {

    @Bean(name = "rpcProxyClient")
    public RpcProxyClient proxyClient() {
        return new RpcProxyClient();
    }
}
