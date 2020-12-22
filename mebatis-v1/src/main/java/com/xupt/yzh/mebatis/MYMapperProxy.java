package com.xupt.yzh.mebatis;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 代理对象：简化了硬编码，相当于在SqlSession调用Excutor前面又加了一层
 * 1.对外：提供给客户端调用
 *          优化了硬编码（约定大于配置）：statementId = 接口类型（namespace） + 方法名
 * 2.对内：调用SqlSession提供的方法 ---statementId----> Excutor执行
 */
public class MYMapperProxy implements InvocationHandler {

    private MYSqlSession sqlSession;

    public MYMapperProxy(MYSqlSession sqlSession) {
        this.sqlSession =  sqlSession;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String mapperInterface = method.getDeclaringClass().getName();
        String methodName = method.getName();
        String statementId = mapperInterface + "." + methodName;
        return this.sqlSession.selectOne(statementId, args[0]);
    }
}
