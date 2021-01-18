package com.xupt.yzh.rpc.framework.config;

import com.xupt.yzh.rpc.framework.provider.MyRpcServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.xupt.yzh")
public class ProviderConfig {
    @Bean(name = "myRpcServer")
    public MyRpcServer myRpcServer() {
        return new MyRpcServer(8080);
    }


}
