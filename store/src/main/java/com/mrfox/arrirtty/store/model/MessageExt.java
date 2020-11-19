package com.mrfox.arrirtty.store.model;

import lombok.Data;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;

/***********************
 * 内部消息存储结构-基础对象
 * @author MrFox
 * @date 2020/9/9 23:44
 * @version 1.0
 * @description
 ************************/
@Data
public class MessageExt implements Serializable {
    private static final long serialVersionUID = 8445773977080406428L;

    /**
     * queueId
     **/
    private Integer queueId;

    /**
     * topic
     **/
    private String topic;

    private long bornTimestamp;

    /**
     * 消息来源host
     **/
    private SocketAddress bornHost;


    private long storeTimestamp;
    /**
     * 消息存储host
     **/
    private SocketAddress storeHost;

    /**
     * 消息重试次数
     **/
    private Integer reconsumeTimes;

    /**
     * 消息体
     **/
    private byte[] body;

    /***
     * 扩展字段属性
     */
    private Map<String, String> properties;

    /**********************
     * 地址转byteBuffer
     * @param socketAddress 主机地址对象
     * @param byteBuffer 返回的对象
     * @return
     *  @description //TODO
     * @date 22:43 2020/10/24
    **********************/
    public static ByteBuffer socketAddress2ByteBuffer(final SocketAddress socketAddress, final ByteBuffer byteBuffer) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        byteBuffer.put(inetSocketAddress.getAddress().getAddress(), 0, 4);
        byteBuffer.putInt(inetSocketAddress.getPort());
        byteBuffer.flip();
        return byteBuffer;
    }

    public ByteBuffer getStoreHostBytes(ByteBuffer byteBuffer) {
        return socketAddress2ByteBuffer(this.storeHost, byteBuffer);
    }

    public ByteBuffer getBornHostBytes(ByteBuffer byteBuffer) {
        return socketAddress2ByteBuffer(this.bornHost, byteBuffer);
    }
}
