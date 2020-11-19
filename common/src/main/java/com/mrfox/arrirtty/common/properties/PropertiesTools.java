package com.mrfox.arrirtty.common.properties;

import com.mrfox.arrirtty.common.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/*****
 * 读取properties文件的工具类
 * @author     : MrFox
 * @date       : 2020-09-03 17:22
 * @description:
 * @version    :
 ****/
@Slf4j
public class PropertiesTools {

	private Properties p;

	/**
	 * 读取properties配置文件信息
	 */

	public PropertiesTools(String path) {
		p = new Properties();
		try {
			p.load( new BufferedInputStream(new FileInputStream(path)));
		} catch (IOException e) {
			log.error("[PropertiesTools]配置文件路径不存在");
		}
	}

	/**
	 * 根据key得到value的值
	 */
	public String getValue(String key) {
		return this.p.getProperty(key);
	}

	/**
	 * 将同名属性封装进对象
	 * */
	public void injectAttr(Object obj){
		//获取对象对应的
		BeanUtils.copyMapProperties(p,obj);
	}
}