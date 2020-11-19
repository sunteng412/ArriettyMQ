package com.mrfox.arrirtty.broker;

import com.mrfox.arrirtty.broker.properties.Conf;
import com.mrfox.arrirtty.common.properties.PropertiesTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/*****
 * 启动类
 * @author     : MrFox
 * @date       : 2020-09-01 14:58
 * @description:
 * @version    :
 ****/
@Slf4j
public class BrokerStarter {

    private static Conf conf = new Conf();

    public static void main(String[] args) {
        //注入属性
        injectProperties();
        new BrokerController(conf);
    }

    /*****
     * 注入properties文件配置
     * @param
     * @return
     * @description:
     ****/
    private static void injectProperties(){
        String path = System.getProperty("brokerConfPath", null);

        if(StringUtils.isBlank(path)){
            throw new RuntimeException("配置文件路径为空,使用-DbrokerConfPath=xxxx");
        }

        //初始化配置
        PropertiesTools propertiesTools = new PropertiesTools(path);
        propertiesTools.injectAttr(conf);
    }
}
