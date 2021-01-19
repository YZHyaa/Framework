package com.xupt.yzh.rpc.framework.consumer;

import com.xupt.yzh.rpc.framework.protocol.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;


public class RpcNetTransport {

    String host;
    int port;

    public RpcNetTransport(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Object send(RpcRequest rpcRequest) throws InterruptedException {

        NioEventLoopGroup wokergroup = new NioEventLoopGroup();

        TransportHandler transportHandler = new TransportHandler();

        Bootstrap client = new Bootstrap();
        client.group(wokergroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();


                        /** LengthFieldBasedFrameDecoder入参有5个，分别解释如下
                         maxFrameLength：框架的最大长度。如果帧的长度大于此值，则将抛出TooLongFrameException。
                         lengthFieldOffset：长度字段的偏移量：即对应的长度字段在整个消息数据中得位置
                         lengthFieldLength：长度字段的长度。如：长度字段是int型表示，那么这个值就是4（long型就是8）
                         lengthAdjustment：要添加到长度字段值的补偿值
                         initialBytesToStrip：从解码帧中去除的第一个字节数
                         */
                        // 1.自定义协议解码器 -----> 完整接收对象，还原InvokerProtocol
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                        // 2.自定义协议编码器 -----> 完整接收对象，还原InvokerProtocol
                        pipeline.addLast(new LengthFieldPrepender(4));
                        // 3.对象参数类型编码器 -----> 反序列化成java能识别的，还原parames（class[])和values(object[])
                        pipeline.addLast("encoder",new ObjectEncoder());
                        // 4.对象参数类型解码器 -----> 反序列化成java能识别的，还原parames（class[])和values(object[])
                        pipeline.addLast("decoder",new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));

                        // 前面是做数据解析，此处真正执行具体的处理逻辑
                        pipeline.addLast(transportHandler);
                    }
                });

        // 建立连接
        ChannelFuture future = client.connect(host, port).sync();
        // 发送请求信息
        future.channel().writeAndFlush(rpcRequest);
        future.channel().closeFuture().sync();

        // 返回请求信息
        return transportHandler.getResponse();
    }

    static class TransportHandler extends ChannelInboundHandlerAdapter {

        private Object response;

        // 接收到消息时触发该回调
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            response = msg;
        }

        public Object getResponse() {
            return response;
        }
    }
}
