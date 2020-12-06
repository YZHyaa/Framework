package com.xupt.yzh.framework.aop.aspect;

import com.xupt.yzh.framework.aop.intercept.MYMethodInterceptor;
import com.xupt.yzh.framework.aop.intercept.MYMethodInvocation;

import java.lang.reflect.Method;

/**
 * 拦截器链组件
 */
public class MYMethodBeforeAdviceInterceptor extends MYAbstractAspectAdvice implements MYAdvice, MYMethodInterceptor {

    private MYJoinPoint joinPoint;

    public MYMethodBeforeAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    private void before(Method method,Object[] args,Object target) throws Throwable{
        //传送了给织入参数
        //method.invoke(target);
        super.invokeAdviceMethod(this.joinPoint,null,null);
    }

    @Override
    public Object invoke(MYMethodInvocation mi) throws Throwable {
        //从被织入的代码中才能拿到，JoinPoint
        this.joinPoint = mi;
        before(mi.getMethod(), mi.getArguments(), mi.getThis());
        return mi.proceed();
    }
}
