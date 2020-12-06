package com.xupt.yzh.framework.aop.aspect;

import com.xupt.yzh.framework.aop.intercept.MYMethodInterceptor;
import com.xupt.yzh.framework.aop.intercept.MYMethodInvocation;

import java.lang.reflect.Method;

/**
 * 拦截器链组件
 */
public class MYAfterThrowingAdviceInterceptor extends  MYAbstractAspectAdvice implements MYMethodInterceptor {

    private String throwingName;

    public MYAfterThrowingAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    public void setThrowName(String throwingName) {
        this.throwingName = throwingName;
    }

    @Override
    public Object invoke(MYMethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        }catch (Throwable e){
            invokeAdviceMethod(mi,null,e.getCause());
            throw e;
        }
    }
}
