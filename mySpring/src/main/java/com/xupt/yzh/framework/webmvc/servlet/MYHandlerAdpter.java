package com.xupt.yzh.framework.webmvc.servlet;

import com.xupt.yzh.framework.annotation.MYRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *   将Request变成Handler可以处理的参数，并与其形参匹配后执行
 *   可想而知，他要拿到HandlerMapping才能干活
 *   就意味着，有几个HandlerMapping就有几个HandlerAdapter
 */
public class MYHandlerAdpter {

    // 判断当前handler能否被当前adptor进行适配（即将传入参数转换成该handler的参数，并处理）
    // 可能要被适配handler还有文件上传等，所以这个判断还是有必要的
    public boolean supports(Object handler) {
        return (handler instanceof MYHandlerMapping);
    }

    public MYModelAndView handle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception{
        MYHandlerMapping handlerMapping = (MYHandlerMapping) handler;

        // 将当前处理请求方法的形参与其顺序对应
        Map<String, Integer> paramIdxMapping = new HashMap<String, Integer>();

        // 拿到有注解的参数
        // 二维数组：[i][j]；i-在参数中第几个位置，j-第几个注解（因为一个参数可能有多个注解
        Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();
        for (int i = 0; i < pa.length; i++) {
            // 第 i 个位置参数的注解们
            for (Annotation a : pa[i]) {
                if (a instanceof MYRequestParam) {
                    String paramName = ((MYRequestParam) a).value();
                    if (!"".equals(paramName.trim())) {
                        // @RequestParam(value = paramName)
                        paramIdxMapping.put(paramName, i);
                    }
                }
            }
        }

        // 获取方法中request，response参数位置
        Class<?>[] parameterTypes = handlerMapping.getMethod().getParameterTypes();
        // 从第一个参数开始遍历
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                // 注：这里放入的是类型，因为HTTPServletRequest与response唯一
                paramIdxMapping.put(type.getName(), i);
            }
        }

        // 最后传给method的参数列表，其中参数必须是与方法形参列表对应
        Object[] paramValues = new Object[parameterTypes.length];

        // 获取request的参数列表
        Map<String, String[]> params = req.getParameterMap();
        // 遍历Request的参数列表
        for (Map.Entry<String, String[]> parm : params.entrySet()) {
            // 根据参数名，判断当前参数是否是method所需
            if (!paramIdxMapping.containsKey(parm.getKey())) {
                continue;
            }

            // 拿到当前参数value
            // 通过正则将 []删除，空白字符换成 ，
            String value = Arrays.toString(parm.getValue()).replaceAll("\\[|\\]","")
                    .replaceAll("\\s",",");
            // 拿到当前参数在方法形参中的位置
            Integer idx = paramIdxMapping.get(parm.getKey());
            // 放入实参数组
            // 注：Request中带的参数都是String类型，这里需要将它们转为method需要的正确类型
            //     paramTypes[idx] = idx位置的类型
            paramValues[idx] = caseStringValue(value,parameterTypes[idx]);
        }
        // 判断当前方法是否需要Request，Response作为参数
        if (paramIdxMapping.containsKey(HttpServletRequest.class.getName())) {
            Integer reqIdx = paramIdxMapping.get(HttpServletRequest.class.getName());
            // 拿到Request位置
            paramValues[reqIdx] = req;
        }
        if (paramIdxMapping.containsKey(HttpServletResponse.class.getName())) {
            Integer respIdx = paramIdxMapping.get(HttpServletResponse.class.getName());
            paramValues[respIdx] = resp;
        }

        // 执行方法，获取返回结果
        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(), paramValues);
        // 如果该方法返回null（出错。。），或没有返回值（增加，删除。。。），不做处理
        if (result == null || result instanceof Void) {
            return null;
        }
        // 如果该方法返回ModelAndView，那么将返回值强转为ModelAndView
        boolean isModelAndView = handlerMapping.getMethod().getReturnType() == MYModelAndView.class;
        if (isModelAndView) {
            return (MYModelAndView) result;
        }

        return null;
    }

    // 将String类型的value，转换为指定类型
    private Object caseStringValue(String value, Class<?> parameterType) {
        if (String.class == parameterType) {
            return value;
        }

        if (Integer.class == parameterType) {
            return Integer.valueOf(value);
        } else if (Double.class == parameterType) {
            return Double.valueOf(value);
        } else {
            if (value != null) {
                return value;
            }
            return null;
        }

        //...还有Long等
        // 可以考虑策略模式
    }
}
