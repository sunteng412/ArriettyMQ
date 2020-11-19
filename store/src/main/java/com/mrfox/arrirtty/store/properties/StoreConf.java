package com.mrfox.arrirtty.store.properties;

import lombok.Data;

/*****
 * 请求配置
 * @author     : MrFox
 * @date       : 2020-09-04 16:40
 * @description:
 * @version    :
 ****/
@Data
public class StoreConf {
    /**
     * commLogPath存储位置
     * */
    private String brokerCommitLogPath;

    /**
     * consumerQueue存储位置
     * */
    private String brokerConsumeQueuePath;

    /**
     * 刷盘方式
     * */
    private String flushDiskType = FlushTypeEnum.ASYNC.getFlushType();

    /**
     * 刷新CommitLog的间隔时间
     * */
    private int flushIntervalCommitLog = 500;

    /**
     * 最小刷新物理页大小(默认为4)
     * */
    private int flushCommitLogLeastPages = 4;

    /**
     * commitLog的文件大小,默认1G
     * */
    private Long brokerCommitLogSize = 1073741824L;

    /**
     * consumerQueue的文件大小,默认6M
     * */
    private Integer brokerConsumerQueueSize = 6291456;

    /**
     * 消息最大长度,默认为512K
     * */
    private Integer  maxMessageSize = 1024 * 1024 * 4;

    /**
     * 物理刷新最小间隔秒数
     * */
    private int flushCommitLogThoroughInterval = 1000 * 10;
}
