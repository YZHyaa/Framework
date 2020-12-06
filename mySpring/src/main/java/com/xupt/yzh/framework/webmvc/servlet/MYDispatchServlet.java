package com.xupt.yzh.framework.webmvc.servlet;

import com.xupt.yzh.framework.annotation.MYController;
import com.xupt.yzh.framework.annotation.MYRequestMapping;
import com.xupt.yzh.framework.context.MYApplicationContext;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MYDispatchServlet extends HttpServlet {

    private final String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";

    private MYApplicationContext context;

    private List<MYHandlerMapping> handlerMappings = new ArrayList<MYHandlerMapping>();

    private Map<MYHandlerMapping, MYHandlerAdpter> handereAdpters = new HashMap<MYHandlerMapping, MYHandlerAdpter>();

    private List<MYViewResolver> viewResolvers = new ArrayList<MYViewResolver>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            // 向页面打印错误
            resp.getWriter().println("500 Exception,Details:\r\n" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]", "").replaceAll(",\\s", "\r\n"));
            e.printStackTrace();
        }
    }

    // 请求分发 --> 通过反射执行相应方法 --> 对结果进行解析与渲染
    /*
        到这里init方法已经执行过，即已经创建好了
            1.处理请求的handler ---> 实质上是把把 IOC 容器管理的Bean实例进行了封装（包括代理对象的替换），并建立了映射关系
            2.请求参数适配及执行handler的Adpter
            3.对处理结果进行视图转换的 ViewResolver，及 ViewResolver内自定义的模本解析引擎
        所以这里处理请求其实只需要拿出相应组件即可
     */
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        // 1.通过从Request中拿到URL，去匹配一个HandlerMapping
        MYHandlerMapping handler = getHandler(req);
        if (handler == null) {
            // 输出404页面
            processDispatchResult(req, resp, new MYModelAndView("404"));
            return;
        }

        // 2.获取当前handler对应的处理参数的Adpter
        MYHandlerAdpter handlerAdpter = getHandlerAdptor(handler);

        // 3.Adpter真正调用处理请求的方法,返回ModelAndView（存储了页面上值，和页面模板的名称）
        // 将 request 进行处理转为 handler的参数并执行
        // 返回方法执行结果（可能是null，可能是ModelAndView。。。）
        MYModelAndView mv = handlerAdpter.handle(req, resp, handler);

        // 4.真正输出,将方法执行进行处理然后返回
        processDispatchResult(req, resp, mv);

    }

    // 将ModelAndView解析成 HTML、json、outputStream、freemark等 --> 然后解析数据 --> 最后输出给前端
    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, MYModelAndView mv) throws Exception {
        // null 表示方法返回类型是void，或返回值是null
        if(mv == null) {
            return;
        }

        // 如果没有视图解析器就返回，因为无法处理ModelAndView
        if (this.viewResolvers.isEmpty()) {
            return;
        }

        // 遍历视图解析器
        for (MYViewResolver viewResolver : this.viewResolvers) {
            // 通过相应解析器，返回相应页面 View
            MYView view = viewResolver.resolveViewName(mv.getViewName(), null);
            // View通过模板引擎（自定义的）解析后输出
            view.render(mv.getModel(), req, resp);
            return;
        }
    }

    private MYHandlerAdpter getHandlerAdptor(MYHandlerMapping handler) {
        if (this.handereAdpters.isEmpty()) {
            return null;
        }
        MYHandlerAdpter handlerAdpter = this.handereAdpters.get(handler);
        // 判断当前handler能否被当前adptor进行适配（即将传入参数转换成该handler的参数，并处理）
        if (handlerAdpter.supports(handler)) {
            return handlerAdpter;
        }
        return null;
    }

    // 通过Request获取相应handler
    private MYHandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty()) return null;

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        for (MYHandlerMapping handler : this.handlerMappings) {
            Matcher matcher = handler.getPattern().matcher(url);
            // 如果没有匹配上就继续遍历handler
            if (!matcher.matches()) {
                continue;
            }
            return handler;
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        // 1.初始化ApplicationContext ！！！
        // tomcat会加载web.xml并创建其中配置的servlet，同时会执行init方法，这里的config即web.xml配置信息
        context = new MYApplicationContext(config.getInitParameter(CONTEXT_CONFIG_LOCATION));

        // 2.初始化SpringMVC九大组件
        initStrategies(context);
    }

    protected void initStrategies(MYApplicationContext context) {
        //多文件上传的组件
        initMultipartResolver(context);
        //初始化本地语言环境
        initLocaleResolver(context);
        //初始化模板处理器
        initThemeResolver(context);


        //handlerMapping，必须实现
        initHandlerMappings(context);
        //初始化参数适配器，必须实现
        initHandlerAdapters(context);
        //初始化异常拦截器
        initHandlerExceptionResolvers(context);
        //初始化视图预处理器
        initRequestToViewNameTranslator(context);


        //初始化视图转换器，必须实现
        initViewResolvers(context);
        //参数缓存器
        initFlashMapManager(context);
    }

    private void initFlashMapManager(MYApplicationContext context) {

    }

    private void initViewResolvers(MYApplicationContext context) {
        // 拿到模板存放路径(layouts)
        String templateRoot = context.getConfig().getProperty("templateRoot");
        // getResourse返回的是URL对象
        // getFile返回文件的绝对路径
        // 即通过相对路径找到目标后，获取到绝对路径
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        String[] templates = templateRootDir.list();
        // 模板引擎可以有多种，且不同的模板需要不同的Resolver去解析成不同的View（jsp，html，json。。）
        // 但这里其实就只有一种（自定义解析）
        // 为了仿真才写了这个循环，其实只循环一次
        for (int i = 0; i < templates.length; i ++) {
            this.viewResolvers.add(new MYViewResolver(templateRoot));
        }
    }

    private void initRequestToViewNameTranslator(MYApplicationContext context) {

    }

    private void initHandlerExceptionResolvers(MYApplicationContext context) {
    }

    private void initHandlerAdapters(MYApplicationContext context) {
        for (MYHandlerMapping handlerMapping : this.handlerMappings) {
            this.handereAdpters.put(handlerMapping, new MYHandlerAdpter());
        }
    }

    private void initHandlerMappings(MYApplicationContext context) {
        // 一个BeanDefinition对应一个Bean,依次拿到
        String[] beanNames = context.getBeanDefinitionNames();

        try {
            for (String beanName : beanNames) {
                Object controller = context.getBean(beanName);

                Class<?> clazz = controller.getClass();

                if (!clazz.isAnnotationPresent(MYController.class)) {
                    continue;
                }

                // 获取当期Controller的共有url
                String baseUrl = "";
                if (clazz.isAnnotationPresent(MYRequestMapping.class)) {
                    MYRequestMapping annotation = clazz.getAnnotation(MYRequestMapping.class);
                    baseUrl = annotation.value();
                }

                // 获取所有方法的处理路径
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(MYRequestMapping.class)) {
                        continue;
                    }

                    MYRequestMapping annotation = method.getAnnotation(MYRequestMapping.class);
                    String regex = ("/" + baseUrl + "/" + annotation.value().replaceAll("\\*", ".*")).replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(regex);
                    // 构建处理器，并加入handlerMapping
                    this.handlerMappings.add(new MYHandlerMapping(controller, method, pattern));
                    log.info("Mapped " + regex + "," + method);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initThemeResolver(MYApplicationContext context) {

    }

    private void initLocaleResolver(MYApplicationContext context) {

    }

    private void initMultipartResolver(MYApplicationContext context) {

    }
}
