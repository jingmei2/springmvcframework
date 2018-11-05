package com.magi.demo.service.impl;

import com.magi.demo.service.IDemoService;

/**
 * 核心业务逻辑
 * @author magi
 */
public class DemoService implements IDemoService {
    public String get(String name) {
        long time = System.currentTimeMillis();
        return "My name is " + name + " " + time;
    }
}
