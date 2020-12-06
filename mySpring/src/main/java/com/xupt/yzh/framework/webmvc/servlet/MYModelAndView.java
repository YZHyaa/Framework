package com.xupt.yzh.framework.webmvc.servlet;

import java.util.Map;

/*
    Controller层返回的对象，里面包含了要返回的页面，以及页面里所需要的参数
    需要Resolver（模板引擎）去解析成View,View再通过模板引擎即系model，然后返回页面
 */
public class MYModelAndView {

    private String ViewName;

    private Map<String, ?> model;

    public MYModelAndView(String viewName, Map<String, ?> model) {
        ViewName = viewName;
        this.model = model;
    }

    public MYModelAndView(String viewName) {
        ViewName = viewName;
    }

    public String getViewName() {
        return ViewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }
}
