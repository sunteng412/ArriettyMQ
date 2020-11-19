package com.mrfox.arrirtty.store.model;

/***********************
 * 储存结果
 * @author MrFox
 * @date 2020/9/13 14:39
 * @version 1.0
 * @description
 ************************/
public enum PutMessageStatus {
    /**
     * 成功
     * */
    PUT_OK,
    /***
     * 创建文件异常
     * */
    CREATE_COMMIT_LOG_ERROR,
    /**
     * 自定义属性过长
     * */
    CUSTOM_PROPERTIES_LENGTH_TOO_LONG,
    /**
     * 消息过长
     * */
    MESSAGE_SIZE_EXCEEDED ;
}
