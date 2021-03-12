package com.xiaoyi.bio;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author 小逸
 * @description
 * @time 2021/3/12 17:05
 */
public class SocketClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 9002);
        // 向服务器发送数据
        socket.getOutputStream().write("Hello Server".getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().flush();
        System.out.println("向服务端发送数据结束");

        byte[] bytes = new byte[1024];
        // 接收服务器数据
        socket.getInputStream().read(bytes);
        System.out.println("接收到服务端的数据：" + new String(bytes));
        socket.close();
    }
}
