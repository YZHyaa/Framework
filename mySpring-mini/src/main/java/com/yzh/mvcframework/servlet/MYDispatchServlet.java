package com.yzh.mvcframework.servlet;

import com.yzh.mvcframework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * Springmvc实际上就是一个做着请求分发的Servlet
 * Tomcat就是用来创建Servlet对象的
 * 两者关联：
 *          web.xml（必须）：要创建DispatchServlet需要在web.xml中配置
 *          server.xml（一般不在应用中配置）：具体的服务器运行配置
 */
public class MYDispatchServlet extends HttpServlet {

    private Properties contextConfig = new Properties();

    private List<String> classNames = new ArrayList<String>();

    private Map<String, Object> ioc = new HashMap<String, Object>();

    private Map<String, Method> handlerMapping = new HashMap<String, Method>();

    // 初始化阶段，在容器创建Servlet时会执行初始化方法（init）
    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1.加载配置文件
        // 传入配置文件路径
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        // 2.扫描先关类
        // 传入配置文件中的scanPackage的包名
        doScanner(contextConfig.getProperty("scanPackage"));
        // 3.初始化扫描到的类，并将它们放入IOC容器
        doInstance();

        // 4.完成依赖注入
        doAutowired();

        // 5.初始化HandlerMappring
        initHandlerMapping();

        System.out.println("MYSpring framework is init");
    }

    // 运行阶段
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 请求分发，将请求分发个相应方法
        // 注：此处，该方法还包含了分发后方法的执行
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Excetion Detail:" +Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    // 请求分发与执行
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // 获取请求路径
        // 注：getRequestURL：http://localhost:8080/demo/query
        //     getRequestURI: /demo/query
        //     getContextPath: 获取当前Context的统一路径，一般用于一个容器部署多个Context，而此处是" "
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        // url处理：将contextPath去除（应用标识），将多个 / 的替换成一个 /
        url = url.replaceAll(contextPath, "").replaceAll("/+","/");

        // 如果没有处理这个url的method，就返回404
        if (!handlerMapping.containsKey(url)) {
            resp.getWriter().println("404 Not Found!!!");
            return;
        }

        // 获取处理该请求的方法
        Method method = this.handlerMapping.get(url);
        // 获取该method需要的参数类型们（形参列表）
        Class<?>[] parameterTypes = method.getParameterTypes();
        // 用来保存当前方法的具体参数（实参列表）
        Object[] paramValues = new Object[parameterTypes.length];

        // 获取请求中携带的参数映射
        Map<String, String[]> parameterMap = req.getParameterMap();

        // 遍历形参列表，将形参与实参对应（即填充paramValues）
        for (int i = 0; i < parameterTypes.length; i++) {
            Class paramterType = parameterTypes[i];
            // 如果是Request，直接将req放入
            if (paramterType == HttpServletRequest.class) {
                paramValues[i] = req;
                continue;
            // 如果是Response，直接将resp放入
            } else if (paramterType == HttpServletResponse.class) {
                paramValues[i] = resp;
                continue;
             // 如果是String，就要具体寻找其对应的实参了
            } else if (paramterType == String.class) {

                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                for (int j = 0; j < parameterAnnotations.length; j++) {
                    for (Annotation a : parameterAnnotations[i]) {
                        // 如果是@RequestParam注解的，就是指定了请求中携带参数名的
                        if (a instanceof MYRequestParam) {
                            // 获取指定的参数名
                            String paramName = ((MYRequestParam)a).value();
                            if (!"".equals(paramName.trim())) {
                                // 在请求Request携带的请求参数map中，获取到对应参数
                                String value = Arrays.toString(parameterMap.get(paramName))
                                        .replaceAll("\\[|\\]","")
                                        .replaceAll("\\s",",");
                                // 放入实参列表
                                paramValues[i] = value;
                            }
                        }
                    }
                }
            }
        }
        // 获取具体执行该方法的对象，
        // 注：此处是直接获取方法的所有类的对象
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        // 执行当前方法，传入req，resp，实参列表
        // 注：此处是直接将获取的参数名写死了！！！
        method.invoke(ioc.get(beanName),new Object[]{req, resp, parameterMap.get("name")[0]});
    }

    // 创建方法与请求的映射
    private void initHandlerMapping() {
        if (ioc.isEmpty()) return;

        // 遍历所有在容器中的对象
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            // 不是Controller直接返回
            if (!clazz.isAnnotationPresent(MYController.class)) continue;

            // 获取Controller上的同一请求路径
            String baseUrl = "";
            if (clazz.isAnnotationPresent(MYRequestMapping.class)) {
                MYRequestMapping annotation = clazz.getAnnotation(MYRequestMapping.class);
                baseUrl = annotation.value();
            }

            // 获取Controller中每个方法的请求路径
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                // 不是处理请求的方法就直接返回
                if (!method.isAnnotationPresent(MYRequestMapping.class)) continue;

                MYRequestMapping annotation = method.getAnnotation(MYRequestMapping.class);
                // URL处理：无论写了几个 / 都处理成一个
                String url = ("/" + baseUrl + "/" + annotation.value()).replaceAll("/+", "/");
                handlerMapping.put(url, method);
                // 日志打印能处理的路径
                System.out.println("Mapped" + url + "," + method);
            }
        }
    }

    // 对已经被管理的对象们进行依赖注入
    private void doAutowired() {
        // IOC容器为空，表示没有能进行依赖注入的对象
        if (ioc.isEmpty()) { return; }

        // 遍历IOC容器
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            // 1.拿到当前对象所有field
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            // 2.遍历所以field们
            for (Field field : fields) {
                // 2.1 判断这些filed中们有没有需要注入的（有@MYAtowired）
                if (field.isAnnotationPresent(MYAutowired.class)) {
                    // 2.2 获取要注入的beanName
                    MYAutowired annotation = field.getAnnotation(MYAutowired.class);
                    // 1.自定义beanName
                    String beanName = annotation.value().trim();
                    if ("".equals(beanName)) {
                        // 2.若没有自定义beanName，就以字段的类名作为beanName
                        beanName = field.getType().getName();
                    }

                    // 3.将IOC中的对象注入当前field
                    // setAccessible是为了对public外的字段也能进行注入
                    field.setAccessible(true);
                    try {
                        field.set(entry.getValue(), ioc.get(beanName));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        }

    }

    // 将需要IOC容器管理的对象放入IOC容器
    private void doInstance() {
        if (classNames.isEmpty()) {return;}

        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                // 1.标了@MYController的类的对象需要IOC容器管理
                if (clazz.isAnnotationPresent(MYController.class)) {
                    Object instance = clazz.newInstance();
                    // 这里getSimpleName是只获取类名，而IOC容器的beanName只要单纯的类名就够了
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, instance);
                 // 2.标了@MYService的类的对象需要IOC容器管理
                } else if (clazz.isAnnotationPresent(MYService.class)) {
                    Object instance = clazz.newInstance();
                    // 2.1 按变量名注入，所以一般需要类名小写（驼峰规则）
                    // 注：这里明确一个问题，classNames中存的是全类名，而这里要做beanName的只能是类名
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    // 2.2 按自定义beanName注入
                    // 注：这里就是注解的基本操作，拿到注解，然后通过注解中定义的方法拿到参数
                    MYService annotation = clazz.getAnnotation(MYService.class);
                    if (!"".equals(annotation.value())) {
                        beanName = annotation.value();
                    }

                    ioc.put(beanName, instance);
                    // 2.3 按类型注入，这里要考虑其接口，因为一个对象不止对应一个类型
                    // 注：getInterface获取当前类的接口
                    for (Class<?> i : clazz.getInterfaces()) {
                        // 注：getName获取的是全类名，与放在classNames中的全类名形式一样，且都是为了反射拿到Class
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The beanName is existed!!!");
                        }
                        ioc.put(i.getName(), instance);
                    }
                // 3.没有注解（或配置）的类，IOC容器不管；
                } else {
                    // 接着看下一个类
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 这里默认是只传入类名大写的，A=65, a=97
    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return new String(chars);
    }

    // 得到指定包下所有类的全类名 ---> 文件操作
    private void doScanner(String scanPackage) {
        // 1.获取包（文件夹）的绝对路径：要new File就要相对路径或绝路径，而包名根本什么都不是
        // 注：getResource方法返回的是URL对象（里面封装了File等），用来获取指定类或包的绝对路径
        //     getResource获取绝对路劲，首先要警包名转化成项目先对路径
        //     最前面 / 表示从根路径中寻找，显然要找到这个包不能从当前目录下寻找
        URL url = this.getClass().getClassLoader().
                getResource("/" + scanPackage.replaceAll("\\.", "/"));

        // 2.创建文件夹的File对象
        File classpath = new File(url.getFile());

        // 3.遍历文件夹，寻找class文件
        for (File file : classpath.listFiles()) {
            if (file.isDirectory()) {
                // 这里是通过递归遍历文件夹，还是包就再执行上述步骤（解析路径->创建目录->遍历）
                doScanner(scanPackage + "." + file.getName());
            } else {
                // 不是class文件的不管
                if (!file.getName().endsWith("class")) {continue;}
                // 这里要保存全类名（包.类名），因为后面要通过反射Class.forName获取Class对象
                String className = (scanPackage + "." + file.getName()).replace(".class", "");
                classNames.add(className);
            }
        }
    }

    // 读取配置文件信息到内存
    private void doLoadConfig(String contextConfigLocation) {
        // 通过当前类的ClassLoader读取Properties文件，成IO流；相当于读入了 scanPackage=com.yzh.demo 到内存中
        InputStream fis = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            // 存到Properties对象中
            contextConfig.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

