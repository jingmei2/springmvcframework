package com.magi.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * 自定义 controller 注解
 */
@Target(ElementType.TYPE)//作用域范围
@Retention(RetentionPolicy.RUNTIME)//生命周期
@Documented
public @interface MjController {

    String value() default "";
}
