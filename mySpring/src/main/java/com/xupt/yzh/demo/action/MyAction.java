package com.xupt.yzh.demo.action;

import com.xupt.yzh.demo.service.IModifyService;
import com.xupt.yzh.demo.service.IQueryService;
import com.xupt.yzh.framework.annotation.MYAutowired;
import com.xupt.yzh.framework.annotation.MYController;
import com.xupt.yzh.framework.annotation.MYRequestMapping;
import com.xupt.yzh.framework.annotation.MYRequestParam;
import com.xupt.yzh.framework.webmvc.servlet.MYModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 */
@MYController
@MYRequestMapping("/web")
public class MyAction {

	@MYAutowired
	IQueryService queryService;
	@MYAutowired
	IModifyService modifyService;

	@MYRequestMapping("/first.html")
    public MYModelAndView query(@MYRequestParam("name") String name) {
        String result = queryService.query(name);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("name", name);
        model.put("data", result);
        model.put("token", 123456);
        return new MYModelAndView("first", model);
    }

	@MYRequestMapping("/query.json")
	public MYModelAndView query(HttpServletRequest request, HttpServletResponse response,
                                @MYRequestParam("name") String name){
		String result = queryService.query(name);
		return out(response,result);
	}

	@MYRequestMapping("/add*.json")
	public MYModelAndView add(HttpServletRequest request, HttpServletResponse response,
                              @MYRequestParam("name") String name, @MYRequestParam("addr") String addr){
		String result = null;
		try {
			// 该方法会抛出自定义异常
			result = modifyService.add(name,addr);
			return out(response,result);
		} catch (Exception e) {
			// 将异常信息保存在Map中，然后放入Model
			Map<String,Object> model = new HashMap<String,Object>();
			// 注：这里在单独测 mvc 模块时要去掉getCause
			model.put("detail",e.getMessage());
			model.put("stackTrace", Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]",""));

			return new MYModelAndView("500",model);
		}

	}

	@MYRequestMapping("/remove.json")
	public MYModelAndView remove(HttpServletRequest request, HttpServletResponse response,
                                 @MYRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		return out(response,result);
	}

	@MYRequestMapping("/edit.json")
	public MYModelAndView edit(HttpServletRequest request, HttpServletResponse response,
                               @MYRequestParam("id") Integer id,
                               @MYRequestParam("name") String name){
		String result = modifyService.edit(id,name);
		return out(response,result);
	}



	private MYModelAndView out(HttpServletResponse resp, String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void test(String name) {
        System.out.println(queryService.query(name));
    }

}
