package com.mrfox.arrirtty.store.service.flush;

import com.mrfox.arrirtty.store.service.CommitLog;
import lombok.extern.slf4j.Slf4j;

/***********************
 * 异步刷新
 * @author MrFox
 * @date 2020/11/1 22:50
 * @version 1.0
 * @description
 ************************/
@Slf4j
public class FlushRealTimeService extends FlushCommitLogService {

    private CommitLog commitLog;

    public FlushRealTimeService(CommitLog commitLog) {
        this.commitLog = commitLog;
    }

    @Override
    public String getServiceName() {
        return FlushRealTimeService.class.getSimpleName();
    }

    /**
     * 最后一次刷新时间毫秒值
     * */
    private long lastFlushTimestamp = 0;

    @Override
    public void run() {
        log.info(getServiceName() + " service started");
        while (!this.isStopped()){
            //刷新间隔时间
            int intervalCommitLog = commitLog.defaultStoreService.getStoreConf().getFlushIntervalCommitLog();

            //刷新物理队列最小页面
            int flushCommitLogLeastPages = commitLog.defaultStoreService.getStoreConf().getFlushCommitLogLeastPages();

            //物理刷新最小间隔秒数 --10秒
            int flushCommitLogThoroughInterval = commitLog.defaultStoreService.getStoreConf().getFlushCommitLogThoroughInterval();


            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis >= (this.lastFlushTimestamp + flushCommitLogThoroughInterval)) {
                //如果最后刷新时间和当前时间差距大于10秒可以直接刷新而不用在乎是否达到刷新物理最小页面
                this.lastFlushTimestamp = currentTimeMillis;
                flushCommitLogLeastPages = 0;
            }

            //等待被唤醒
           waitForRunning(intervalCommitLog);

            //开始刷新
            long begin = System.currentTimeMillis();

            try {
                commitLog.mappedFileQueue.flush(flushCommitLogLeastPages);
            }catch (Exception e){
                log.warn(this.getServiceName() + " service has exception. ", e);
            }

            long past = System.currentTimeMillis() - begin;
            if (past > 500) {
                log.info("Flush data to disk costs {} ms", past);
            }
        }
    }
}
