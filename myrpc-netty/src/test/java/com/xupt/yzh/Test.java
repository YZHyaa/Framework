package com.xupt.yzh;

import java.net.URL;

public class Test {

    public void test() {
        URL url = this.getClass().getClassLoader().getResource("com.xupt.yzh".replaceAll("\\.", "/"));
        String file = url.getFile();
        System.out.println(this.getClass().getName());
        System.out.println(file);
    }

    public static void main(String[] args) {
        new Test().test();
    }
}
