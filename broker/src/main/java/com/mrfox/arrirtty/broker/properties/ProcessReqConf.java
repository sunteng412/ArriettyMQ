package com.mrfox.arrirtty.broker.properties;

import lombok.Data;

/*****
 * 请求配置
 * @author     : MrFox
 * @date       : 2020-09-04 16:40
 * @description:
 * @version    :
 ****/
@Data
public class ProcessReqConf {

    /**
     * 接收处理消息请求线程池核心数
     * */
    private Integer processReqExecutorCoreSize;

    /**
     * 接收处理消息请求线程池最大线程数
     * */
    private Integer processReqExecutorMaxSize;
}
