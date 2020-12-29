package com.xupt.yzh.tomcat;

import com.xupt.yzh.tomcat.http.MYRequest;
import com.xupt.yzh.tomcat.http.MYResponse;
import com.xupt.yzh.tomcat.http.MYServlet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.io.FileInputStream;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MYTomcat {

    private int port = 8080;
    private ServerSocket server;
    // 用来保存路径与Servlet的映射关系（servlet单例模式）
    private Map<String, MYServlet> servletMapping = new HashMap<>();
    // web.xml
    private Properties webxml = new Properties();


    // 加载web.xml文件,同时初始化 ServletMapping对象
    private void init(){

        try{
            String WEB_INF = this.getClass().getResource("/").getPath();
            FileInputStream fis = new FileInputStream(WEB_INF + "web.properties");

            webxml.load(fis);

            for (Object k : webxml.keySet()) {

                String key = k.toString();
                // 以url结尾的key就是要映射的路径
                if(key.endsWith(".url")){
                    String servletName = key.replaceAll("\\.url$", "");
                    String url = webxml.getProperty(key);

                    // 拿到对应servlet全类名后，通过反进行实例化，之后放入map中（单例模式）
                    // 注：这里是将所有Servlet都强转为MyServlet，所以一定要继承MyServlet
                    String className = webxml.getProperty(servletName + ".className");
                    MYServlet obj = (MYServlet)Class.forName(className).newInstance();
                    servletMapping.put(url, obj);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 启动tomcat
     * 1. 调用init方法，加载web.xml
     * 2.等待用户请求，并对每个请求进行处理
     */
    public void start() {
        init();

        // netty封装了nio，Reactor模型，Boss，worker
        // Boss线程
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        // Worket线程
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // Netty服务
            ServerBootstrap server = new ServerBootstrap();
            // 链路式编程
            server.group(bossGroup, workerGroup)
                    // 主线程处理类,看到这样的写法，底层就是用反射
                    .channel(NioServerSocketChannel.class)
                    // 子线程处理类 , Handler
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // 客户端初始化处理
                        protected void initChannel(SocketChannel client) throws Exception {
                            // 无锁化串行编程
                            // Netty对HTTP协议的封装，顺序有要求
                            // HttpResponseEncoder 编码器
                            client.pipeline().addLast(new HttpResponseEncoder());
                            // HttpRequestDecoder 解码器
                            client.pipeline().addLast(new HttpRequestDecoder());
                            // 业务逻辑处理
                            client.pipeline().addLast(new MYTomcatHandler());
                        }

                    })
                    // 针对主线程的配置 分配线程最大数量 128
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 针对子线程的配置 保持长连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 启动服务器
            ChannelFuture f = server.bind(port).sync();
            System.out.println("MYTomcat 已启动，监听的端口是：" + port);
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class MYTomcatHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpRequest) {
                HttpRequest req = (HttpRequest) msg;

                // 转交给我们自己的request实现
                MYRequest request = new MYRequest(ctx, req);
                // 转交给我们自己的response实现
                MYResponse response = new MYResponse(ctx, req);
                // 实际业务处理
                String url = request.getUrl();

                if (servletMapping.containsKey(url)) {
                    servletMapping.get(url).service(request, response);
                } else {
                    response.write("404 - Not Found");
                }

            }
        }
    }

    public static void main(String[] args) {
        new MYTomcat().start();
    }
}
