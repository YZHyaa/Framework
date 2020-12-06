package com.xupt.yzh.framework.aop.support;

import com.xupt.yzh.framework.aop.aspect.MYAfterReturningAdviceInterceptor;
import com.xupt.yzh.framework.aop.aspect.MYAfterThrowingAdviceInterceptor;
import com.xupt.yzh.framework.aop.aspect.MYMethodBeforeAdviceInterceptor;
import com.xupt.yzh.framework.aop.config.MYAopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 主要封装了被代理对象信息，以及一些处理被代理对象的方法
 *  每个IOC容器中对象都一一对应一个AdvisedSupport对象，但只有符合pointCut的对象才是真正的target
 */
public class MYAdvisedSupport {
    // 目标类
    private Class<?> targetClass;
    // 目标对象，即被代理对象
    private Object target;
    // 配置文件信息
    private MYAopConfig config;
    // 被代理类特征正则表达式
    private Pattern pointCutClassPattern;
    // 保存当前对象切入点方法们，与拦截器链的映射关系
    private transient Map<Method, List<Object>> methodCache;

    public MYAdvisedSupport(MYAopConfig config) {
        this.config = config;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    // 将当前类放入AdvisedSupport对象中，同时初始化pointCutClassPattern（被代理类特征正则）
    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    /*
        1.初始化目标类特征pointCutClassPattern
        2.保存切面类的所有方法（LogAspect），将它们放入aspectMethods的Map中
        3.为当前类中符合切入点的方法构造拦截器链，并将它们放入map中
     */
    private void parse() {
        String pointCut = config.getPointCut()
                .replaceAll("\\.","\\\\.")
                .replaceAll("\\\\.\\*",".*")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");
        // pointCut=public .* com.xupt.yzh.demo.service..*Service..*(.*)
        // 1.通过正则表达式获取被代理类特征，初始化pointCutForClassRegex
        String pointCutForClassRegex = pointCut.substring(0,pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegex.substring(
                pointCutForClassRegex.lastIndexOf(" ") + 1));

        try {
            // 2.拿到切面类，这里是LogAspect，并保存其所有方法
            Class aspectClass = Class.forName(this.config.getAspectClass());
            // 保存切面类（LogAspect）的所有方法
            Map<String,Method> aspectMethods = new HashMap<String,Method>();
            for (Method m : aspectClass.getMethods()) {
                aspectMethods.put(m.getName(),m);
            }

            methodCache = new HashMap<Method, List<Object>>();
            // 拿到切入点正则，即切入方法特征
            Pattern pattern = Pattern.compile(pointCut);

            // 3.遍历当前类的所有方法，为符合切入点的方法构造拦截器链，最后放入map中
            for (Method m : this.targetClass.getMethods()) {
                String methodString = m.toString();
                if (methodString.contains("throws")) {
                    methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
                }

                // 判断当前方法是否是切点方法
                Matcher matcher = pattern.matcher(methodString);
                if(matcher.matches()){
                    // 若是，则构造一个执行器链，
                    List<Object> advices = new LinkedList<Object>();
                    // 把每一个方法包装成 MethodIterceptor
                    // before
                    if(!(null == config.getAspectBefore() || "".equals(config.getAspectBefore()))) {
                        //创建一个Advivce
                        advices.add(new MYMethodBeforeAdviceInterceptor(aspectMethods.get(config.getAspectBefore()),aspectClass.newInstance()));
                    }
                    // after
                    if(!(null == config.getAspectAfter() || "".equals(config.getAspectAfter()))) {
                        //创建一个Advivce
                        advices.add(new MYAfterReturningAdviceInterceptor(aspectMethods.get(config.getAspectAfter()),aspectClass.newInstance()));
                    }
                    // afterThrowing
                    if(!(null == config.getAspectAfterThrow() || "".equals(config.getAspectAfterThrow()))) {
                        //创建一个Advivce
                        MYAfterThrowingAdviceInterceptor throwingAdvice =
                                new MYAfterThrowingAdviceInterceptor(
                                        aspectMethods.get(config.getAspectAfterThrow()),
                                        aspectClass.newInstance());
                        throwingAdvice.setThrowName(config.getAspectAfterThrowingName());
                        advices.add(throwingAdvice);
                    }
                    // 最后，将当前方法与执行器链放入mthodCache
                    methodCache.put(m,advices);
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public Class<?> getTargetClass() {
        return this.targetClass;
    }

    public Object getTarget() {
        return this.target;
    }

    // 获取指定方法的执行器链
    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) throws Exception{
        List<Object> cached = methodCache.get(method);

        // 如果当前方法没有拦截器链，那么把拦截器链置为null，也封装一下，然后返回原方法
        if (cached == null) {
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());
            // cached 就等于原方法
            cached = methodCache.get(m);
            //底层逻辑，对代理方法进行一个兼容处理
            this.methodCache.put(m, cached);
        }

        return cached;
    }

    // 判断当前AdvisedSupport是否匹配能匹配上切点表达式的类
    public boolean pointCutMatch() {
        // 通过被代理类特征正则表达式来匹配当前类
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }
}
