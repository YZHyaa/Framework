package com.yzh.tomcat.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * Request本质就是InputStream
 * 注：在Tomcat类的while（true）--> process()可以看到，每一次请求都会构造一个InputStream，然后再封装成Request
 */
public class MYRequest {

    private String method;
    private String url;

    public MYRequest(InputStream in) {
        try {
            // content用来保存InputStream中的http请求信息
            String content = "";
            byte[] buff = new byte[1024];
            int len = 0;
            if ((len = in.read(buff)) > 0) {
                content = new String(buff, 0, len);
            }
            //System.out.println(content);

            // 对http请求信息进行处理，得到Method与Url
            String line = content.split("\\n")[0];
            String[] arr = line.split("\\s");
            this.method = arr[0];
            this.url = arr[1].split("\\?")[0];

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return this.url;
    }

    public String getMethod() {
        return this.method;
    }
}
