package com.magi.mvcframework.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author magi
 */
public class MjDispatcherServlet extends HttpServlet{

    private Properties contextConfig = new Properties();

    private List<String> classNames = new ArrayList<String>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        super.doPost(req, resp);

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        super.doGet(req, resp);
        //调用 doGet 或者 doPost 方法,将结果输出到浏览器
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
//        super.init(config);

        //1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2.解析配置文件,扫描所有相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        //3.初始化所有相关的类,并且保存到 IOC 容器之中
        doInstance();

        //4.完成自动化的依赖注入, DI
        doAutowired();

        //5.创建 HandlerMapping 将 URL和 method 建立对应关系
        initHandlerMapping();


    }

    private void initHandlerMapping() {
    }

    private void doAutowired() {
    }

    private void doInstance() {
    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.","/"));

        File classDir = new File(url.getFile());

        for (File file:classDir.listFiles()){

            //递归判断
            if (file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (file.getName().contains(".class")){continue;}
                String className = scanPackage + "." + file.getName().replace(".class","").trim();
                classNames.add(className);
            }
        }

    }

    private void doLoadConfig(String contextConfigLocation) {
        //从类的路径下 取得 properties
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null!=is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
