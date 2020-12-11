package com.xupt.starter.autoConfiguration;

import com.xupt.starter.HelloFormatTemplate;
import com.xupt.starter.format.FormatProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FormatAutoConfiguration.class) // 将具体FormatProcessor的Bean扫描进来
//@ComponentScan
@EnableConfigurationProperties(HelloProperties.class) // 将具体属性Bean（HelloProperties）扫描进来?
public class HelloAutoConfiguration {                 // 注：这不是要加入IOC的Bean，所以不能用Componet替换

    @Bean
    public HelloFormatTemplate helloFormatTemplate(FormatProcessor formatProcessor, HelloProperties helloProperties) {
        // 由于import了Format这Bean的配置类，spring就有据可依的能找到相应bean去注入
        // 这里还会根据具体Condition判断注入哪个bean
        return new HelloFormatTemplate(formatProcessor, helloProperties);
    }
}
