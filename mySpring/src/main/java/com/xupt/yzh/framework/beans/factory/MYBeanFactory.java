package com.xupt.yzh.framework.beans.factory;

/**
 * IOC容器是单例模式
 * BeanFactory是顶层设计，相当于规范了IOC容器的功能
 */
public interface MYBeanFactory {

    Object getBean(String beanName) throws Exception;

    Object getBean(Class<?> beanClass) throws Exception;
}
