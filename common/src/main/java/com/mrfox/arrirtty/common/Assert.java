package com.mrfox.arrirtty.common;

import com.mrfox.arrirtty.common.constants.CustomExceptionErrorMsg;
import com.mrfox.arrirtty.common.exception.CustomBusinessException;

import java.util.Objects;

/***********************
 * 断言工具类
 * @author MrFox
 * @date 2020/9/13 17:10
 * @version 1.0
 * @description
 ************************/
public class Assert {

    /**********************
     * 校验不能为空
     * @param
     * @return
     * @description //TODO
     * @date 17:17 2020/9/13
    **********************/
    public static void notNull(Object obj){
        if(Objects.isNull(obj)){
            throw new CustomBusinessException(CustomExceptionErrorMsg.OBJ_IS_NULL);
        }
    }

    public static void notNull(Object obj, String msg) {
        if(Objects.isNull(obj)){
            throw new CustomBusinessException(msg);
        }
    }
}
