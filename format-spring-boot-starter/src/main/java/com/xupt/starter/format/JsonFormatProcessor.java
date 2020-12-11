package com.xupt.starter.format;

import com.alibaba.fastjson.JSON;

public class JsonFormatProcessor implements FormatProcessor {
    @Override
    public <T> String format(T obj) {
        return "JsonFormatProcessor:" + JSON.toJSONString(obj);
    }
}
