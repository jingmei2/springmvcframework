package com.magi.demo.service.impl;

import com.magi.demo.service.IDemoService;
import com.magi.mvcframework.annotation.MjService;

/**
 * 核心业务逻辑
 * @author magi
 */
@MjService
public class DemoService implements IDemoService {
    @Override
    public String get(String name) {
        long time = System.currentTimeMillis();
        return "My name is " + name + " " + time;
    }
}
