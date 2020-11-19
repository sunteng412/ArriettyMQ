package com.mrfox.arrirtty.store.service;

import java.io.File;

/*****
 * 队列映射抽象
 * @author     : MrFox
 * @date       : 2020-09-14 15:39
 * @description:
 * @version    :
 ****/
public class ConsumerQueue extends AbstractFile{

    private ConsumerQueueManager consumerQueueManager;

    public ConsumerQueue(String topic, int queueId, String path, ConsumerQueueManager consumerQueueManager) {
        super(path + File.separator + topic +  File.separator + queueId,Long.MAX_VALUE,
                consumerQueueManager.getDefaultStoreService());
        this.consumerQueueManager = consumerQueueManager;
    }

}
