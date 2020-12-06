package com.xupt.yzh.framework.beans;

// Spring做法是，不会把最原始的对象放出去，会用一个BeanWrapper来进行一次包装
public class MYBeanWrapper {

    private Object wrappedInstance;
    // 还要保存class，是为了多例模式服务
    private Class<?> wrappedClass;

    public MYBeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    public Object getWrappedInstance() {
        return this.wrappedInstance;
    }

    // 这里没有通过构造函数或者set方法进行注入，而是直接通过instance获取到Class
    public Class<?> getWrappedClass() {
        return this.wrappedInstance.getClass();
    }
}
