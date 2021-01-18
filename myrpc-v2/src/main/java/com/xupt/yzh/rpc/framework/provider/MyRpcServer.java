package com.xupt.yzh.rpc.framework.provider;

import com.xupt.yzh.rpc.framework.annotation.RpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
/**
 * 引入Spring的意义就在于对Bean的管理（生老病死）更灵活，这里就是对服务Bean的管理更灵活
 */
public class MyRpcServer implements ApplicationContextAware, InitializingBean {

    ExecutorService executorServic = Executors.newCachedThreadPool();

    // 存放接口名（服务名）与服务Bean
    private Map<String, Object> handlerMap = new HashMap<>();

    private int port;

    public MyRpcServer(int port) {
        this.port = port;
    }

    @Override
    /**
     * ApplicationContextAware-setApplicationContext: Bean初始化时执行，可以获取到所有Bean
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 通过注解拿到服务Bean
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (!serviceBeanMap.isEmpty()) {
            for (Object serviceBean : serviceBeanMap.values()) {
                RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
                // 拿到服务名（接口名）
                String serviceName = rpcService.value().getName();
                // 放入容器
                handlerMap.put(serviceName, serviceBean);
            }
        }
    }

    @Override
    /**
     * InitializingBean-afterPropertiesSet：在Bean初始化时执行，setApplicationContext之后
     */
    public void afterPropertiesSet() throws Exception {

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                // 这里是将handlerMap传入给具体的处理器，让处理器在其中拿到具体的服务Bean
                // 注：这里就不能像v1直接传入一个固定的service，而是要动态获取
                executorServic.execute(new ProcessorHandler(socket, handlerMap));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
