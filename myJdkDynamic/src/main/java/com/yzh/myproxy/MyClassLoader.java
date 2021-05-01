package com.yzh.myproxy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class MyClassLoader extends ClassLoader{

    private File classPathFile;
    public MyClassLoader() {
        // 获取 ClassLoader 的绝对路径
        String classPath = MyClassLoader.class.getResource("").getPath();
        // 拿到 File 对象
        this.classPathFile = new File(classPath);
    }

    @Override
    // 根据类名将指定类加载进 JVM
    // 注：当一个类有 Class 对象了，就表示 JVM 将该类加载进来了
    protected Class<?> findClass(String name) throws ClassNotFoundException {

        // 拼接全类名
        String className = MyClassLoader.class.getPackage().getName() + "." + name;
        if(classPathFile  != null){
            // 根据绝对路径，以及 class 文件名，拿到 class 文件
            File classFile = new File(classPathFile,name.replaceAll("\\.","/") + ".class");
            // 如果 class 文件存在
            if(classFile.exists()){

                // 将 class 文件读入内存，暂存到一个字节数组中
                FileInputStream in = null;
                ByteArrayOutputStream out = null;
                try{
                    in = new FileInputStream(classFile);
                    out = new ByteArrayOutputStream();
                    byte [] buff = new byte[1024];
                    int len;
                    while ((len = in.read(buff)) != -1){
                        out.write(buff,0,len);
                    }

                    // 构造类的 Class 对象
                    // 注：defineClass() 是一个 native 方法
                    return defineClass(className,out.toByteArray(),0,out.size());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
