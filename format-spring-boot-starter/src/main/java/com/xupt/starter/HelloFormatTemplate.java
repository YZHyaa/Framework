package com.xupt.starter;

import com.xupt.starter.autoConfiguration.HelloProperties;
import com.xupt.starter.format.FormatProcessor;

public class HelloFormatTemplate {

    private FormatProcessor formatProcessor;

    private HelloProperties helloProperties;

    public HelloFormatTemplate(FormatProcessor formatProcessor, HelloProperties helloProperties) {
        this.formatProcessor = formatProcessor;
        this.helloProperties = helloProperties;
    }

    public <T> String doFormat(T obj) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Execute format").append("\n");
        stringBuilder.append("HelloProperties: ").append(formatProcessor.format(helloProperties.getInfo())).append("\n");
        stringBuilder.append("Obj format result: ").append(formatProcessor.format(obj)).append("\n");
        return stringBuilder.toString();
    }
}
