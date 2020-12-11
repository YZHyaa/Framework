package com.xupt.starter.autoConfiguration;

import com.xupt.starter.format.FormatProcessor;
import com.xupt.starter.format.JsonFormatProcessor;
import com.xupt.starter.format.StringFormatProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class FormatAutoConfiguration {

    @Bean
    @Primary // FormatProcessor有多个实现类时，要具体指定默认使用哪个
    @ConditionalOnMissingClass("com.alibaba.fastjson.JSON") // 当没有fastjson时注入StringFormat
    public FormatProcessor stringFormat() {                 // 注：这里是指定具体类，而一个jar包中会包含多个类
        return new StringFormatProcessor();                 //     比如我们判断如果有redis时，是拿使用redis的核心类
    }

    @Bean
    @ConditionalOnClass(name = "com.alibaba.fastjson.JSON") // 当存在fastjson时注入JsonFormat
    public FormatProcessor jsonFormat() {
        return new JsonFormatProcessor();
    }
}
