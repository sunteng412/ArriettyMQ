package com.mrfox.arrirtty.remoting.handler;

import com.mrfox.arrirtty.common.constants.Remoting2MessageExtInnerMapping;
import com.mrfox.arrirtty.remoting.model.RemoteModel;
import com.mrfox.arrirtty.remoting.model.RemoteActionEnum;
import com.mrfox.arrirtty.remoting.model.RemoteTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/***********************
 * @author MrFox
 * @date 2020/5/14 23:04
 * @version 1.0
 * @description
 ************************/
public class ClientServerHandler extends SimpleChannelInboundHandler<RemoteModel> {

    /**********************
     * 请求处理
     * @param
     * @return
     * @description //TODO
     * @date 23:04 2020/5/14
    **********************/
    protected void channelRead0(ChannelHandlerContext ctx, RemoteModel msg) throws Exception {
        System.out.println(ctx.channel().remoteAddress());
        System.out.println("client output:"+msg);
        ctx.writeAndFlush("from client:"+ LocalDateTime.now());
    }

    /**********************
     * 捕获异常
     * @param
     * @return
     * @description //TODO
     * @date 23:06 2020/5/14
    **********************/
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.channel().close();
    }

    /**********************
     * 处于活动状态
     * @param
     * @return
     * @description //TODO
     * @date 23:12 2020/5/14
    **********************/
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        RemoteModel remoteModel = new RemoteModel();
        remoteModel.setContent("你好".getBytes());
        remoteModel.setRemoteActionEnum(RemoteActionEnum.MSG_REQ);
        remoteModel.setRemoteTypeEnum(RemoteTypeEnum.ASYNC);
        remoteModel.setBornTimestamp(System.currentTimeMillis());

        Map<String,String> map = new HashMap<>();
        map.put(Remoting2MessageExtInnerMapping.Remoting2MessageExtInnerMappingEnum.TAGS.m2(),"tag1");
        map.put(Remoting2MessageExtInnerMapping.Remoting2MessageExtInnerMappingEnum.TOPIC.m2(),"topic1");
        map.put(Remoting2MessageExtInnerMapping.Remoting2MessageExtInnerMappingEnum.QUEUE_ID.m2(),"1");
        map.put(Remoting2MessageExtInnerMapping.Remoting2MessageExtInnerMappingEnum.RECONSUME_TIMES.m2(),"0");
        map.put("我是自定义属性1","111");
        map.put("wxs","222");

        remoteModel.setExtFields(map);

        //创建一个byteBuf
        ctx.writeAndFlush(remoteModel);
    }
}
