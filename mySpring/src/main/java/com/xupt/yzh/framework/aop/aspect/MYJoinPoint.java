package com.xupt.yzh.framework.aop.aspect;

import java.lang.reflect.Method;

public interface MYJoinPoint {

    Object getThis();

    Object[] getArguments();

    Method getMethod();

    void setUserAttribute(String key, Object value);

    Object getUserAttribute(String key);
}
