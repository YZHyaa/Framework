package com.xupt.yzh.framework.beans.support;

import com.xupt.yzh.framework.beans.config.MYBeanDefinition;
import com.xupt.yzh.framework.context.support.MYAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IOC容器的默认实现（因此继承了AbstractApplicationContext），是扩展其余IOC容器的基础
 */
public class MYDefaultListableBeanFactory extends MYAbstractApplicationContext {
    // 伪IOC容器，保存了BeanDefinition（类信息）
    // 这里的key是factoryBeanName，即beanName（对于一种Bean而言factoryName是唯一的）
    protected final Map<String, MYBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, MYBeanDefinition>();
}
