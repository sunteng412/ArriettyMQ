package com.mrfox.arrirtty.store.properties;

import lombok.Getter;

/***********************
 * 刷盘方式 async-异步刷盘 sync-同步刷盘
 * @author MrFox
 * @date 2020/11/1 18:02
 * @version 1.0
 * @description
 ************************/
public enum  FlushTypeEnum {

    /**
     * 异步刷盘
     * */
    ASYNC("async"),
    /**
     * 同步刷盘
     * */
    SYNC("sync")
    ;

    @Getter
    private final String flushType;

    FlushTypeEnum(String flushType) {
        this.flushType = flushType;
    }
}
