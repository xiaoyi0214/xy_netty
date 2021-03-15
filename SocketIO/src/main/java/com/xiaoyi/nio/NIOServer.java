package com.xiaoyi.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author 小逸
 * @description
 * @time 2021/3/12 17:42
 */
public class NIOServer {
    public static void main(String[] args) throws IOException {
        // 创建一个在本地端口上监听的服务Socket通道，并设置为非阻塞方式
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        // 必须配置为非阻塞才能往selector上注册，负责会报错。selector本身是非阻塞方式的
        socketChannel.configureBlocking(false);
        socketChannel.socket().bind(new InetSocketAddress(9002));
        // 创建一个选择器selector
        Selector selector = Selector.open();
        // 把ServerSocketChannel注册到selector上，并且selector对客户端accept连接操作感兴趣
        socketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true){
            System.out.println("等待连接发生。。。");
            // 轮询监听channel里的key，select是阻塞的，accept也是阻塞的
            selector.select(); //timeout：到了一定时间后，就算没有连接也会轮询一次select
            System.out.println("有连接到来。。。");
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                // 删除本次已处理的key，防止下次select重复处理
                iterator.remove();
                handle(selectionKey);
            }
        }
    }

    private static void handle(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isAcceptable()){
            System.out.println("有客户端连接事件发生");
            ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
            //NIO非阻塞体现：此处accept方法是阻塞的，但是这里因为是发生了连接事件，所以这个方法会马上执行完，不会阻塞
            //处理完连接请求不会继续等待客户端的数据发送
            SocketChannel accept = channel.accept();
            accept.configureBlocking(false);
            // 通过selector监听channel对读事件感兴趣
            accept.register(selectionKey.selector(),SelectionKey.OP_READ);
        }else if (selectionKey.isReadable()){
            System.out.println("有客户端可读事件发生");
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            //NIO非阻塞体现:首先read方法不会阻塞，其次这种事件响应模型，当调用到read方法时肯定是发生了客户端发送数据的事件
            int len = channel.read(buffer);
            if (len != -1) {
                System.out.println("读取到客户端发送的数据：" + new String(buffer.array(), 0, len));
            }
            ByteBuffer bufferToWrite = ByteBuffer.wrap("HelloClient".getBytes());
            channel.write(bufferToWrite);
            selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }else if (selectionKey.isWritable()){
            SocketChannel sc = (SocketChannel) selectionKey.channel();
            System.out.println("write事件");
            // NIO事件触发是水平触发
            // 使用Java的NIO编程的时候，在没有数据可以往外写的时候要取消写事件，
            // 在有数据往外写的时候再注册写事件
            selectionKey.interestOps(SelectionKey.OP_READ);
            //sc.close();
        }
    }
}
