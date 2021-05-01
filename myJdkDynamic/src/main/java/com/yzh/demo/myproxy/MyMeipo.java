package com.yzh.demo.myproxy;

import com.yzh.myproxy.MyClassLoader;
import com.yzh.myproxy.MyInvocationHandler;
import com.yzh.myproxy.MyProxy;

import java.lang.reflect.Method;

// 实现 MyInvocationHandler 接口
public class MyMeipo implements MyInvocationHandler {
	
    // 被代理对象（依赖注入）
    private Object target;
    public Object getInstance(Object target) throws Exception{
        this.target = target;
        Class<?> clazz = target.getClass();
        // 核心！
        return MyProxy.newProxyInstance(new MyClassLoader(),clazz.getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        before();
        // 调用被代理对象的当前方法
        Object obj = method.invoke(this.target,args);
        after();
       return obj;
    }

    private void before(){
        System.out.println("我是媒婆，负责找对象，现在已经确认需求");
        System.out.println("开始物色");
    }

    private void after(){
        System.out.println("OK的话，准备办事");
    }
}