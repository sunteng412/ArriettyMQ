package com.mrfox.arrirtty.store.service;

import com.mrfox.arrirtty.store.model.MessageExtInner;
import com.mrfox.arrirtty.store.model.PutMessageResult;
import com.mrfox.arrirtty.store.model.PutMessageStatus;
import com.mrfox.arrirtty.store.properties.FlushTypeEnum;
import com.mrfox.arrirtty.store.service.flush.FlushCommitLogService;
import com.mrfox.arrirtty.store.service.flush.FlushRealTimeService;
import com.mrfox.arrirtty.store.service.put.DefaultPutMessageCallBack;
import com.mrfox.arrirtty.store.service.put.PutMessageCallBack;
import com.mrfox.arrirtty.store.service.store.DefaultStoreServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/*****
 * commitLog映射对象
 * @author     : MrFox
 * @date       : 2020-09-14 14:29
 * @description:
 * @version    :
 ****/
@Slf4j
public class CommitLog extends AbstractFile {

    // Message's MAGIC CODE daa320a7
    public final static int MESSAGE_MAGIC_CODE = -626843481;

    // End of file empty MAGIC CODE cbd43194
    public final static int BLANK_MAGIC_CODE = -875286124;

    public static HashMap<String/* topic-queueid */, Long/* offset */>
            topicQueueTable = new HashMap<>(1024);

    private PutMessageCallBack putMessageCallBack;

    /**
     * commitLog存储路径
     * */
    private String commitLogPath;

    /**
     * 存储非公平锁
     * */
    private ReentrantLock storeLock = new ReentrantLock();

    /**
     * 刷盘方式
     * */
    private final FlushCommitLogService flushCommitLogService;


    public CommitLog(DefaultStoreServiceImpl defaultStoreService) {
        super(defaultStoreService.getStoreConf().getBrokerCommitLogPath(),
                defaultStoreService.getStoreConf().getBrokerCommitLogSize(),defaultStoreService);
        this.commitLogPath =  defaultStoreService.getStoreConf().getBrokerCommitLogPath();
        putMessageCallBack = new DefaultPutMessageCallBack(defaultStoreService.getStoreConf()
                .getMaxMessageSize());

        //初始化刷盘 - 目前只需要做异步就行
       /* if(FlushTypeEnum.ASYNC.getFlushType().equals(
                this.defaultStoreService.getStoreConf().getFlushDiskType())){
            flushCommitLogService = new FlushRealTimeService();
        }*/
        flushCommitLogService = new FlushRealTimeService(this);
    }


    public Boolean load() {
        Integer load = super.load0();
        if( load >= 0){
            log.info("装入commitLog文件成功,装入数量[{}],路径:[{}]",load,commitLogPath);
        }
        return load >= 0;
    }

    /**********************
     * 存储单个msg
     * @param messageExtInner 消息
     * @return
     * @description //TODO
     * @date 23:39 2020/9/9
     **********************/
    public PutMessageResult putMessage(MessageExtInner messageExtInner) {
        long startMillis = System.currentTimeMillis();


        PutMessageResult putMessageResult;
        long endMillis;
        storeLock.lock();
        try {
            //首先去获取mapperFile
            MappedFile lastMappedFile = findLastMappedFile(Boolean.TRUE);

            if(Objects.isNull(lastMappedFile)){
                log.error("[CommitLog#putMessage]创建commitLog,msg:[{}]",messageExtInner);
                return PutMessageResult.fastFail(PutMessageStatus.CREATE_COMMIT_LOG_ERROR);
            }

            putMessageResult = lastMappedFile.putInnerMessage(messageExtInner,putMessageCallBack);

            endMillis = System.currentTimeMillis();

            //唤醒刷盘
            handlerDiskFlush(putMessageResult,messageExtInner);
        }finally {
            storeLock.unlock();
        }

        log.info("[CommitLog#putMessage]存储成功,耗时:[{}],结果:[{}]",endMillis - startMillis,putMessageResult);
        return putMessageResult;
    }

    /**********************
     * 执行刷盘
     * @param
     * @param putMessageResult
     * @param messageExtInner
     * @return
     * @description //TODO
     * @date 17:39 2020/11/1
    **********************/
    private void handlerDiskFlush(PutMessageResult putMessageResult, MessageExtInner messageExtInner) {
        //唤醒刷盘服务
        flushCommitLogService.wakeup();
    }


    /**********************
     * 计算消息长度
     * @param bodyLength 消息长度
     * @param propertiesLength 自定义属性长度
     * @param topicLength 主题长度
     * @return
     * @description //TODO
     * @date 23:20 2020/10/24
    **********************/
    public static int calMsgLength(int bodyLength, int topicLength, int propertiesLength) {
        final int msgLen = 4 //TOTALSIZE 总大小
                + 4 //MAGICCODE 魔数 代表是否有数据
                + 4 //QUEUEID 对列id
                + 8 //QUEUEOFFSET 对列偏移量
                + 8 //BORNTIMESTAMP 发送时间戳
                + 8 //BORNHOST 发送IP
                + 8 //STORETIMESTAMP 存储时间戳
                + 8 //STOREHOSTADDRESS 存储机器IP
                + 4 //RECONSUMETIMES 消费次数
                + 4 + (bodyLength > 0 ? bodyLength : 0) //BODY
                + 1 + topicLength //TOPIC
                + 2 + (propertiesLength > 0 ? propertiesLength : 0) //propertiesLength
                + 0;
        return msgLen;
    }

    /**
     * 载入偏移量等信息
     * **/
    public void recoverNormally() {
        //获取所有的mappedFile
        final List<MappedFile> mappedFiles = this.mappedFileQueue.getMappedFiles();

        if(!mappedFiles.isEmpty()){
            MappedFile mappedFile = mappedFiles.get(mappedFiles.size() - 1);
            long processOffset = mappedFile.getFileFromOffset();
            //文件读取偏移量
            long  mappedFileOffset = 0;

            //返回映射文件
            ByteBuffer sliceByteBuffer = mappedFile.sliceByteBuffer();

            //获取commitLog文件对应的最终的文件偏移量的点
            while (true){
                int msgAndReturnSize = this.checkMsgAndReturnSize(sliceByteBuffer);
                if(msgAndReturnSize > 0 ){
                    //有消息
                   mappedFileOffset += msgAndReturnSize;
                }else {
                    //没有消息了
                    break;
                }
            }

            processOffset += mappedFileOffset;
            this.mappedFileQueue.setFlushedWhere(processOffset);
            this.mappedFileQueue.setCommittedWhere(processOffset);
            //这里是代表截断脏commitLog文件与设置对应的commitLog的存储偏移量
            this.mappedFileQueue.truncateDirtyFiles(processOffset);
        }


    }

    /**********************
     * 返回该条消息的大小
     * @param
     * @return
     * @description //TODO
     * @date 21:49 2020/10/31
    **********************/
    private int checkMsgAndReturnSize(ByteBuffer sliceByteBuffer) {
        return sliceByteBuffer.getInt();
    }
}
