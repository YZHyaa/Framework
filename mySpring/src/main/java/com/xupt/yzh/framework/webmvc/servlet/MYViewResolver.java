package com.xupt.yzh.framework.webmvc.servlet;

import java.io.File;
import java.util.Locale;

/**
 * 解析ModelAndeView的View路径，返回视图对象View
 * 注：ViewResolver有多种，可将ModelAndView解析成多种View（如html，json，outputStream等）
 * ModelAndView --（ViewResolver） --> View --- （模板引擎（自定义））---> model ====》HTML
 * 注：模板引擎是解析html/jsp等页面中的{{}}等数据标签的工具
 */
public class MYViewResolver {

    private final String DEFALUT_TEMPALTE_SUFIX = ".html";

    // 视图目录
    private File templateRootDir;

    public MYViewResolver(String templateRoot) {
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        this.templateRootDir = new File(templateRootPath);
    }

    // 通过页面Name，返回相应View视图
    public MYView resolveViewName(String viewName, Locale locale) throws Exception {
        if (null == viewName || "".equals(viewName.trim())) {
            return null;
        }
        // 给没有 .html的加上后缀
        viewName = viewName.endsWith(DEFALUT_TEMPALTE_SUFIX) ? viewName : (viewName + DEFALUT_TEMPALTE_SUFIX);
        // 返回相应视图
        File templateFile = new File((templateRootDir.getPath() + "/" + viewName).replaceAll("/+", "/"));
        return new MYView(templateFile);
    }
}
