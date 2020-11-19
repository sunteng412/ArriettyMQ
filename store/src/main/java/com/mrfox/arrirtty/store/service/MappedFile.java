package com.mrfox.arrirtty.store.service;

import com.mrfox.arrirtty.store.model.MessageExtInner;
import com.mrfox.arrirtty.store.model.PutMessageResult;
import com.mrfox.arrirtty.store.service.put.PutMessageCallBack;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

/*****
 * 映射文件抽象
 * @author     : MrFox
 * @date       : 2020-09-14 14:26
 * @description:
 * @version    :
 ****/
@Slf4j
public class MappedFile {

    /**
     * 操作系统页大小,默认4K每页
     * */
    public static final int OS_PAGE_SIZE = 1024 * 4;

    /**
     * 映射ByteBuffer
     * */
    private MappedByteBuffer mappedByteBuffer;

    /**
     * 当前文件偏移量
     * */
    @Getter
    private Long fileFromOffset;

    @Getter
    private Long mappedFileSize;

    /**
     * 最后一次存储的时间
     * */
    @Getter
    @Setter
    private volatile long storeTimestamp = 0;

    private AtomicInteger wrotePos = new AtomicInteger(0);


    protected final AtomicInteger committedPosition = new AtomicInteger(0);
    private final AtomicInteger flushedPosition = new AtomicInteger(0);

    /***
     * 文件名称
     */
    @Getter
    private String fileName;


    public MappedFile(String path, String fileName, Long mappedFileSize) throws IOException {
        this.mappedFileSize = mappedFileSize;
        init(path + File.separator + fileName,mappedFileSize,Boolean.TRUE);
        this.fileName = fileName;
        fileFromOffset = Long.parseLong(fileName);
    }

    public MappedFile(String path, Long mappedFileSize) throws IOException {
        this.mappedFileSize = mappedFileSize;
        init(path,mappedFileSize,Boolean.TRUE);
    }

    /*****
     * 初始化
     * @param path 路径
     * @param mappedFileSize 文件映射虚拟内存大小
     * @param isNotExist 是否存在
     * @return
     * @description:
     ****/
    private void init(String path,Long mappedFileSize, Boolean isNotExist) throws IOException {
        if(isNotExist){
            if (path != null) {
                File f = new File(path);
                if (!f.exists()) {
                    boolean result = f.mkdirs();
                    log.info(path + " mkdir " + (result ? "OK" : "Failed"));
                }
            }
        }

        RandomAccessFile randomAccessFile = new RandomAccessFile(path , "rw");

        FileChannel fileChannel = randomAccessFile.getChannel();

        //设置映射从开始到指定位置的大小
        this.mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, mappedFileSize);


    }

    /**********************
     * 存储消息
     * @param messageExtInner 消息
     * @param putMessageCallBack 消息回调
     * @return  {@link PutMessageResult}
     * @description //TODO
     * @date 16:57 2020/9/20
    **********************/
    public PutMessageResult putInnerMessage(MessageExtInner messageExtInner, PutMessageCallBack putMessageCallBack) {
        ByteBuffer byteBuffer = mappedByteBuffer.slice();
        PutMessageResult putMessageResult = putMessageCallBack
                .doAppend(byteBuffer, fileFromOffset, mappedFileSize.intValue() - wrotePos.get(), messageExtInner);
        wrotePos.addAndGet(putMessageResult.getWroteBytesCount());
        log.info("[MappedFile#putInnerMessage]存储成功,结果:[{}]",putMessageResult);
        return putMessageResult;
    }


    /**********************
     * 返回该文件映射buffer的slice
     * @param
     * @return
     * @description //TODO
     * @date 21:42 2020/10/31
    **********************/
    public ByteBuffer sliceByteBuffer() {
        return this.mappedByteBuffer.slice();
    }

    public void setWrotePos(int wrotePos) {
        this.wrotePos.set(wrotePos);
    }

    public void setCommittedPosition(int committedPosition) {
        this.committedPosition.set(committedPosition);
    }

    public void setFlushedPosition(int flushedPosition) {
        this.flushedPosition.set(flushedPosition);
    }

    /**********************
     * 刷新物理磁盘
     * @param
     * @return
     * @description //TODO
     * @date 21:53 2020/11/4
    *********************/
    public int flush(int flushCommitLogLeastPages) {
        if(isAbleToFlush(flushCommitLogLeastPages)){
            mappedByteBuffer.force();
        }
        return flushedPosition.get();
    }

    /**
     * 判断是否可刷新
     * @param
     * @return
     * @description:
     */
    private boolean isAbleToFlush(int flushCommitLogLeastPages) {
        int flush = flushedPosition.get();

        //上次写的位置
        int write = wrotePos.get();

        if(mappedFileSize == wrotePos.get()){
            //写满了
            return true;
        }

        if (flushCommitLogLeastPages > 0) {
            return ((write / OS_PAGE_SIZE) - (flush / OS_PAGE_SIZE)) >= flushCommitLogLeastPages;
        }

        return flush <  write;
    }
}
