package com.mrfox.arrirtty.store.service;

import com.mrfox.arrirtty.store.properties.StoreConf;
import com.mrfox.arrirtty.store.service.store.DefaultStoreServiceImpl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/*****
 * 消费队列服务
 * @author     : MrFox
 * @date       : 2020-09-14 15:16
 * @description:
 * @version    :
 ****/
@Slf4j
public class ConsumerQueueManager{

    private ConcurrentHashMap<String, ConcurrentHashMap<Integer,ConsumerQueue>> consumeQueueTable;

    private String path;

    @Getter
    private DefaultStoreServiceImpl defaultStoreService;

    public ConsumerQueueManager(StoreConf storePath, DefaultStoreServiceImpl defaultStoreService) {
        this.defaultStoreService = defaultStoreService;
        consumeQueueTable = new ConcurrentHashMap<>();
        this.path = storePath.getBrokerConsumeQueuePath();
    }

    /*****
     * 加载
     * @param
     * @return
     * @description:
     ****/
    public Boolean load() {
        AtomicInteger count = new AtomicInteger(0);
        File dirLogic = new File(path);
        File[] fileTopicList = dirLogic.listFiles();
        if (fileTopicList != null) {

            for (File fileTopic : fileTopicList) {
                String topic = fileTopic.getName();

                File[] fileQueueIdList = fileTopic.listFiles();
                if (fileQueueIdList != null) {
                    for (File fileQueueId : fileQueueIdList) {
                        int queueId;
                        try {
                            queueId = Integer.parseInt(fileQueueId.getName());
                        } catch (NumberFormatException e) {
                            continue;
                        }
                        ConsumerQueue logic = new ConsumerQueue(
                                topic,
                                queueId,
                                path,
                                this);
                        this.putConsumeQueue(topic, queueId, logic);
                        Integer load = logic.load0();
                        if (load == -1) {
                            return false;
                        }else {
                            count.addAndGet(1);
                        }
                    }
                }
            }
        }

        log.info("装入topic对应consumerQueue文件成功,装入数量[{}]",count.get());

        return true;
    }

    /*****
     * 装入map里
     * @param
     * @return
     * @description:
     ****/
    private void putConsumeQueue(String topic, int queueId, ConsumerQueue logic) {
        ConcurrentHashMap<Integer, ConsumerQueue> map = this.consumeQueueTable.get(topic);
        if (null == map) {
            map = new ConcurrentHashMap<Integer
                    /* queueId */,
                    ConsumerQueue>();
            map.put(queueId, logic);
            this.consumeQueueTable.put(topic,  map);
        } else {
            map.put(queueId, logic);
        }
    }
}
