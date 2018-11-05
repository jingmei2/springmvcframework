package com.magi.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * 自定义 RequestMapping 注解
 */
@Target({ElementType.TYPE,ElementType.METHOD})//作用域范围
@Retention(RetentionPolicy.RUNTIME)//生命周期
@Documented
public @interface MjRequestMapping {

    String value() default "";
}
