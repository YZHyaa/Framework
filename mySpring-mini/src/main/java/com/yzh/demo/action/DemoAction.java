package com.yzh.demo.action;

import com.yzh.demo.service.IDemoService;
import com.yzh.mvcframework.annotation.MYAutowired;
import com.yzh.mvcframework.annotation.MYController;
import com.yzh.mvcframework.annotation.MYRequestMapping;
import com.yzh.mvcframework.annotation.MYRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@MYController
@MYRequestMapping("/demo")
public class DemoAction {

  	@MYAutowired
	private IDemoService demoService;

	@MYRequestMapping("/query")
	public void query(HttpServletRequest req, HttpServletResponse resp,
                      @MYRequestParam("name") String name){
		String result = demoService.get(name);
//		String result = "My name is " + name;
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@MYRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@MYRequestParam("a") Integer a, @MYRequestParam("b") Integer b){
		try {
			resp.getWriter().write(a + "+" + b + "=" + (a + b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@MYRequestMapping("/remove")
	public void remove(HttpServletRequest req, HttpServletResponse resp,
                       @MYRequestParam("id") Integer id){
	}

}
