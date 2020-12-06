package com.xupt.yzh.framework.aop;

import com.xupt.yzh.framework.aop.intercept.MYMethodInvocation;
import com.xupt.yzh.framework.aop.support.MYAdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class MYJdkDynamicAopProxy implements MYAopProxy, InvocationHandler {

    private MYAdvisedSupport advised;

    public MYJdkDynamicAopProxy(MYAdvisedSupport config) {
        this.advised = config;
    }

    // 通过该方法对外返回Proxy
    @Override
    public Object getProxy() {
        // 调用传入ClassLoader的方法，创建Proxy
        return getProxy(this.advised.getTargetClass().getClassLoader());
    }

    // 通过jdk动态代理具体生成代理对象
    @Override
    public Object getProxy(ClassLoader classLoader) {
        // newProxyInstance方法会根据被代理对象ClassLoader，class对象，及增强逻辑（InvocationHanlder.invoke）生成一个代理对象
        // 注：动态代理只能代理有接口对象，本质原因是单继承
        return Proxy.newProxyInstance(classLoader, this.advised.getTargetClass().getInterfaces(), this);
    }

    @Override
    // 当 doDispatch 将请求分发给当前方法后执行
    // 注： 1.这个方法是在调用后执行，不是在初始化时执行，初始化时只是通过AdvisedSupport创建了拦截器链
    //      2..对外是invoke原对象的方法，但实际上执行invoke代理对象的方法
    //      3.这里传入要执行的method，可以对进行判断进行选择性增强
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 在AdvisedSupport中获取方法的拦截器链
        // 其中符合切点表达式的方法会有拦截器链，而不是切点的方法就只有自己
        List<Object> interceptorsAndDynamicMethodMatchers = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, this.advised.getTargetClass());
        // 获取拦截器链执行器
        MYMethodInvocation methodInvocation = new MYMethodInvocation(proxy, this.advised.getTarget(), method, args, this.advised.getTargetClass(), interceptorsAndDynamicMethodMatchers);
        // 执行拦截器链及invoke当前方法
        return methodInvocation.proceed();
    }
}
