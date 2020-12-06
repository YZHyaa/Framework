package com.xupt.yzh;

public class Test01 {

    public static void main(String[] args) throws NoSuchFieldException {
        Class<?> a = A.class.getField("b").getType();
        System.out.println(a.getName());
    }
}
