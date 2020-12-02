package test;

import test.test2.test3.Test2;

import java.lang.reflect.Field;

public class TestBeanName {

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
//        new Test().test();
//        new Test2().test();
//        // test.test2.test3.Test2
//        Class<?> clazz = Class.forName("test.test2.test3.Test2");
//        Object instance = clazz.newInstance();
//        Test2 test = (Test2)instance;
//        test.test();

        Class<Test> testClass = Test.class;
        Field[] declaredFields = testClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            System.out.println(declaredField.getType().getName());
        }
    }
}
