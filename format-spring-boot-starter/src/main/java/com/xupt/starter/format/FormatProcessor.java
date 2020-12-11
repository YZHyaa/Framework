package com.xupt.starter.format;

public interface FormatProcessor {

    // 定义一个格式化方法
    <T>String format(T obj);
}
