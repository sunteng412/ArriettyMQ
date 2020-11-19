package com.mrfox.arrirtty.store.model;

import lombok.Data;

/***********************
 * @author MrFox
 * @date 2020/9/13 14:39
 * @version 1.0
 * @description
 ************************/
@Data
public class PutMessageResult {

    /**
     * 存储结果
     * */
    private PutMessageStatus putMessageStatus;

    /**
     * ext详情-存储字节数
     * */
    private Integer wroteBytesCount;

    /**
     * ext详情-存储偏移量-针对于该topic来说,如果为批量则为最后插入的偏移量
     * */
    private Long wroteOffset;

    /**
     * ext详情-消息唯一id,算法为雪花算法 + broker的唯一id生成,保证不会重复
     * */
    private String msgId;

    /**
     * ext详情-存储毫秒值
     * */
    private Long wroteTimeStamp;

    /**
     * ext详情-消息个数,默认为1
     * */
    private Integer msgNum = 1;

    /**
     * 页缓存耗费时间
     * */
    private long pagecacheRT = 0;

    /**
     * 队列内偏移量
     * */
    private Long queueOffset;

    public static PutMessageResult fastFail(PutMessageStatus putMessageStatus) {
        PutMessageResult putMessageResult = new PutMessageResult();
        putMessageResult.setPutMessageStatus(putMessageStatus);
        return putMessageResult;
    }

    public static PutMessageResult PutMessageResult(PutMessageStatus putMessageStatus,
                                            long wroteOffset, Integer wroteBytes,String msgId,
                                            long storeTimestamp, Long queueOffset, long pagecacheRT) {
        PutMessageResult putMessageResult = new PutMessageResult();
        putMessageResult.setPutMessageStatus(putMessageStatus);
        putMessageResult.setWroteOffset(wroteOffset);
        putMessageResult.setWroteBytesCount(wroteBytes);
        putMessageResult.setMsgId(msgId);
        putMessageResult.setWroteTimeStamp(storeTimestamp);
        putMessageResult.setQueueOffset(queueOffset);
        putMessageResult.setPagecacheRT(pagecacheRT);
        return putMessageResult;
    }
}
