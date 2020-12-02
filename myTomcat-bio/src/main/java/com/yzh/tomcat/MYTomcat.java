package com.yzh.tomcat;

import com.yzh.tomcat.http.MYRequest;
import com.yzh.tomcat.http.MYResponse;
import com.yzh.tomcat.http.MYServlet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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

        try {
            server = new ServerSocket(this.port);
            System.out.println("MYTomcat已启动，监听的端口是" + this.port);

            // 用一个死循环来等待用户请求
            while (true) {
                Socket client = server.accept();
                process(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 具体处理请求
     * 1.创建IO流，并包装成Request与Response
     * 2.获取请求Url，取出对应Servlet进行处理
     * @param client
     * @throws IOException
     */
    private void process(Socket client) throws IOException {
        // 1.获取IO流，并封装成Request与Response
        InputStream is = client.getInputStream();
        OutputStream os = client.getOutputStream();

        MYRequest request = new MYRequest(is);
        MYResponse response = new MYResponse(os);

        // 2.获取请求URL，寻找相应Servlet进行处理
        String url = request.getUrl();

        if (servletMapping.containsKey(url)) {
            // 调用service方法
            servletMapping.get(url).service(request, response);
        } else {
            response.write("404 - Not Found");
        }

        os.flush();
        os.close();
        is.close();
        client.close();
    }

    public static void main(String[] args) {
        new MYTomcat().start();
    }
}
