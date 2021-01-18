package com.xupt.yzh.rpc.framework.common;

import java.io.Serializable;

/**
 * 封装了消费者要调用方法的具体信息
 * 注：只有实现了序列化接口，才能实现远程传输
 */
public class RpcRequest implements Serializable {

    private String className;  // 类
    private String methodName;  // 方法
    private Object[] parameters;  // 参数

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

}
