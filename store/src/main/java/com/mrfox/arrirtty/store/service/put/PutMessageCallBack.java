package com.mrfox.arrirtty.store.service.put;

import com.mrfox.arrirtty.store.model.MessageExtInner;
import com.mrfox.arrirtty.store.model.PutMessageResult;

import java.nio.ByteBuffer;

/***
 * 插入消息回调
 * */
public interface PutMessageCallBack {

    /**********************
     * 单个插入消息
     * @param byteBuffer 对应commitLog的映射数组
     * @param fileFromOffset 文件当前的偏移点
     * @param messageExtInner 消息
     * @param remainingSpace 剩余可用空间
     * @return {@link PutMessageResult}
     * @description //TODO
     * @date 16:54 2020/9/20
    **********************/
    PutMessageResult doAppend(final ByteBuffer byteBuffer, final Long fileFromOffset,
                              final Integer remainingSpace,final MessageExtInner messageExtInner);
}
