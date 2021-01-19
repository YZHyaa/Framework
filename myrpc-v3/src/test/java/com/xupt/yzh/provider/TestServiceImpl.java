package com.xupt.yzh.provider;

import com.xupt.yzh.api.TestService;
import com.xupt.yzh.rpc.framework.annotation.RpcService;

import java.util.Random;

/**
 * 默认版本号是 v1.0
 */
@RpcService(TestService.class)
public class TestServiceImpl implements TestService {

    @Override
    public String test(String name) {
        System.out.println("new requst coming..." + name);

        Random random = new Random();
        String json = "{\"name\":" + "\"" + name + "\"" + ", "
                       + "\"age\":" + random.nextInt(40) +", "
                       + "\"version\":" + "\"【v1.0】\""
                       + "}";
        return json;
    }
}
