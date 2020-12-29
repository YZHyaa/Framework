package com.xupt.yzh.tomcat.servlet;

import com.xupt.yzh.tomcat.http.MYRequest;
import com.xupt.yzh.tomcat.http.MYResponse;
import com.xupt.yzh.tomcat.http.MYServlet;

public class SecondServlet extends MYServlet {

    @Override
    protected void doPost(MYRequest request, MYResponse response) throws Exception {
        response.write("this is SecondServlet...");
    }

    @Override
    protected void doGet(MYRequest request, MYResponse response) throws Exception {
        doPost(request, response);
    }
}
