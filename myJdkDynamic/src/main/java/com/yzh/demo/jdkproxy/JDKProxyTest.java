package com.yzh.demo.jdkproxy;

import com.yzh.demo.Customer;
import com.yzh.demo.Person;

public class JDKProxyTest {

    public static void main(String[] args) {
        try {
			Person obj = (Person)new JDKMeipo().getInstance(new Customer());
			obj.findLove();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
