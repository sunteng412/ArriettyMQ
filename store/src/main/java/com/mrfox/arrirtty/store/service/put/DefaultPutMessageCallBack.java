package com.mrfox.arrirtty.store.service.put;

import com.mrfox.arrirtty.common.MessageDecoder;
import com.mrfox.arrirtty.store.model.MessageExtInner;
import com.mrfox.arrirtty.store.model.PutMessageResult;
import com.mrfox.arrirtty.store.model.PutMessageStatus;
import com.mrfox.arrirtty.store.service.CommitLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.util.Objects;

/***********************
 * 默认插入消息回调
 * @author MrFox
 * @date 2020/10/17 15:51
 * @version 1.0
 * @description
 ************************/
@Slf4j
public class DefaultPutMessageCallBack implements PutMessageCallBack{

    /**
     * 存放主机地址(包括存储主机IP和发送主机IP)
     * */
    private final ByteBuffer hostHolder = ByteBuffer.allocate(8);

    /**
     * 存储临时msgId
     * */
    private final ByteBuffer msgIdMemory;

    /**
     * 消费组topic偏移消息的key
     * */
    private final StringBuilder keyBuilder = new StringBuilder();

    /**
     * 文件末尾的最小固定长度为空
     * */
    private static final int END_FILE_MIN_BLANK_LENGTH = 4 + 4;

    /**
     * 存储消息对应映射内存
     * */
    private final ByteBuffer msgStoreItemMemory;

    /**
     * 消息最大长度
     * */
    private final int maxMessageSize;

    public DefaultPutMessageCallBack(int size) {
        this.msgIdMemory = ByteBuffer.allocate(MessageDecoder.MSG_ID_LENGTH);
        this.msgStoreItemMemory = ByteBuffer.allocate(size + END_FILE_MIN_BLANK_LENGTH);
        this.maxMessageSize = size;
    }

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
    @Override
    public PutMessageResult doAppend(final ByteBuffer byteBuffer,final Long fileFromOffset,
                                     final Integer remainingSpace,final MessageExtInner messageExtInner) {
        //1.获取文件偏移量
        long wroteOffset = fileFromOffset + byteBuffer.position();

        //重置host的byteBuffer
        this.resetByteBuffer(hostHolder, 8);

        //创建消息唯一Id 通过主机地址和偏移量
        String msgId = MessageDecoder.createMessageId(this.msgIdMemory, messageExtInner.getStoreHostBytes(hostHolder), wroteOffset);

        //消费组需要偏移量,记录偏移信息
        keyBuilder.setLength(0);
        keyBuilder.append(messageExtInner.getTopic());
        keyBuilder.append('-');
        keyBuilder.append(messageExtInner.getQueueId());
        String key = keyBuilder.toString();

        //队列偏移量
        Long queueOffset = CommitLog.topicQueueTable.computeIfAbsent(key, k -> 0L);

        //todo:事务消息回滚消息处理

        //序列化自定义属性
        final byte[] propertiesData =
                StringUtils.isBlank(messageExtInner.getPropertiesString()) ? null :
                        messageExtInner.getPropertiesString().getBytes(MessageDecoder.CHARSET_UTF8);

        //序列化自定义属性
        final int propertiesLength =  Objects.isNull(propertiesData) ? 0 : propertiesData.length;

        if (propertiesLength > Short.MAX_VALUE) {
            log.warn("[DefaultPutMessageCallBack.doAppend]putMessage message properties length too long. length={}", propertiesData.length);
            return  PutMessageResult.fastFail(PutMessageStatus.CUSTOM_PROPERTIES_LENGTH_TOO_LONG);
        }

        //topic数据
        final byte[] topicData = messageExtInner.getTopic().getBytes(MessageDecoder.CHARSET_UTF8);

        final int topicLength = topicData.length;

        final int bodyLength =Objects.isNull( messageExtInner.getBody()) ? 0 : messageExtInner.getBody().length;

        //计算总长度
        final int msgLen = CommitLog.calMsgLength(bodyLength, topicLength, propertiesLength);

        //判断消息长度
        if (msgLen > this.maxMessageSize) {
            log.warn("message size exceeded, msg total size: " + msgLen + ", msg body size: " + bodyLength
                    + ", maxMessageSize: " + this.maxMessageSize);
            return  PutMessageResult.fastFail(PutMessageStatus.MESSAGE_SIZE_EXCEEDED);
        }

        //判断是否足够
        if ((msgLen + END_FILE_MIN_BLANK_LENGTH) > remainingSpace) {
            this.resetByteBuffer(this.msgStoreItemMemory, remainingSpace);
            // 1 TOTALSIZE
            this.msgStoreItemMemory.putInt(remainingSpace);
            // 2 MAGICCODE
            this.msgStoreItemMemory.putInt(CommitLog.BLANK_MAGIC_CODE);
            // 3 The remaining space may be any value
            // Here the length of the specially set maxBlank
            final long beginTimeMills = System.currentTimeMillis();
            byteBuffer.put(this.msgStoreItemMemory.array(), 0, remainingSpace);
            return  PutMessageResult.PutMessageResult(PutMessageStatus.MESSAGE_SIZE_EXCEEDED,wroteOffset,remainingSpace,msgId,
                    messageExtInner.getStoreTimestamp(),
                    queueOffset,System.currentTimeMillis() - beginTimeMills);
        }

        // 初始化存储空间
        this.resetByteBuffer(msgStoreItemMemory, msgLen);
        // 4 //TOTALSIZE 总大小
        msgStoreItemMemory.putInt(msgLen);
        // + 4 //MAGICCODE 魔数 代表是否有数据
        msgStoreItemMemory.putInt(CommitLog.MESSAGE_MAGIC_CODE);
         //+ 4 //QUEUEID 队列id
        msgStoreItemMemory.putInt(messageExtInner.getQueueId());
         //       + 8 //QUEUEOFFSET 对列偏移量
        msgStoreItemMemory.putLong(queueOffset);
        //+ 8 //BORNTIMESTAMP 发送时间戳
        msgStoreItemMemory.putLong(messageExtInner.getBornTimestamp());
         //+ 8 //BORNHOST 发送IP
        this.resetByteBuffer(hostHolder,8);
        msgStoreItemMemory.put(messageExtInner.getBornHostBytes(hostHolder));
        //+ 8 //STORETIMESTAMP 存储时间戳
        msgStoreItemMemory.putLong(messageExtInner.getStoreTimestamp());
        //+ 8 //STOREHOSTADDRESS 存储机器IP
        this.resetByteBuffer(hostHolder,8);
        msgStoreItemMemory.put(messageExtInner.getStoreHostBytes(hostHolder));
        //+ 4 //RECONSUMETIMES 消费次数
        msgStoreItemMemory.putInt(messageExtInner.getReconsumeTimes());
        //+ 4 + (bodyLength > 0 ? bodyLength : 0) //BODY
        msgStoreItemMemory.putInt(bodyLength);
        if (bodyLength > 0){
            msgStoreItemMemory.put(messageExtInner.getBody());
        }
         //+ 1 + topicLength //TOPIC
        msgStoreItemMemory.put((byte) topicLength);
        if(topicLength > 0){
            msgStoreItemMemory.put(topicData);
        }
        //+ 2 + (propertiesLength > 0 ? propertiesLength : 0) //propertiesLength
        msgStoreItemMemory.putShort((short) propertiesLength);
        if (propertiesLength > 0){
            this.msgStoreItemMemory.put(propertiesData);
        }
        final long beginTimeMills = System.currentTimeMillis();
        // 写入映射内存中
        byteBuffer.put(this.msgStoreItemMemory.array(), 0, msgLen);

        //更新topicQueue的偏移位 Todo:待写

        return  PutMessageResult.PutMessageResult(PutMessageStatus.PUT_OK,wroteOffset,remainingSpace,msgId,
                messageExtInner.getStoreTimestamp(),
                queueOffset,System.currentTimeMillis() - beginTimeMills);
    }

    private void resetByteBuffer(final ByteBuffer byteBuffer, final int limit) {
        byteBuffer.flip();
        byteBuffer.limit(limit);
    }
}
