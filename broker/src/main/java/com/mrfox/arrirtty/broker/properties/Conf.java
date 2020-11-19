package com.mrfox.arrirtty.broker.properties;

import com.mrfox.arrirtty.store.properties.StoreConf;
import lombok.Data;

/*****
 * 配置集合
 * @author     : MrFox
 * @date       : 2020-09-04 16:40
 * @description:
 * @version    :
 ****/
@Data
public class Conf {

    /**
     * 接收处理消息请求线程池核心数
     * */
    private ProcessReqConf processReqConf;

    /**
     * 存储相关配置
     * */
    private StoreConf storeConf;
}
