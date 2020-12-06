package com.xupt.yzh.framework.beans.support;

import com.xupt.yzh.framework.beans.config.MYBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 1.加载配置文件，在Spring中本来是由Resource相关类进行解析，而这里为了简便就都放在这一个类中了
 * 2.将配置文件组装成Beandefinition
 * 为了扩展，这里本来海英有一个统一接口，然后应用策略模式；但为了方便这里只加载properties文件
 */
public class MYBeanDefinitionReader {

    private List<String> registerBeanClasses = new ArrayList<String>();

    private Properties config = new Properties();

    // 定义Properties文件中要扫描包的key，相当于一种规范
    private final String SCAN_PACKAGE = "scanPackage";

    public MYBeanDefinitionReader(String... locations) {
        // 根据url读取配置文件，加载成io流
        // 注：这里因为知道要加载properties文件，所以取[0]
        //     删掉classpath是因为，在传入配置文件位置时，常见写法是 classpath：application.Properties
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));
        // 通过Properties类，将IO流解析成properties，然后关闭IO流
        try {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 对指定包进行扫描
        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    // 扫描指定包，当前包下所有class
    private void doScanner(String scanPackage) {
        // 1.获取包（文件夹）的绝对路径：要new File就要相对路径或绝路径，而包名根本什么都不是
        // 注：getResource是通过相对路径（需转换）找到目标文件后，获取其绝对路径，返回的是URL对象（里面封装了File等）
        //     通过URL对象的getFile方法，获取指定类或包的绝对路径
        //     最前面 / 表示从根路径中寻找，显然要找到这个包不能从当前目录下寻找
        URL url = this.getClass().getResource("/" + scanPackage.replaceAll("\\.", "/"));

        // 2.创建文件夹的File对象
        // 通过URL对象的getFile方法获取到绝对路径
        File classpath = new File(url.getFile());

        // 3.遍历文件夹，寻找class文件
        for (File file : classpath.listFiles()) {
            if (file.isDirectory()) {
                // 这里是通过递归遍历文件夹，还是包就再执行上述步骤（解析路径->创建目录->遍历）
                doScanner(scanPackage + "." + file.getName());
            } else {
                // 不是class文件的不管
                if (!file.getName().endsWith("class")) {continue;}
                // 这里要保存全类名（包.类名），因为后面要通过反射Class.forName获取Class对象
                String className = (scanPackage + "." + file.getName()).replace(".class", "");
                registerBeanClasses.add(className);
            }
        }
    }

    // 将Scanner读取出来的类（名）转换成BeanDefinition
    public List<MYBeanDefinition> loadBeanDefinitions() {
        List<MYBeanDefinition> result = new ArrayList<MYBeanDefinition>();
        try {
            for (String className : registerBeanClasses) {
                Class<?> clazz = Class.forName(className);
                // 接口不能实例化（相当于不能有factoryBean），不处理
                if (clazz.isInterface()) continue;

                // 一个Class对应多个BeanDefinition，一个BeanDefinition对应一个Bean，一个Bean对应多个beanName
                // 所以在beanName这里又可以分为五三类
                // 1.默认类名首字母小写
                result.add(doCreateBeanDefinition(toLowerFirstCase(clazz.getSimpleName()), clazz.getName()));
                // 2.接口
                Class<?>[] interfaces = clazz.getInterfaces();
                for (Class<?> i : interfaces) {
                    // 注：这里若一个接口有多个实现类，那么就会覆盖（相当于只能保存一个实现类的）
                    //      这种情况下，可以通过注入Bean时指定name解决
                    result.add(doCreateBeanDefinition(i.getName(), clazz.getName()));
                }
                // TODO 3.自定义beanName
                // 一般配合@Resource注解
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private MYBeanDefinition doCreateBeanDefinition(String factoryBeanName, String beanClassName) {
        MYBeanDefinition myBeanDefinition = new MYBeanDefinition();
        myBeanDefinition.setBeanClassName(beanClassName);
        myBeanDefinition.setFactoryBeanName(factoryBeanName);
        return myBeanDefinition;
    }

    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public Properties getConfig() {
        return this.config;
    }
}
