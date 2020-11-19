package com.mrfox.arrirtty.broker;

import com.mrfox.arrirtty.broker.processor.ProcessMessageReqService;
import com.mrfox.arrirtty.broker.properties.Conf;
import com.mrfox.arrirtty.remoting.BrokerNettyServer;
import com.mrfox.arrirtty.remoting.model.RemoteActionEnum;
import com.mrfox.arrirtty.store.service.store.DefaultStoreServiceImpl;
import com.mrfox.arrirtty.store.service.store.StoreService;
import lombok.extern.slf4j.Slf4j;

/***********************
 * broker服务
 * @author MrFox
 * @date 2020/9/7 23:30
 * @version 1.0
 * @description
 ************************/
@Slf4j
public class BrokerController {

    /**
     * 传输配置
     */
    private Conf conf;

    /**
     * 传输模块抽象
     * */
    private BrokerNettyServer brokerNettyServer;

    /**
     * 存储模块抽象
     * */
    private StoreService storeService;


    public BrokerController(final Conf conf) {
        this.conf = conf;
        //初始化netty
        this.brokerNettyServer = new BrokerNettyServer();
        brokerNettyServer.start();

        //注册处理器
        registryProcessor();

        //初始化存储模块
        storeService = new DefaultStoreServiceImpl(conf.getStoreConf());
        //load commitLog和consumerQueue
        Boolean load = storeService.load();

        if(!load){
            log.error("[brokerController]加载commitLog或consumerQueue文件失败");
            System.exit(0);
        }



    }

    /*****
     * 注册处理器
     * @param
     * @return
     * @description:
     ****/
    public void registryProcessor() {
        //处理消息线程
        ProcessMessageReqService processMessageReqService = new ProcessMessageReqService(this);
        brokerNettyServer.registryProcessor(RemoteActionEnum.MSG_REQ, processMessageReqService, processMessageReqService.getExecutorService());
        log.info("[BrokerServer]装入处理线程池成功");
    }

    public StoreService getStoreService() {
        return storeService;
    }

    public Conf getConf() {
        return conf;
    }
}
