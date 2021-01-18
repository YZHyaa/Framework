package com.xupt.yzh.rpc.framework.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 通过注解标识要发布的服务，相比上一个版本，有了可扩展性
 */
@Target(ElementType.TYPE) // 用在类/接口
@Retention(RetentionPolicy.RUNTIME) // 运行时
@Component // 有该注解的类，会被Spring实例化，然后放入IOC容器
public @interface RpcService {

    // 记录要发布服务的接口
    Class<?> value();

    // todo（2）
    /**
     * 版本号
     */
    String version() default "v1.0";
}
