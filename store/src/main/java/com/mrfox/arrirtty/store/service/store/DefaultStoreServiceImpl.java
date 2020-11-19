package com.mrfox.arrirtty.store.service.store;

import com.mrfox.arrirtty.store.model.MessageBatchExtInner;
import com.mrfox.arrirtty.store.model.MessageExtInner;
import com.mrfox.arrirtty.store.model.PutMessageResult;
import com.mrfox.arrirtty.store.properties.StoreConf;
import com.mrfox.arrirtty.store.service.CommitLog;
import com.mrfox.arrirtty.store.service.ConsumerQueueManager;
import com.mrfox.arrirtty.store.service.dispatch.CommitLogDispatchService;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/***********************
 * 默认存储实现
 * @author MrFox
 * @date 2020/9/13 15:07
 * @version 1.0
 * @description
 ************************/
@Slf4j
public class DefaultStoreServiceImpl implements StoreService{

    /**
     * 存储配置
     * */
    @Getter
    private final StoreConf storeConf;

    private List<CommitLogDispatchService> dispatchServices;

    private CommitLog commitLog;

    private ConsumerQueueManager consumerQueueManager;

    /**********************
     * 新增插入commitLog之后操作的service
     * @param
     * @return
     * @description //TODO
     * @date 17:07 2020/9/13
    **********************/
    public void addDispatchService(CommitLogDispatchService commitLogDispatchService){
        dispatchServices.add(commitLogDispatchService);
    }

    public DefaultStoreServiceImpl(StoreConf storeConf) {
        this.storeConf = storeConf;
        //初始化commitLog记录事件转发
        dispatchServices = new LinkedList<>();
        initialDefaultService();

        //commitLog
        commitLog = new CommitLog(this);
        //consumeQueue
        consumerQueueManager = new ConsumerQueueManager(storeConf,this);
    }

    /*****
     * 初始化默认
     * @param
     * @return
     * @description:
     ****/
    private void initialDefaultService() {

    }


    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public Boolean load() {
        Boolean load;

        load = commitLog.load();
        load = load && consumerQueueManager.load();

        //初始化commitLog
        if(load){
            //正常流程启动
            commitLog.recoverNormally();
        }
        return load;
    }

    /**********************
     * 存储单个msg
     * @return
     * @description //TODO
     * @date 23:39 2020/9/9
     **********************/
    @Override
    public PutMessageResult putMessage(MessageExtInner messageExtInner){
        return commitLog.putMessage(messageExtInner);
    }

    /**********************
     * 存储多个msg
     * @param
     * @return
     * @description
     * //TODO 待开发
     * @date 23:39 2020/9/9
     **********************/
    public PutMessageResult putMessages(MessageBatchExtInner messageBatchExtInner){
        return null;
    }


}
