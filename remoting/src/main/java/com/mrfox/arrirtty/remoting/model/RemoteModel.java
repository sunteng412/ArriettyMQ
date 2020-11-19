package com.mrfox.arrirtty.remoting.model;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/*****
 * 远程传输实体统一实体对象
 * @author     : MrFox
 * @date       : 2020-09-02 15:53
 * @description:
 * @version    :
 ****/
@Getter
@Setter
public class RemoteModel {

    /**
     * 自定义属性
     * */
    private Map<String,String> extFields;

    /**
     * 行为属性
     * */
    private RemoteActionEnum remoteActionEnum;

    /**
     * 类型属性
     * */
    private RemoteTypeEnum remoteTypeEnum;

    /**
     * 内容
     * */
    private byte[] content;

    /**
     * 响应编码
     * */
    private Integer code;

    /**
     * 响应结果
     * */
    private Boolean isSuccess;

    /**
     * 消息时间戳
     * */
    private Long bornTimestamp;

    /*****
     * 解码
     * @param  byteBuffer bb
     * @return
     * @description:
     ****/
    public static Object decode(ByteBuffer byteBuffer) {
        int limit = byteBuffer.limit();
        byte[] bytes = new byte[limit];
        if(limit > 0 ){
            byteBuffer.get(bytes);
            return JSON.parseObject(new String(bytes, Charset.defaultCharset()), RemoteModel.class);
        }
        return null;
    }

    @Override
    public String toString() {
        return "RemoteModel{" +
                "extFields=" + extFields +
                ", remoteTypeEnum=" + remoteActionEnum +
                ", content=" + new String(content, StandardCharsets.UTF_8) +
                '}';
    }

    public static RemoteModel buildGeneralResponse(int systemError){
        RemoteModel remoteModel = new RemoteModel();
        remoteModel.setIsSuccess(Boolean.FALSE);
        remoteModel.setCode(systemError);
        return remoteModel;
    }
}
