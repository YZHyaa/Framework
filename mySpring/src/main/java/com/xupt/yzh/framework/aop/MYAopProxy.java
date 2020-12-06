package com.xupt.yzh.framework.aop;

/**
 * 代理的最顶层接口，有两种实现（JDK,cglib）
 */
public interface MYAopProxy {

    Object getProxy();

    Object getProxy(ClassLoader classLoader);
}
