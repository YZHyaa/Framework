package com.yzh.tomcat.http;

import java.io.IOException;

/*
    Servlet就是一个应用于web的对象，具有规范作用
    注：1.servlet对象一般是单例的
        2.具体处理逻辑doGet/doPost是模板方法模式
 */
public abstract class MYServlet {

    // 注：这里的request与response都是Tomcat对象创建好然后传进来的
    public void service(MYRequest request, MYResponse response) throws IOException {
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            doGet(request, response);
        } else{
            doPost(request, response);
        }
    }

    // 这里是模板方法模式，交给子类去具体实现
    protected abstract void doPost(MYRequest request, MYResponse response) throws IOException;
    protected abstract void doGet(MYRequest request, MYResponse response) throws IOException;
}
