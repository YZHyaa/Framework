package com.xupt.yzh.provider;

import com.xupt.yzh.rpc.framework.annotation.RpcService;
import com.xupt.yzh.api.IHelloService;
import com.xupt.yzh.api.User;

@RpcService(IHelloService.class) // 会被实例化出IHelloService的Bean，放入IOC容器
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
