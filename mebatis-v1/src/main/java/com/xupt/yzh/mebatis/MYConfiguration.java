package com.xupt.yzh.mebatis;

import java.lang.reflect.Proxy;
import java.util.ResourceBundle;

/**
 * 1.对于配置文件的映射
 * 2.创建MapperProxy
 */
public class MYConfiguration {

    // 拿到具体的Mapper文件
    public static final ResourceBundle sqlMappings;

    static {
        sqlMappings = ResourceBundle.getBundle("mesql");
    }

    /**
     * 返回接口的代理类对象
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getMapper(Class clazz, MYSqlSession sqlSession) {
        return (T)Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{clazz},
                new MYMapperProxy(sqlSession));
    }
}
