package com.mrfox.arrirtty.remoting;

import com.mrfox.arrirtty.remoting.handler.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*****
 * broker的netty服务,负责传输
 * @author     : MrFox
 * @date       : 2020-09-02 11:06
 * @description:
 * @version    :
 ****/
@Slf4j
@Data
public class ClientNettyServer {

    /**
     * netty启动器
     * */
    private final Bootstrap clientBootstrap;

    /**
     * boss
     * */
    private final EventLoopGroup eventLoopGroupBoss;

    public ClientNettyServer() {
        clientBootstrap = new Bootstrap();
        this.eventLoopGroupBoss = new NioEventLoopGroup(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyBoss_%d", this.threadIndex.incrementAndGet()));
            }
        });
    }


    public ClientNettyServer start(){
        //创建
        clientBootstrap.group(eventLoopGroupBoss)
                //设置发送内核缓冲区
                .option(ChannelOption.SO_SNDBUF,65536)
                //设置接收内核缓冲区
                .option(ChannelOption.SO_RCVBUF, 65536)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        //byte按长度转为RemoteModel-接收
                        pipeline.addLast(new NettyByte2RemoteModelHandler(Integer.MAX_VALUE,
                                0, 4, 0, 4))
                                //发送前自动带上长度-发送
                                .addLast(new LengthFieldPrepender(4))
                                //RemoteModel转byte-发送
                                .addLast(new NettyRemoteModel2ByteHandler())
                                // 心跳监测机制 + 自动下线(暂时默认120秒)
                                .addLast(new IdleStateHandler(0, 0, 120, TimeUnit.SECONDS))
                                //连接状态管理
                                .addLast(new NettyConnectManageHandler())
                                //业务处理
                                .addLast(new ClientServerHandler());
                    }
                });

        //TODO:写死 配置连接
        ChannelFuture channelFuture = clientBootstrap.connect("localhost", 8888);
        log.info("[ClientNettyServer#start]连接成功[host:{}],port:[{}]","localhost",8888);
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }



}
