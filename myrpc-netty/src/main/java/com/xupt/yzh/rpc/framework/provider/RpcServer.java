package com.xupt.yzh.rpc.framework.provider;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class  RpcServer{

    // 初始化主线程池，Selector
    NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    // 初始化子线程池，对应具体客户端处理逻辑
    NioEventLoopGroup workerGrop = new  NioEventLoopGroup();

    public void publisher(int port) throws InterruptedException {

        ServerBootstrap server = new ServerBootstrap();
        server.group(bossGroup, workerGrop)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();

                        // 1.自定义协议解码器 -----> 完整接收对象，还原InvokerProtocol
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                        // 2.自定义协议编码器 -----> 完整接收对象，还原InvokerProtocol
                        pipeline.addLast(new LengthFieldPrepender(4));
                        // 3.对象参数类型编码器 -----> 反序列化成java能识别的，还原parames（class[])和values(object[])
                        pipeline.addLast("encoder",new ObjectEncoder());
                        // 4.对象参数类型解码器 -----> 反序列化成java能识别的，还原parames（class[])和values(object[])
                        pipeline.addLast("decoder",new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));

                        // 前面是做数据解析，此处真正执行具体的处理逻辑
                        pipeline.addLast(new ProcessorHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128) // 最大SelectionKey数量
                .childOption(ChannelOption.SO_KEEPALIVE, true); // 保证所有子线程是长连接，且可以重复利用

        // 绑定端口，并阻塞
        ChannelFuture future = server.bind(port).sync();
        // 正式启动服务，相当于用一个死循环开始轮训
        future.channel().closeFuture().sync();
    }
}
