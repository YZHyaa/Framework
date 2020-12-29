package com.xupt.yzh.tomcat.servlet;

import com.xupt.yzh.tomcat.http.MYRequest;
import com.xupt.yzh.tomcat.http.MYResponse;
import com.xupt.yzh.tomcat.http.MYServlet;

import java.io.IOException;

public class FirstServlet extends MYServlet {
    @Override
    protected void doPost(MYRequest request, MYResponse response) throws Exception {
        response.write("this is FirstServlet!");
    }

    @Override
    protected void doGet(MYRequest request, MYResponse response) throws Exception {
        doPost(request, response);
    }
}
