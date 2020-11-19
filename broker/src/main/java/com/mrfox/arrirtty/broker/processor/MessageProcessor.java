package com.mrfox.arrirtty.broker.processor;

import com.mrfox.arrirtty.common.Assert;
import com.mrfox.arrirtty.common.BeanUtils;
import com.mrfox.arrirtty.common.MessageDecoder;
import com.mrfox.arrirtty.common.constants.Remoting2MessageExtInnerMapping;
import com.mrfox.arrirtty.remoting.common.RemotingHelper;
import com.mrfox.arrirtty.remoting.model.RemoteModel;
import com.mrfox.arrirtty.store.model.MessageExtInner;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

/***********************
 * 消息工具类
 * @author MrFox
 * @date 2020/9/13 15:24
 * @version 1.0
 * @description
 ************************/
public class MessageProcessor {

    /**********************
     * RemoteModel -> MessageExtInner转换
     * @param
     * @param remote
     * @param local
     * @return
     * @description //TODO
     * @date 16:31 2020/9/13
    **********************/
    public static MessageExtInner buildMsgInner(SocketAddress remote, SocketAddress local, RemoteModel remoteModel) {
        Assert.notNull(remoteModel);
        MessageExtInner messageExtInner = new MessageExtInner();
        messageExtInner.setBody(remoteModel.getContent());
        messageExtInner.setBornTimestamp(remoteModel.getBornTimestamp());
        messageExtInner.setStoreTimestamp(System.currentTimeMillis());

        Map<String, String> extFields = remoteModel.getExtFields();
        BeanUtils.copyMapPropertiesByMappingMap(extFields,messageExtInner, Remoting2MessageExtInnerMapping.MAPPING_MAP);
        messageExtInner.setPropertiesString(MessageDecoder.messageProperties2String(extFields));
        messageExtInner.setBornHost(remote);
        messageExtInner.setStoreHost(local);
        //tag的hashCode
        messageExtInner.setTagsCode(messageExtInner.getTags().hashCode());

        /**
         * 过滤已经解析的自定义属性
         * */
        Map<String, String> properties = new HashMap<>();
        extFields.forEach((k,v)->{
            if(!Remoting2MessageExtInnerMapping.MAPPING_MAP.containsKey(v)){
                properties.put(k,v);
            }

        });
        messageExtInner.setProperties(properties);

        //获取
        return messageExtInner;
    }
}
