package com.xupt.yzh.provider;

import com.xupt.yzh.api.IHelloService;
import com.xupt.yzh.api.User;

public class HelloServiceImpl implements IHelloService {
    @Override
    public String sayHello(String content) {
        System.out.println("Request in sayHello:" + content);
        return "say Hello: " + content;
    }

    @Override
    public String saveUser(User user) {
        System.out.println("Request in saveUser:" + user);
        return "SUCCESS";  
    }
}
