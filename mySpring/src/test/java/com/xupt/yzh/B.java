package com.xupt.yzh;

import java.net.URL;

public class B {

    public void test() {
        URL resource = this.getClass().getClassLoader().getResource("layouts/500.html");
        String file = resource.getFile();
        System.out.println(resource);
        System.out.println(file);
    }
}
