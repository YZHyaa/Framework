package com.xupt.yzh.framework.beans.config;

/**
 * 用来保存Bean的信息 : 实际类信息 + 配置信息（包括了class，factory，method，lazyInit，等（在本类中就只取了其中四个）
 * 对应关系：一个Class对应多个BeanDefinition，一个BeanDefinition对应一个Bean，一个Bean又对应三种获取方式
 * 什么时候使用BeanDefinitoin：在通过getBean获取Bean实例时，首先要拿到这里实例的类信息（BeanDefinition）
 */
public class MYBeanDefinition {
    // 全类名
    // 为了后面通过反射拿到Class对象，然后创建实例和注解判断
    private String beanClassName;

    // factoryBeanName：即每个类的对象应该用什么具体工厂bean创建 --> 一个Bean对应一个工厂
    // 这里是用来保存beanName的，同时作为bean的唯一标识！！！
    private String factoryBeanName;

    // 默认
    private boolean isLazyInit = false;
    private boolean isSigleton = true;



    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public boolean isLazyInit() {
        return isLazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        isLazyInit = lazyInit;
    }

    public boolean isSigleton() {
        return isSigleton;
    }

    public void setSigleton(boolean sigleton) {
        isSigleton = sigleton;
    }


}
