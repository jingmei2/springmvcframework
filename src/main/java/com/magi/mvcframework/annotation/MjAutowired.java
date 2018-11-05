package com.magi.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * 自定义 MjAutowired 注解
 */
@Target({ElementType.FIELD})//作用域范围
@Retention(RetentionPolicy.RUNTIME)//生命周期
@Documented
public @interface MjAutowired {

    String value() default "";
}
