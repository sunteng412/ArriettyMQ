package com.mrfox.arrirtty.store.service;

import com.mrfox.arrirtty.store.properties.StoreConf;
import com.mrfox.arrirtty.store.service.store.DefaultStoreServiceImpl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/*****
 * 文件抽象层
 * @author     : MrFox
 * @date       : 2020-09-14 14:33
 * @description:
 * @version    :
 ****/
@Slf4j
public abstract class AbstractFile {

    /***
     * 文件映射 集合
     */
    @Getter
    public MappedFileQueue mappedFileQueue;

    @Getter
    public DefaultStoreServiceImpl defaultStoreService;

    public AbstractFile(String storePath,Long mappedFileSize,DefaultStoreServiceImpl defaultStoreService) {
        this.defaultStoreService = defaultStoreService;
        this.mappedFileQueue = new MappedFileQueue(storePath,mappedFileSize);
    }


    /*****
     * 加载
     * @param
     * @return
     * @description:
     ****/
    public Integer load0(){
        return mappedFileQueue.load();
    }


    /*****
     * 查询最后一个MappedFile
     * @param  findNoWillBeCreate 如果发现不存在则新建
     * @return
     * @description:
     ****/
    MappedFile findLastMappedFile(Boolean findNoWillBeCreate){
        return mappedFileQueue.findLastMappedFile(findNoWillBeCreate);
    }



}
