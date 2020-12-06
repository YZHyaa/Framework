package com.xupt.yzh.framework.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MYRequestParam {
    String value() default "";

    boolean required() default true;
}
