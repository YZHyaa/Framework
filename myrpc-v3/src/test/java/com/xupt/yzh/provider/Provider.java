package com.xupt.yzh.provider;

import com.xupt.yzh.rpc.framework.config.ProviderConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Provider {

    public static void main(String[] args) {

        new AnnotationConfigApplicationContext(ProviderConfig.class).start();
    }
}
