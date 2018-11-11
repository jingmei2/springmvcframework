package com.magi.mvcframework.servlet;

import com.magi.mvcframework.annotation.MjAutowired;
import com.magi.mvcframework.annotation.MjController;
import com.magi.mvcframework.annotation.MjRequestMapping;
import com.magi.mvcframework.annotation.MjService;

import javax.naming.Name;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author magi
 */
public class MjDispatcherServlet extends HttpServlet{

    private Properties contextConfig = new Properties();

    /**
     * bean类的集合
     */
    private List<String> classNames = new ArrayList<String>();

    /**
     * IOC 容器
     */
    private Map<String,Object> ioc = new HashMap<String,Object>();

    /**
     * handleMapping
     */
    private Map<String,Method> handleMapping = new HashMap<>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        super.doPost(req, resp);
        //调用 doGet 或者 doPost 方法,将结果输出到浏览器
        try {
            //找到 Method方法,通过反射机制 invoker, 再将返回的结果交给 IOC 容器
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            //处理异常
            resp.getWriter().write("500 Exception" + Arrays.toString(e.getStackTrace()));
        }

    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //拿到请求,处理请求
        if (this.handleMapping.isEmpty()){return;}
        //访问的地址
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url.replaceAll(contextPath,"").replaceAll("/+","/");
        //判断handleMapping是否含有这个 url
        if (!this.handleMapping.containsKey(url)){
            resp.getWriter().write("404 Not Found!!");
            return;
        }
        //根据 url 取到方法
        Method method = this.handleMapping.get(url);
        Map<String,String[]> params = req.getParameterMap();
        //方法反射得到对象,加入到 IOC 容器中
        String beanName = lowerFristCase(method.getDeclaringClass().getSimpleName());

        //Todo 还有其他方法获取参数的 key,value, 不需要写死
        method.invoke(ioc.get(beanName),new Object []{req,resp},params.get("name")[0]);
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
        //通过反射机制,将实例化的对象放入 IOC 容器中
        doInstance();

        //4.完成自动化的依赖注入, DI
        //在 IOC 容器中的实例,有很多属性没有复制,自动把需要的赋值的属性经行赋值
        doAutowired();

        //5.创建 HandlerMapping 将 URL和 method 建立对应关系
        //能够将一个 URL 和一个 method 进行一个关联映射
        initHandlerMapping();

        if (ioc.isEmpty()){
            System.out.println("MjSpring MVC 's IOC is null");
        } else {
            System.out.println("MjSpring MVC is init");
        }
    }

    private void initHandlerMapping() {
        if (ioc.isEmpty()){return;}
        //遍历 IOC 容器中的实体
        for (Map.Entry<String,Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();

            if (!clazz.isAnnotationPresent(MjController.class)){continue;}
            String baseUrl ="";
            //判断是否含有MjRequestMapping注解
            if (clazz.isAnnotationPresent(MjRequestMapping.class)){
                MjRequestMapping requestMapping = clazz.getAnnotation(MjRequestMapping.class);
                baseUrl = requestMapping.value();

            }

            //开始扫描所有类 获取所有公共方法
            Method [] methods = clazz.getMethods();
            for (Method method : methods){
                //判断注解的类
                if (!method.isAnnotationPresent(MjRequestMapping.class)){continue;}

                MjRequestMapping requestMapping = method.getAnnotation(MjRequestMapping.class);
                String url = (("/"+baseUrl+"/"+requestMapping.value()).replaceAll("/+","/"));

                //把访问地址和方法映射到handleMapping里面
                handleMapping.put(url,method);

                System.out.println("Mapped:"+url+","+method);
            }

        }
    }

    private void doAutowired() {
        if (ioc.isEmpty()){return;}

        for (Map.Entry<String,Object> entry : ioc.entrySet()){
            //获取容器里 bean 类的字段数组
            Field [] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields){
                //判断字段上是否有注解(各种注解)
                if (!field.isAnnotationPresent(MjAutowired.class)){
                    continue;
                }

                MjAutowired autowired = field.getAnnotation(MjAutowired.class);
                String beanName = autowired.value();
                if ("".equals(beanName)){
                    beanName = field.getType().getName();
                }
                //如果是 private 的话 是不可见,需要强制可见
                field.setAccessible(true);
                try {
                    //给字段设置, key 和注入的对象
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }


            }
        }


    }

    private void doInstance() {
        if (classNames.isEmpty()){return;}

        //开始反射
        try {
            for (String className : classNames){
                Class<?> clazz = Class.forName(className);

                //判断需要实例化的class
                if (clazz.isAnnotationPresent(MjController.class)){
                    //取单纯的类名 不含有包名
                    String beanName = lowerFristCase(clazz.getSimpleName());
                    ioc.put(beanName,clazz.newInstance());



                } else if (clazz.isAnnotationPresent(MjService.class)){

                    //1. 默认类名首字母小写
                    //2. 自定义的命名
                    MjService service = clazz.getAnnotation(MjService.class);
                    String beanName = service.value();
                    if ("".equals(beanName)){
                        beanName = lowerFristCase(clazz.getSimpleName());
                    }

                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);

                    //3. 注入接口的实现类,用接口的全称作为 key, 用接口的实现类实例作为值
                    //实现接口可能有多个,是为数组
                    Class<?> [] interfaces = clazz.getInterfaces();
                    for (Class ifs : interfaces){
                        if (ioc.containsKey(ifs.getName())){
                            throw new Exception("The beanName is exists:"+ifs.getName());
                        }
                        ioc.put(ifs.getName(),instance);

                    }

                } else {
                    continue;
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private String lowerFristCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return chars.toString();
    }

    private void doScanner(String scanPackage) {
        //扫描到编译成功后的类的相对地址
        //Todo 扫描编译成功后的 class 文件 不需要写死
        URL url = this.getClass().getClassLoader().getResource("../../target/classes/"+scanPackage.replaceAll("\\.","/"));

        File classDir = new File(url.getFile());

        //递归判断 判断文件夹 并且扫描出所有的 class 文件

//        File[] files = classDir.listFiles(new FileFilter() {
//            @Override
//            public boolean accept(File pathname) {
//                // TODO Auto-generated method stub
//                String KEY_PRE = ".class";
//                if (pathname.isFile()&&pathname.getName().endsWith(KEY_PRE)) {
//
//                    return true;
//                }
//                    return false;
//            }
//        });
//
//        for (int i = 0; i < files.length; i++) {
//            File file = files[i];
//            System.out.println(files[i].getName());
//            if (!file.getName().contains(".class")){continue;}
//                String className = scanPackage + "." + file.getName().replace(".class","").trim();
//                classNames.add(className);
//        }

        for (File file:classDir.listFiles()){

            //递归判断
            if (file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().contains(".class")){continue;}
                String className = scanPackage + "." + file.getName().replace(".class","").trim();
                classNames.add(className);
            }
        }

    }

    private void doLoadConfig(String contextConfigLocation) {
        //从类的路径下 取得 properties
        //Todo 需要兼容 classpath:* 这样的写法
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
