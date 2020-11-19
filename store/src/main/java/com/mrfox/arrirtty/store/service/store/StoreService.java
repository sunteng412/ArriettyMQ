package com.mrfox.arrirtty.store.service.store;

import com.mrfox.arrirtty.store.model.MessageBatchExtInner;
import com.mrfox.arrirtty.store.model.MessageExtInner;
import com.mrfox.arrirtty.store.model.PutMessageResult;

import java.net.SocketAddress;

/*****
 * 存储服务
 * @author     : MrFox
 * @date       : 2020-09-04 13:57
 * @description:
 * @version    :
 ****/
public interface StoreService {

    /**********************
     * 启动
     * @param
     * @return
     * @description //TODO
     * @date 23:38 2020/9/9
    **********************/
    void start();

    /**********************
     * 下线
     * @param
     * @return
     * @description //TODO
     * @date 23:38 2020/9/9
     **********************/
    void shutdown();

    /**********************
     * 加载消息模块
     * @param
     * @return
     * @description //TODO
     * @date 23:39 2020/9/9
    **********************/
    Boolean load();


    /**********************
     * 存储单个msg
     * @return
     * @description //TODO
     * @date 23:39 2020/9/9
     **********************/
    PutMessageResult putMessage( MessageExtInner messageExtInner);

    /**********************
     * 存储多个msg
     * @param
     * @return
     * @description
     * //TODO 待开发
     * @date 23:39 2020/9/9
     **********************/
    PutMessageResult putMessages(MessageBatchExtInner messageBatchExtInner);


}
