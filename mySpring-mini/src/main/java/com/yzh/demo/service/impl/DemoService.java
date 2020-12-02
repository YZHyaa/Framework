package com.yzh.demo.service.impl;

import com.yzh.demo.service.IDemoService;
import com.yzh.mvcframework.annotation.MYService;

/**
 * 核心业务逻辑
 */
@MYService
public class DemoService implements IDemoService {

	public String get(String name) {
		return "My name is " + name;
	}
}
