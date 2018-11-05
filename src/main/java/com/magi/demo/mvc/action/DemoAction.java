package com.magi.demo.mvc.action;

import com.magi.demo.service.IDemoService;
import com.magi.mvcframework.annotation.MjAutowired;
import com.magi.mvcframework.annotation.MjController;
import com.magi.mvcframework.annotation.MjRequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@MjController
@MjRequestMapping("/demo")
public class DemoAction {
    @MjAutowired
    private IDemoService demoService;

    public void query(HttpServletRequest request, HttpServletResponse response, String name){
        String result = demoService.get(name);
        try {
            response.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @MjRequestMapping("add")
    public void add(HttpServletRequest request,HttpServletResponse response, Integer a,Integer b){
            try {
            response.getWriter().write(a+"+"+b+"="+(a+b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @MjRequestMapping("remove")
    public void remove(HttpServletRequest request,HttpServletResponse response, Integer id){
        try {
            response.getWriter().write(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
