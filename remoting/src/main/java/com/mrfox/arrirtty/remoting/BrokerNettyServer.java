package com.mrfox.arrirtty.remoting;

import com.google.common.base.Throwables;
import com.mrfox.arrirtty.remoting.common.RemotingUtil;
import com.mrfox.arrirtty.remoting.handler.NettyByte2RemoteModelHandler;
import com.mrfox.arrirtty.remoting.handler.NettyConnectManageHandler;
import com.mrfox.arrirtty.remoting.handler.NettyRemoteModel2ByteHandler;
import com.mrfox.arrirtty.remoting.model.RemoteModel;
import com.mrfox.arrirtty.remoting.service.NettyServer;
import com.mrfox.arrirtty.remoting.service.NettyServerAbstract;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class BrokerNettyServer extends NettyServerAbstract implements NettyServer {

    /**
     * netty启动器
     */
    private final ServerBootstrap serverBootstrap;

    /**
     * worker
     */
    private final EventLoopGroup eventLoopGroupSelector;

    /**
     * boss
     */
    private final EventLoopGroup eventLoopGroupBoss;

    public BrokerNettyServer() {

        serverBootstrap = new ServerBootstrap();
        this.eventLoopGroupBoss = new NioEventLoopGroup(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyBoss_%d", this.threadIndex.incrementAndGet()));
            }
        });

        if (useEpoll()) {
            this.eventLoopGroupSelector = new EpollEventLoopGroup(3, new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);
                private int threadTotal = 3;

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerEPOLLSelector_%d_%d", threadTotal, this.threadIndex.incrementAndGet()));
                }
            });
        } else {
            this.eventLoopGroupSelector = new NioEventLoopGroup(3, new ThreadFactory() {
                private AtomicInteger threadIndex = new AtomicInteger(0);
                private int threadTotal = 3;

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, String.format("NettyServerNIOSelector_%d_%d", threadTotal, this.threadIndex.incrementAndGet()));
                }
            });
        }

    }


    private boolean useEpoll() {
        return RemotingUtil.isLinuxPlatform()
                && Epoll.isAvailable();
    }


    public Boolean start() {
        //创建
        ServerBootstrap childHandler = this.serverBootstrap.group(eventLoopGroupBoss, eventLoopGroupSelector)
                .localAddress(8888)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                //如果指定handler，则会对bossGroup生效
                .handler(new LoggingHandler(LogLevel.INFO))
                //设置发送内核缓冲区
                .childOption(ChannelOption.SO_SNDBUF, 65536)
                //设置接收内核缓冲区
                .childOption(ChannelOption.SO_RCVBUF, 65536)
                //配置初始化--针对于workerGroup
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //配置Handler
                        socketChannel.pipeline()
                                //byte按长度转为RemoteModel-接收
                                .addLast(new NettyByte2RemoteModelHandler(Integer.MAX_VALUE,
                                        0, 4, 0, 4))
                                //发送前自动带上长度-发送
                                .addLast(new LengthFieldPrepender(4))
                                //RemoteModel转byte-发送
                                .addLast(new NettyRemoteModel2ByteHandler())
                                // 心跳监测机制 + 自动下线
                                .addLast(new IdleStateHandler(0, 0, 120, TimeUnit.SECONDS))
                                //连接状态管理
                                .addLast(new NettyConnectManageHandler())
                                //业务处理
                                .addLast(new BrokerServerHandler());
                    }
                });

        //开启
        try {
            serverBootstrap.bind().sync();
        } catch (InterruptedException e) {
            log.error("[BrokerNettyServer#BrokerNettyServer]netty开启失败,error:{}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException("[BrokerNettyServer#BrokerNettyServer]netty开启失败", e);

        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean shutdown() {
        return null;
    }

    /*****
     * broker处理请求
     * @author     : MrFox
     * @date       : 2020-09-02 17:44
     * @description:
     * @version    :
     ****/
    class BrokerServerHandler extends SimpleChannelInboundHandler<RemoteModel> {
        /**
         * <strong>Please keep in mind that this method will be renamed to
         * {@code messageReceived(ChannelHandlerContext, I)} in 5.0.</strong>
         * <p>
         * Is called for each message of type {@link }.
         *
         * @param ctx         the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
         *                    belongs to
         * @param remoteModel the message to handle
         * @throws Exception is thrown if an error occurred
         */
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RemoteModel remoteModel) throws Exception {
            log.info("[BrokerServerHandler]收到来自[{}]消息,内容:[{}]",
                    RemotingUtil.socketAddress2String(ctx.channel().remoteAddress()), remoteModel.toString());
            //首选去processMap里根据code拿出对应的线程组及service
            processMessageReceived(ctx, remoteModel);
        }
    }





}
