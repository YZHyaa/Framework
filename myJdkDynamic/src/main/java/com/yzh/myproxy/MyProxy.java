package com.yzh.myproxy;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 相当于生成代理对象的工具类（写->创建）
 */
public class MyProxy {

    // 换行符
    private static final String ln = "\r\n";

    public static Object newProxyInstance(MyClassLoader classLoader,
                                          Class<?>[] interfaces,
                                          MyInvocationHandler h) {
        try {

            // 1.生成动态代理类的代码
            String src = generateSrc(interfaces);

            // 2.将代码写入 .java 文件（target 目录下）
            // 获取当前类的位置，绝对路径
            String filePath = MyProxy.class.getResource("").getPath();
            // 创建一个新的 .java 文件
            // 注：代理类的编号依次从 0 递增，这里是直接写死了
            File f = new File(filePath + "$Proxy0.java");
            // 通过 FileWriter 写出到 .java 文件
            FileWriter fw = new FileWriter(f);
            fw.write(src);
            fw.flush();
            fw.close();

            // 3.将.java 文件编译成.class文件
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager manage = compiler.getStandardFileManager(null,null,null);
            Iterable iterable = manage.getJavaFileObjects(f);

            JavaCompiler.CompilationTask task = compiler.getTask(null,manage,null,null,null,iterable);
            task.call();
            manage.close();

            // 4.将编译生成的.class文件加载到JVM中
            // 注：加载到 JVM 即生成相应 Class 对象
            Class proxyClass =  classLoader.findClass("$Proxy0");

            // 5.生成代理对象实例
            // 获取当前代理类的构造器（入参是 InvocationHandler）
            Constructor c = proxyClass.getConstructor(MyInvocationHandler.class);
            f.delete(); // 将 java 文件删除
            // 入参是 Proxy.newProxyInstance() 传入的 InvocationHandler 实例
            return c.newInstance(h);

        }catch (Exception e){
            e.printStackTrace();
        }
            return null;
    }

    // 生成代理类的 java 代码
    // 本质就是拿代码写代码
    private static String generateSrc(Class<?>[] interfaces) {

        StringBuilder sb = new StringBuilder();

        sb.append("package com.yzh.myproxy;" + ln);
        // 反射相关依赖
        sb.append("import java.lang.reflect.*;" + ln);
        // 实现的接口也要引入
        // 为了方便这里直接写死了
        sb.append("import com.yzh.demo.Person;" + ln);


        sb.append("public class $Proxy0 implements " + interfaces[0].getName() + "{" + ln);

            sb.append("MyInvocationHandler h;" + ln);
            // 代理类的构造器，入参是 InvocationHandler
            sb.append("public $Proxy0(MyInvocationHandler h) {" + ln);
                sb.append("this.h = h;" + ln);
            sb.append("}" + ln);

            /**
             * 生成接口中的所有方法
             *
             * public 返回值类型 方法名(参数类型 参数名) {
             *     Method m = 当前类.class。getMethod(方法名, new Class[]{参数类型数组});
             *     return h.invoke(this, m, new Object[]{参数数组});
             * }
             */
             for (Method m : interfaces[0].getMethods()) {
                 // 获取当前方法所有参数的类型
                 Class<?>[] params = m.getParameterTypes();

                 // 参数类型 参数名（，拼接）
                 StringBuilder paramNames = new StringBuilder();
                 // 参数名（，拼接）
                 StringBuilder paramValues = new StringBuilder();
                 // 参数类型（，拼接）
                 StringBuilder paramClasses = new StringBuilder();

                 for (int i = 0; i < params.length; i++) {
                     Class clazz = params[i];
                     String type = clazz.getName();
                     // 参数名 = 参数类型首字母小写
                     String paramName = toLowerFirstCase(clazz.getSimpleName());

                     paramNames.append(type + " " + paramName);
                     paramValues.append(paramName);
                     paramClasses.append(clazz.getName() + ".class");

                     // 逗号拼接
                     if (i > 0 && i < params.length - 1) {
                         paramNames.append(",");
                         paramClasses.append(",");
                         paramValues.append(",");
                     }
                 }

                 // public 返回值类型 方法名(参数类型 参数名)
                 sb.append("public " + m.getReturnType().getName() + " " + m.getName() + "(" + paramNames.toString() + ") {" + ln);
                    sb.append("try{" + ln);
                        // Method m = 当前类.class。getMethod(方法名, new Class[]{参数类型数组});
                        sb.append("Method m = " + interfaces[0].getName() + ".class.getMethod(\"" + m.getName() + "\",new Class[]{" + paramClasses.toString() + "});" + ln);
                        // return h.invoke(this, m, new Object[]{参数数组});
                        // 注：如果返回类型为 void，则不需要 return，直接调用 invoke() 就行
                        sb.append((hasReturnValue(m.getReturnType()) ? "return " : "") + getCaseCode("this.h.invoke(this,m,new Object[]{" + paramValues + "})",m.getReturnType()) + ";" + ln);
                    sb.append("}catch(Error _ex) {" + ln + "}");
                    sb.append("catch(Throwable e){" + ln);
                    sb.append("throw new UndeclaredThrowableException(e);" + ln);
                    sb.append("}");
                    // 如果 try 出现异常了，且 catch 未抛出异常
                    // 且当前方法有返回值
                    // 则需要 return 一个值，比如 return 0，return null
                    sb.append(getReturnEmptyCode(m.getReturnType()));
                 sb.append("}");
             }
        sb.append("}" + ln);
        return sb.toString();
    }

    // 字符串首字母转小写
    private static String toLowerFirstCase(String src){
        char [] chars = src.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private static Map<Class,Class> mappings = new HashMap<>();
    static {
        mappings.put(int.class,Integer.class);
    }

    private static String getReturnEmptyCode(Class<?> returnClass){
        // 如果返回类型为 int，则 return 0;
        if(mappings.containsKey(returnClass)){
            return "return 0;";
        // 如果返回类型为 void，则直接 return;
        }else if(returnClass == void.class){
            return "";
        // 如果返回类型为其他（比如 Object），则 return null;
        }else {
            return "return null;";
        }
    }

    private static String getCaseCode(String code,Class<?> returnClass){
        if(mappings.containsKey(returnClass)){
            return "((" + mappings.get(returnClass).getName() +  ")" + code + ")." + returnClass.getSimpleName() + "Value()";
        }
        return code;
    }

    private static boolean hasReturnValue(Class<?> clazz){
        return clazz != void.class;
    }
}
