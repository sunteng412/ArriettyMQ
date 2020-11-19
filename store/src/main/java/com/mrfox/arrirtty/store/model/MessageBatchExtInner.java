package com.mrfox.arrirtty.store.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/***********************
 * 内部消息存储结构
 * @author MrFox
 * @date 2020/9/9 23:44
 * @version 1.0
 * @description
 ************************/
@EqualsAndHashCode(callSuper = true)
@Data
public class MessageBatchExtInner extends MessageExt {

    /**
     * tagsCode
     **/
    private Long tagsCode;

    /**
     * tag
     **/
    private String tags;
}
