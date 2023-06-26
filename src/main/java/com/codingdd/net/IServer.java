package com.codingdd.net;

import com.codingdd.net.messagequeue.IMessageHandler;
import io.netty.channel.ChannelFutureListener;
import java.net.InetSocketAddress;
import java.util.Map;

public interface IServer extends IOService {

    void addChannelListener(ChannelFutureListener listener);

    void startNetListen(InetSocketAddress address, IMessageHandler handler) throws Exception;

    void stopNetListen();
    
    boolean isClientChannelEmpty();

    void changePara(String key, Object val);

    Map<String, Object> getParas();

    <T> T getPara(String key);

    void setPara(Map<String, Object> paras);

}
