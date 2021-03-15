package com.xiaoyi.netty.chat_owner;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 小逸
 * @description
 * @time 2021/3/15 9:13
 */
public class ChatOwnerServerHandler extends SimpleChannelInboundHandler<String> {

    //GlobalEventExecutor.INSTANCE是全局的事件执行器，是一个单例
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    //表示 channel 处于就绪状态, 提示上线
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelGroup.writeAndFlush("【客户端】"+channel.remoteAddress()+" 上线了 "+sdf.format(new Date()));
        channelGroup.add(channel);
        System.out.println(channel.remoteAddress() + "上线了 \n");
    }

    // channel 不活动状态，下线
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelGroup.writeAndFlush("【客户端】"+channel.remoteAddress()+" 下线了 "+sdf.format(new Date()));
        System.out.println(ctx.channel().remoteAddress() + " 下线了"+ "\n");
        System.out.println("channelGroup.size():"+channelGroup.size());
        channelGroup.remove(channel);
        System.out.println("channelGroup.size():"+channelGroup.size());

    }

    //读取数据
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel channel = ctx.channel();
        channelGroup.forEach(ch->{
            if (channel != ch){
                ch.writeAndFlush("【客户端】"+channel.remoteAddress()+" 发送了消息："+msg +"\n");
            }else {
                ch.writeAndFlush("【自己】发送了消息："+msg+"\n");
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
