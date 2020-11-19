package com.mrfox.arrirtty.remoting.model;

/*****
 * 操作类型枚举
 * @author     : MrFox
 * @date       : 2020-09-03 14:45
 * @description:
 * @version    :
 ****/
public enum RemoteTypeEnum {

    ONE_WAY(1,"不需要等待响应"),
    ASYNC(2,"异步响应"),
    SYNC(3,"同步响应");

    private Integer code;

    private String desc;


    RemoteTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
