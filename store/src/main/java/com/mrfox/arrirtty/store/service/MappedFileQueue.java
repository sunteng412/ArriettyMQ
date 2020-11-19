package com.mrfox.arrirtty.store.service;

import com.google.common.base.Throwables;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/*****
 * 映射文件集合抽象
 * @author     : MrFox
 * @date       : 2020-09-14 14:23
 * @description:
 * @version    :
 ****/
@Slf4j
public class MappedFileQueue {

    @Getter
    private final CopyOnWriteArrayList<MappedFile> mappedFiles = new CopyOnWriteArrayList<>();

    private Long mappedFileSize;

    /**
     * 下个文件开始点
     * */
    private long nextFileOffset = 0;

    /**
     * 存储位置
     * */
    private  String storePath;

    /**
     * 提交位置--代表本地磁盘的最后一次落地位置
     * */
    @Getter
    @Setter
    private long committedWhere = 0;

    /**
     * 刷新位置-刷新到本地磁盘位置(这里是代表缓存区与磁盘的相关位置)
     * */
    @Getter
    @Setter
    private long flushedWhere = 0;

    /**
     * 最终刷盘
     * */
    @Getter
    @Setter
    private volatile long storeTimestamp = 0;

    public MappedFileQueue(String storePath,Long mappedFileSize) {
        this.storePath = storePath;
        this.mappedFileSize = mappedFileSize;
    }

    /*****
     * 单一路径多文件加载
     * @description:
     ****/
    public Integer load() {
        File dir = new File(this.storePath);
        File[] files = dir.listFiles();
        if (files != null) {
            // ascending order
            Arrays.sort(files);
            for (File file : files) {

                if (file.length() != this.mappedFileSize) {
                    log.warn(file + "\t" + file.length()
                            + " 长度没有对应文件大小");
                    return -1;
                }

                try {
                    MappedFile mappedFile = new MappedFile(file.getPath(),  mappedFileSize);
                    this.mappedFiles.add(mappedFile);
                    nextFileOffset = Long.parseLong(file.getName());
                    log.info("load " + file.getPath() + " OK");
                } catch (IOException e) {
                    log.error("load file " + file + " error", e);
                    return -1;
                }
            }
        }
        return mappedFiles.size();
    }

    /*****
     * 查询最后一个MappedFile
     * @param  findNoWillBeCreate 如果发现不存在则新建
     * @return
     * @description:
     ****/
    public MappedFile findLastMappedFile(Boolean findNoWillBeCreate) {
        if(mappedFiles.isEmpty()){
            if(findNoWillBeCreate){
                try {
                    MappedFile mappedFile = new MappedFile(this.storePath,nextFileOffset + mappedFileSize + "", mappedFileSize);
                    mappedFiles.add(mappedFile);
                } catch (Exception e) {
                    log.error("[findLastMappedFile]创建文件失败", e);
                    return null;
                }
            }
        }

        return mappedFiles.get(mappedFiles.size()-1);
    }

    /**********************
     * 这里是代表截断脏commitLog文件与设置对应的commitLog的存储偏移量
     * @param processOffset 最后一个文件的偏移量
     * @return
     * @description //TODO
     * @date 23:59 2020/10/31
    **********************/
    public void truncateDirtyFiles(long processOffset) {
        List<MappedFile> willRemoveFiles = new ArrayList<>();

        for (MappedFile file : this.mappedFiles) {
            long fileTailOffset = file.getFileFromOffset() + this.mappedFileSize;
            if (fileTailOffset > processOffset) {
                if (processOffset >= file.getFileFromOffset()) {
                    file.setWrotePos((int) (processOffset % this.mappedFileSize));
                    file.setCommittedPosition((int) (processOffset % this.mappedFileSize));
                    file.setFlushedPosition((int) (processOffset % this.mappedFileSize));
                } else {
                    //销毁mapperFile对象
              //    file.destroy(1000);
                    willRemoveFiles.add(file);
                }
            }
        }

        this.deleteExpiredFile(willRemoveFiles);
    }


    void deleteExpiredFile(List<MappedFile> files) {

        if (!files.isEmpty()) {

            Iterator<MappedFile> iterator = files.iterator();
            while (iterator.hasNext()) {
                MappedFile cur = iterator.next();
                if (!this.mappedFiles.contains(cur)) {
                    iterator.remove();
                    log.info("This mappedFile {} is not contained by mappedFiles, so skip it.", cur.getFileName());
                }
            }

            try {
                if (!this.mappedFiles.removeAll(files)) {
                    log.error("deleteExpiredFile remove failed.");
                }
            } catch (Exception e) {
                log.error("deleteExpiredFile has exception.", e);
            }
        }
    }

    /**********************
     * 物理刷盘
     * @param
     * @return
     * @description //TODO
     * @date 21:17 2020/11/4
    **********************/
    public boolean flush(int flushCommitLogLeastPages) {
        boolean result = true;
        MappedFile mappedFile = this.findMappedFileByOffset(this.flushedWhere, this.flushedWhere == 0);

        if (mappedFile != null) {
            long tmpTimeStamp = mappedFile.getStoreTimestamp();
            int offset = mappedFile.flush(flushCommitLogLeastPages);
            long where = mappedFile.getFileFromOffset() + offset;
            result = where == this.flushedWhere;
            this.flushedWhere = where;
            if (0 == flushCommitLogLeastPages) {
                this.storeTimestamp = tmpTimeStamp;
            }
        }
        return result;
    }

    /**********************
     * 根据偏移量来获取对应的mapperFile,如果每有则返回第一个
     * @param
     * @return
     * @description //TODO
     * @date 21:22 2020/11/4
    **********************/
    private MappedFile findMappedFileByOffset(long offset, boolean returnFirstOnNotFound) {
        if(mappedFileSize.equals(0))
        return null;

        MappedFile firstMappedFile = mappedFiles.get(0);
        MappedFile lastMappedFile = mappedFiles.get(mappedFiles.size() -1);

        try {
            //不正确的偏移量
            if(offset < firstMappedFile.getFileFromOffset() || offset >= lastMappedFile.getFileFromOffset() + mappedFileSize){
                log.warn("Offset not matched. Request offset: {}, firstOffset: {}, lastOffset: {}, mappedFileSize: {}, mappedFiles count: {}",
                        offset,
                        firstMappedFile.getFileFromOffset(),
                        lastMappedFile.getFileFromOffset() + this.mappedFileSize,
                        this.mappedFileSize,
                        this.mappedFiles.size());

            }else {
                //获取偏移量对应的下标
                int index = (int) ((offset / this.mappedFileSize) - (firstMappedFile.getFileFromOffset() / this.mappedFileSize));
                MappedFile targetFile = null;
                try {
                    targetFile = this.mappedFiles.get(index);
                } catch (Exception ignored) {
                }

                if (targetFile != null && offset >= targetFile.getFileFromOffset()
                        && offset < targetFile.getFileFromOffset() + this.mappedFileSize) {
                    return targetFile;
                }

                //保险措施
                for (MappedFile tmpMappedFile : this.mappedFiles) {
                    if (offset >= tmpMappedFile.getFileFromOffset()
                            && offset < tmpMappedFile.getFileFromOffset() + this.mappedFileSize) {
                        return tmpMappedFile;
                    }
                }
            }
        }catch (Exception e){
            log.info("查找mapperFile文件错误,e:[{}]", Throwables.getStackTraceAsString(e));
        }

        if(returnFirstOnNotFound){

            return firstMappedFile;
        }else {
            return null;
        }
    }
}
