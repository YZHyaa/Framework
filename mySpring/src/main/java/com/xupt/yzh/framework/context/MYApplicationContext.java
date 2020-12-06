package com.xupt.yzh.framework.context;

import com.xupt.yzh.framework.annotation.MYAutowired;
import com.xupt.yzh.framework.annotation.MYController;
import com.xupt.yzh.framework.annotation.MYService;
import com.xupt.yzh.framework.aop.MYAopProxy;
import com.xupt.yzh.framework.aop.MYCglibAopProxy;
import com.xupt.yzh.framework.aop.MYJdkDynamicAopProxy;
import com.xupt.yzh.framework.aop.config.MYAopConfig;
import com.xupt.yzh.framework.aop.support.MYAdvisedSupport;
import com.xupt.yzh.framework.beans.MYBeanWrapper;
import com.xupt.yzh.framework.beans.config.MYBeanDefinition;
import com.xupt.yzh.framework.beans.config.MYBeanPostProcessor;
import com.xupt.yzh.framework.beans.support.MYBeanDefinitionReader;
import com.xupt.yzh.framework.beans.support.MYDefaultListableBeanFactory;
import com.xupt.yzh.framework.beans.factory.MYBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class MYApplicationContext extends MYDefaultListableBeanFactory implements MYBeanFactory {

    private String[] configLocations;
    private MYBeanDefinitionReader reader;

    // 单例的IOC容器，存的是实例对象
    private Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<String, Object>();
    // 通用IOC容器，存的是BeanWrapper
    private Map<String, MYBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<String, MYBeanWrapper>();

    // 构造函数，要传入加载的配置文件，然后调用refresh方法
    public MYApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void refresh() throws Exception {
        // 1.定位，定位配置文件
        reader = new MYBeanDefinitionReader(configLocations);

        // 2.加载，加载配置文件到内存（BeanDefinition）
        List<MYBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        // 3.注册，注册配置信息到容器里面（伪IOC容器）
        doRegisterBeanDefinition(beanDefinitions);

        //4.把不是延时加载的类，提前初始化
        // 一般不开启懒加载，即在IOC容器初始化时就完成实例化与注入
        doAutowired();
    }

    // 只处理非延时加载情况，在初始化是就把bean创建出来
    private void doAutowired() {
        // 遍历BeanDefinition，看哪个是延时加载了
        for (Map.Entry<String, MYBeanDefinition> entry : super.beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            // 这里默认isLazyInit=false，即先提前将Bean放入IOC容器中，即这里所有BeanDefinition都会创建一个bean
            if (!entry.getValue().isLazyInit()) {
                try {
                    getBean(beanName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 将BeanDefinition们注入容器（伪IOC容器）
    // 该方法执行完后，IOC容器初始化就完成了
    private void doRegisterBeanDefinition(List<MYBeanDefinition> beanDefinitions) throws Exception {
        for (MYBeanDefinition beanDefinition : beanDefinitions) {
            if (super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The “" + beanDefinition.getFactoryBeanName() + "” is exists!!");
            }
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }
    }

    @Override
    // 依赖注入，从这里开始
    // 1/读取BeanDefinition，通过反射创建Bean实例，包装成BeanWrapper，并放入IOC容器
    // 2.对IOC容器管理的Bean进行依赖注入
    /**
     * 调用getBean的时机
     * 1.DispatchServlet创建 IOC容器：refresh --> doAutowired
     * 2.DispatchServlet创建HandlerMapping，要拿出所有Bean
     * 3.手动getBean
     */
    /** 关于循环注入：
     *  class A{ B b; }
     *  class B{ A a;  }
     *  因此要分成初始化与注入两个方法，即先将对象创建出来，然后再将依赖注入
     **/
    public Object getBean(String beanName) throws Exception {

        MYBeanDefinition myBeanDefinition = this.beanDefinitionMap.get(beanName);
        Object instance = null;

        // 创建事件处理器
        MYBeanPostProcessor beanPostProcessor = new MYBeanPostProcessor();
        // 在创建bean之前进行一些动作
        beanPostProcessor.postProcessBeforeInitialization(instance, beanName);

        // 判断是否是Spring管理的对象
        // 在创建BeanDefinition时，不是Spring管理的就没有BeanDefinition(这里是通过Properties配置scanPackage不是注解）
        if (myBeanDefinition == null) {
            throw new Exception("This Bean not exists!");
        }

        // 1.初始化
        // 注：所有Bean实例都要封装成BeanWrapper，然后在BeanWrapper中再取出Bean实例
        MYBeanWrapper beanWrapper = instantiteBean(beanName, myBeanDefinition);



        // 2.将拿到的BeanWrapper放入IOC容器
        this.factoryBeanInstanceCache.put(beanName, beanWrapper);

        // 在创建bean之后进行一些动作
        beanPostProcessor.postProcessAfterInitialization(instance, beanName);

        // 3.注入
        populateBean(beanName, new MYBeanDefinition(), beanWrapper);

        Object o = this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();
        // 注：即使是单例模式有单例IOC容器，但获取Instance也要先封装为Wrapper，然后再在通用容器中取
        return this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();
    }

    /**
     * 对IOC中的Bean进行依赖注入
     *   1.byName：指定 beanName（首字母小写，接口，自定义） -----> @Resource时
     *   2. byType：类型注入（默认）  ----> @Autowired
     */
    private void populateBean(String beanName, MYBeanDefinition myBeanDefinition, MYBeanWrapper myBeanWrapper) {
        Class<?> clazz = myBeanWrapper.getWrappedClass();
        // 只有容器管理的bean才会给他依赖注入
        if (! clazz.isAnnotationPresent(MYController.class ) || clazz.isAnnotationPresent(MYService.class)) { return; }

        Object instance = myBeanWrapper.getWrappedInstance();
        // 注：这里是getDeclaredFields，getFields只能获取到public字段
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(MYAutowired.class)) { continue; }

            MYAutowired annotation = field.getAnnotation(MYAutowired.class);
            String autowiredBeanName = annotation.value().trim();

            // 没有自定义的话，通过类型进行注入
            if ("".equals(autowiredBeanName)) {
                // 除了simpleName，通过class拿到的都是全类名
                // 前面初始化时已经用className向容器中注入过wrapper了
                autowiredBeanName = field.getType().getName();
            }

            field.setAccessible(true);

            try {
                // 因为要给当前Bean注入时，可能要注入的Bean还没初始化，因此就暂时不给这个字段注入
                // 但是当正式使用时还会getBean一次，这时所有bean都初始化完成了，就可以注入了
                if(this.factoryBeanInstanceCache.get(autowiredBeanName) == null){ continue; }

                // 获取具体Bean实例：这里是在通用IOC容器中获取，因为可能有多例情况
                field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private MYBeanWrapper instantiteBean(String beanName, MYBeanDefinition myBeanDefinition) {
        // 1.拿到要实例化的类名
        String className = myBeanDefinition.getBeanClassName();
        // 2.通过反射进行实例化
        Object instance = null;
        try {
            // 假设所有对象都是单例的，即都要通过单例IOC容器中获取Bean
            if (this.factoryBeanObjectCache.containsKey(className)) {
                instance = this.factoryBeanObjectCache.get(className);
            } else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();

                //-------------------------AOP部分入口代码-----------------------
                // 读取配置文件中的信息，并以当前对象信息构建一个AdvisedSupport对象
                MYAdvisedSupport config = instantionAopConfig(myBeanDefinition);
                config.setTargetClass(clazz);
                config.setTarget(instance);
                // 符合PointCut的规则的话（即符合被代理的对象要求），创建将代理对象
                // 然后用代理对象替换当前对象，并放入IOC容器，
                // 到时 mvc 初始化IOC容器时，就会将代理对象放入，再后来创建处理请求的handler时就会将该代理对象封装进去
                if(config.pointCutMatch()) {
                    // 这时获取到的 Proxy 持有的AdvisedSupport已经构造好了拦截器链
                    // 到时 mvc 分发请求过来直接 proceed 执行即可
                    instance = createProxy(config).getProxy();
                }
                //----------------------------------------------------------------

                this.factoryBeanObjectCache.put(myBeanDefinition.getFactoryBeanName(), instance);
                // 注：这里还要根据className（全类名）放一个
                // 因为在通过类型进行getBean时，BeanDefinition只封装了接口做factoryName
                this.factoryBeanObjectCache.put(className, instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 3.封装BeanWrapper
        // 注：无论单例多例，都要先封装成BeanWrapper
        MYBeanWrapper beanWrapper = new MYBeanWrapper(instance);
        return beanWrapper;
    }

    // 创建代理对象
    private MYAopProxy createProxy(MYAdvisedSupport config) {
        Class<?> targetClass = config.getTargetClass();
        if (targetClass.getInterfaces().length > 0) {
            return new MYJdkDynamicAopProxy(config);
        }
        return new MYCglibAopProxy();
    }

    // 读取配置文件信息，封装成AopCofig后交给AopSupport去处理
    private MYAdvisedSupport instantionAopConfig(MYBeanDefinition myBeanDefinition) {
        MYAopConfig config = new MYAopConfig();
        config.setPointCut(this.reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(this.reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(this.reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(this.reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(this.reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(this.reader.getConfig().getProperty("aspectAfterThrowingName"));

        return new MYAdvisedSupport(config);
    }

    @Override
    public Object getBean(Class<?> beanClass) throws Exception {
        return null;
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public int getBeanDefinitonCount() {
        return this.beanDefinitionMap.size();
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }
}
