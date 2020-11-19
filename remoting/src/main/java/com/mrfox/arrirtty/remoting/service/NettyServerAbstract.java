package com.mrfox.arrirtty.remoting.service;

import com.mrfox.arrirtty.common.model.Pair;
import com.mrfox.arrirtty.remoting.model.RemoteModel;
import com.mrfox.arrirtty.remoting.model.RemoteActionEnum;
import com.mrfox.arrirtty.remoting.model.RemoteTypeEnum;
import com.mrfox.arrirtty.common.constants.RemotingSysResponseCode;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/***********************
 * @author MrFox
 * @date 2020/9/6 18:05
 * @version 1.0
 * @description
 ************************/
@Slf4j
public abstract class NettyServerAbstract {
    private Map<RemoteActionEnum, Pair<NettyRequestProcessor, ExecutorService>> dispatchMap = new ConcurrentHashMap<>(64);

    /*****
     * 注册处理器
     * @param
     * @return
     * @description:
     ****/
    public void registryProcessor(RemoteActionEnum remoteActionEnum, NettyRequestProcessor nettyRequestProcessor, ExecutorService executorService) {
        //装入处理
        dispatchMap.put(remoteActionEnum, new Pair<>(nettyRequestProcessor, executorService));
    }


    /**********************
     * 真正的处理逻辑
     * @param ctx 管道处理上下文
     * @param msg 消息
     * @description //TODO
     * @date 18:24 2020/9/6
     **********************/
    public void processMessageReceived(ChannelHandlerContext ctx, RemoteModel msg) throws Exception {
        if(Objects.isNull(msg.getRemoteActionEnum())){
            log.error("[NettyServerAbstract#processMessageReceived]不存在远程类型枚举,消息格式错误");
        }
        //首先根据Code去拿对应的处理
        Pair<NettyRequestProcessor, ExecutorService> servicePair = dispatchMap.get(msg.getRemoteActionEnum());

        if(Objects.isNull(servicePair)){
            log.error("[BrokerNettyServer]未找到合适的处理器");
            return;
        }

        servicePair.getObject2().submit(()->{
            RemoteModel remoteModel = servicePair.getObject1().processRequest(ctx, msg);
            try {
                if(!msg.getRemoteTypeEnum().equals(RemoteTypeEnum.ONE_WAY)){
                    ctx.writeAndFlush(remoteModel);
                }
            }catch (Exception e){
                if(!msg.getRemoteTypeEnum().equals(RemoteTypeEnum.ONE_WAY)){
                    ctx.writeAndFlush(RemoteModel.buildGeneralResponse(RemotingSysResponseCode.SYSTEM_ERROR));
                }
                log.error("[processMessageReceived]消息返回错误",e);
            }
        });
    }
}
