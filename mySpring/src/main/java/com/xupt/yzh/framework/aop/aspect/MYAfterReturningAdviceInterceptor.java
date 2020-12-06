package com.xupt.yzh.framework.aop.aspect;

import com.xupt.yzh.framework.aop.intercept.MYMethodInterceptor;
import com.xupt.yzh.framework.aop.intercept.MYMethodInvocation;

import java.lang.reflect.Method;

/**
 * 拦截器链组件
 */
public class MYAfterReturningAdviceInterceptor extends MYAbstractAspectAdvice implements MYMethodInterceptor {

    private MYJoinPoint joinPoint;

    public MYAfterReturningAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(MYMethodInvocation mi) throws Throwable {
        Object retVal = mi.proceed();
        this.joinPoint = mi;
        this.afterReturning(retVal,mi.getMethod(),mi.getArguments(),mi.getThis());
        return retVal;
    }

    private void afterReturning(Object retVal, Method method, Object[] arguments, Object aThis) throws Throwable {
        super.invokeAdviceMethod(this.joinPoint,retVal,null);
    }
}
