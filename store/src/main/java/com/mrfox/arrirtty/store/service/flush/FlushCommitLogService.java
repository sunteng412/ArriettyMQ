package com.mrfox.arrirtty.store.service.flush;

import com.mrfox.arrirtty.common.ServiceThread;

public abstract class FlushCommitLogService extends ServiceThread {
        protected static final int RETRY_TIMES_OVER = 10;

}