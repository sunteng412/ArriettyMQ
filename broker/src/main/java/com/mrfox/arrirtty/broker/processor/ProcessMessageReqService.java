package com.mrfox.arrirtty.broker.processor;

import com.mrfox.arrirtty.broker.BrokerController;
import com.mrfox.arrirtty.remoting.model.RemoteModel;
import com.mrfox.arrirtty.common.constants.RemotingSysResponseCode;
import com.mrfox.arrirtty.remoting.service.NettyRequestProcessor;
import com.mrfox.arrirtty.store.model.PutMessageResult;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/***********************
 * 处理消息存储请求
 * @author MrFox
 * @date 2020/9/6 15:31
 * @version 1.0
 * @description
 ************************/
@Data
@Slf4j
public class ProcessMessageReqService implements NettyRequestProcessor {

    private BrokerController brokerController;

    private ExecutorService executorService;

    public ProcessMessageReqService(BrokerController brokerController) {
        this.brokerController = brokerController;
        executorService =  new ThreadPoolExecutor(brokerController.getConf().getProcessReqConf()
                .getProcessReqExecutorCoreSize(), brokerController.getConf().getProcessReqConf().getProcessReqExecutorMaxSize(),
                100, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2000), new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("[p-m-r-service]"+ threadIndex.addAndGet(1));
                return thread;
            }
        });

    }

    @Override
    public RemoteModel processRequest(ChannelHandlerContext ctx, RemoteModel request) {
        PutMessageResult putMessageResult =
                brokerController.getStoreService().putMessage(
                MessageProcessor.buildMsgInner(ctx.channel().remoteAddress(),
                        ctx.channel().localAddress(),
                        request));
        log.debug("[ProcessMessageReqService#processRequest]收到消息[{}]",request.toString());
        return handlerResult(putMessageResult);
    }

    /**********************
     * 结果
     * @param
     * @return
     * @description
     * //TODO:待开发
     * @date 16:47 2020/9/13
    **********************/
    private RemoteModel handlerResult(PutMessageResult putMessageResult) {
        RemoteModel remoteModel = new RemoteModel();
        remoteModel.setCode(RemotingSysResponseCode.SUCCESS);
        remoteModel.setIsSuccess(Boolean.TRUE);
        return remoteModel;
    }


    @Override
    public Boolean rejectRequest() {
        return Boolean.TRUE;
    }
}
