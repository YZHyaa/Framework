package com.xupt.yzh.rpc.framework.protocol;

import java.io.Serializable;

public class RpcRequest implements Serializable {

    private String className;// 服务名
    private String methodName; // 方法名，具体的逻辑
    private Object[] parameters; // 实参列表

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
