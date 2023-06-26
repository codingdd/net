package com.codingdd.net.messagequeue;

import com.codingdd.net.event.NetEvent;
import com.lmax.disruptor.EventHandler;

/**
 * 消息处理器，消息是IO数据封装而成,启动服务后要实现该接口处理消息
 * 
 * @author codingdd
 * 
 */
public interface IMessageHandler extends EventHandler<NetEvent> {
    public void startStatistics();
}
