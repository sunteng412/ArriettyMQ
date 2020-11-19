package com.mrfox.arrirtty.client;

import com.mrfox.arrirtty.remoting.BrokerNettyServer;
import com.mrfox.arrirtty.remoting.ClientNettyServer;
import lombok.extern.slf4j.Slf4j;

/*****
 * 启动类
 * @author     : MrFox
 * @date       : 2020-09-01 14:58
 * @description:
 * @version    :
 ****/
@Slf4j
public class ClientStarter {

    public static void main(String[] args) {
        //初始化netty
        ClientNettyServer clientNettyServer = new ClientNettyServer()
                .start();
    }
}
