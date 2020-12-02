package com.yzh.tomcat.servlet;

import com.yzh.tomcat.http.MYRequest;
import com.yzh.tomcat.http.MYResponse;
import com.yzh.tomcat.http.MYServlet;

import java.io.IOException;

public class FirstServlet extends MYServlet {
    @Override
    protected void doPost(MYRequest request, MYResponse response) throws IOException {
        response.write("this is FirstServlet!");
    }

    @Override
    protected void doGet(MYRequest request, MYResponse response) throws IOException {
        doPost(request, response);
    }
}
