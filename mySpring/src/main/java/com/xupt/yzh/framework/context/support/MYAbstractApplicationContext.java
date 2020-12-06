package com.xupt.yzh.framework.context.support;

/**
 * IOC容器实现的顶层设计
 * 规范了IOC容器如何实现（想做成类似于模板模式）
 */
public abstract class MYAbstractApplicationContext {
    // 受保护，只提供给子类重写
    protected void refresh() throws Exception {}
}
