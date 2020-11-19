package com.mrfox.arrirtty.common;

import com.mrfox.arrirtty.common.juc.CountDownLatch2;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/***********************
 * 线程
 * @author MrFox
 * @date 2020/11/1 22:24
 * @version 1.0
 * @description
 ************************/
@Slf4j
public abstract class ServiceThread implements Runnable{
    protected final Thread thread;

    /**
     * 停止点
     * */
    protected final CountDownLatch2 waitPoint = new CountDownLatch2(1);

    /**
     * 是否停止
     * */
    protected volatile boolean stopped = false;

    /**
     * 是否被唤醒
     * */
    protected volatile AtomicBoolean hasNotified = new AtomicBoolean(false);

    protected ServiceThread() {
        this.thread = new Thread(this,this.getServiceName());
    }

    public abstract String getServiceName();

    public void start() {
        this.thread.start();
    }

    protected void waitForRunning(long interval) {
        if (hasNotified.compareAndSet(true, false)) {
            this.onWaitEnd();
            return;
        }

        //entry to wait
        waitPoint.reset();

        try {
            waitPoint.await(interval, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Interrupted", e);
        } finally {
            hasNotified.set(false);
            this.onWaitEnd();
        }
    }

    protected void onWaitEnd() {
    }

    public void wakeup() {
        //cas操作更新被唤醒状态
        if (hasNotified.compareAndSet(false, true)) {
            waitPoint.countDown(); // notify
        }
    }

    public boolean isStopped() {
        return stopped;
    }
}
