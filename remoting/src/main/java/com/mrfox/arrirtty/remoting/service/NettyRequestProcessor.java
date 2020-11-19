
package com.mrfox.arrirtty.remoting.service;

import com.mrfox.arrirtty.remoting.model.RemoteModel;
import io.netty.channel.ChannelHandlerContext;

/*****
 * 处理请求服务
 * @author     : MrFox
 * @date       : 2020-09-04 16:28
 * @description:
 * @version    :
 ****/
public interface NettyRequestProcessor {

    /*****
     * 处理逻辑
     * @param   ctx channel处理器上下文
     * @param request 请求
     * * @return  {@link RemoteModel}
     * @description:
     ****/
    RemoteModel processRequest(ChannelHandlerContext ctx, RemoteModel request);

    /*****
     * 判断是否可以处理
     * @return {@link Boolean}
     * @description:
     ****/
    Boolean rejectRequest();
}
