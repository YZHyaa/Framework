package com.yzh.tomcat.http;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Response本质就是OutputStream
 */
public class MYResponse {

    private OutputStream out;

    public MYResponse(OutputStream out) {
        this.out = out;
    }

    /**
     * 因为写出的内容要被http协议解析，所以要符合http协议规范，有其要求的响应头
     * @param s
     * @throws IOException
     */
    public void write(String s) throws IOException {
        StringBuilder sb = new StringBuilder();
        // 为了符合Http协议，要给出状态码和响应格式
        sb.append("HTTP/1.1 200 OK\n")
                .append("Content-Type: text/html;\n")
                .append("\r\n")
                .append(s);
        out.write(sb.toString().getBytes());
    }
}
