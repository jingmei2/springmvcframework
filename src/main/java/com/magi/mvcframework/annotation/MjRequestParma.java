package com.magi.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * 自定义 MjRequestParma 注解
 */
@Target({ElementType.PARAMETER})//作用域范围
@Retention(RetentionPolicy.RUNTIME)//生命周期
@Documented
public @interface MjRequestParma {

    String value() default "";
}
