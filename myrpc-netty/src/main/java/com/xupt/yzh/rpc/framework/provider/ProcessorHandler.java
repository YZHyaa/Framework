package com.xupt.yzh.rpc.framework.provider;

import com.xupt.yzh.rpc.framework.annotation.RpcService;
import com.xupt.yzh.rpc.framework.protocol.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessorHandler extends SimpleChannelInboundHandler<RpcRequest> {

    // 扫描目标包后所有 Java 文件的全类名
    private List<String> classNames = new ArrayList<>();
    // 有 @RpcService 注解的服务bean的（serviceName，instance）
    private Map<String, Object> registryMap = new ConcurrentHashMap<>();

    public ProcessorHandler() {
        try {
            scannerClass("com.xupt.yzh.provider");
            doRegistry();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scannerClass(String packageName) {
        // 根据包名获取绝对路径
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File classpath = new File(url.getFile());

        for (File file : classpath.listFiles()) {
            if (file.isDirectory()) {
                scannerClass(packageName + "." + file.getName());
            } else {
                // 将扫描到的类的全类名放入 classNames
                classNames.add(packageName + "." + file.getName().replace(".class", ""));
            }
        }
    }

    private void doRegistry() throws Exception {
        if (classNames.isEmpty()) {
            return;
        }

        for (String className : classNames) {
            Class<?> clazz = Class.forName(className);

            RpcService annotation = clazz.getAnnotation(RpcService.class);
            if (annotation != null) {

                // 注：如果实现了多个接口，只取第一个
                Class<?> i = clazz.getInterfaces()[0];
                String serviceName = i.getName();

                // 注册
                registryMap.put(serviceName, clazz.newInstance());
            }
        }
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        if (registryMap.containsKey(request.getClassName())) {
            Object service = registryMap.get(request.getClassName());

            // 根据实参获取方法的形参
            // 注：获取形参列表后才能确定一个方法
            Object[] args = request.getParameters();
            Class<?>[] types = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                types[i] = args[i].getClass();
            }

            // 获取 Method
            Method method = service.getClass().getMethod(request.getMethodName(), types);

            // 执行方法
            Object res = method.invoke(service, args);

            ctx.writeAndFlush(res);
            ctx.close();
        }
    }
}
