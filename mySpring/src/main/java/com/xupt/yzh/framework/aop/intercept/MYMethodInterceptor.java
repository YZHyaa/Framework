package com.xupt.yzh.framework.aop.intercept;

// 拦截器链的组件的统一接口
public interface MYMethodInterceptor {

    Object invoke(MYMethodInvocation invocation) throws Throwable;
}
