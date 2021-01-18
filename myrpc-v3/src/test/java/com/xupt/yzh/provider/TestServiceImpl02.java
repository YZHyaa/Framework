package com.xupt.yzh.provider;

import com.xupt.yzh.api.TestService;
import com.xupt.yzh.rpc.framework.annotation.RpcService;

import java.util.Random;

// todo（3）
/**
 * 在发布服务时加上版本号
  */
@RpcService(value = TestService.class, version = "v2.0")
public class TestServiceImpl02 implements TestService {

    @Override
    public String test(String name) {
        System.out.println("new requst coming..." + name);

        Random random = new Random();
        String json = "{\"name\":" + "\"" + name + "\"" + ", "
                + "\"age\":" + random.nextInt(40) +", "
                + "\"version\":" + "\"【v2.0】\""
                + "}";
        return json;
    }
}
