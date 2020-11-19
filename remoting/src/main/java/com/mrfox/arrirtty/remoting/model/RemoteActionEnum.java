package com.mrfox.arrirtty.remoting.model;

/*****
 * 操作类型枚举
 * @author     : MrFox
 * @date       : 2020-09-03 14:45
 * @description:
 * @version    :
 ****/
public enum RemoteActionEnum {

    MSG_REQ(1,"存储消息");

    private Integer code;

    private String desc;


    RemoteActionEnum(int code, String desc) {
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
