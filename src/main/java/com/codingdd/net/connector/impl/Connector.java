package com.codingdd.net.connector.impl;

import com.codingdd.net.Statistics.StatisticsV2;
import com.codingdd.net.event.NetEvent;
import com.codingdd.net.messagequeue.IMessageHandler;
import com.codingdd.net.messagequeue.MessageQueue;
import com.codingdd.net.AbstractIOService;
import com.codingdd.net.IChannel;
import com.codingdd.net.IConnector;
import com.codingdd.net.channel.impl.Channel;
import com.codingdd.net.multiserver.impl.codec.CodecFactory;
import io.netty.channel.ChannelFutureListener;
import java.net.InetSocketAddress;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class Connector extends AbstractIOService implements IConnector {
    NioSocketConnector connector = new NioSocketConnector();


    public Connector(IMessageHandler messageHandler, short head_id) {
        /**
         * 设置缓冲区的大小
         */
        int recsize = Integer.valueOf(10240);
        int sendsize = Integer.valueOf(20480);

        SocketSessionConfig sessionConfig = connector.getSessionConfig();
        sessionConfig.setReuseAddress(true);
        sessionConfig.setReceiveBufferSize(recsize);
        sessionConfig.setSendBufferSize(sendsize);
        sessionConfig.setTcpNoDelay(true);
        sessionConfig.setSoLinger(0);
        sessionConfig.setIdleTime(IdleStatus.BOTH_IDLE, 0);

        connector.setConnectTimeoutMillis(1000 * 60);
        ProtocolCodecFactory pcf = new CodecFactory();
        DefaultIoFilterChainBuilder fcb = this.connector.getFilterChain();
        IoFilter protocol = new ProtocolCodecFilter(pcf);
        fcb.addLast("codec", protocol);


        MessageQueue mq = new MessageQueue(messageHandler);
        connector.setHandler(new ConnectorIOHandler(mq, head_id));

        // 监控统计
        StatisticsV2 statistics = new StatisticsV2(mq);
        statistics.setNetStatistics(connector.getStatistics());
        this.setStatistics(statistics);

        mq.start();
    }

    @Override
    public IChannel connect2server(InetSocketAddress address) {
        ConnectFuture f = connector.connect(address);
        try {
            if (f.await(5000l)) {
                if (f.isConnected()) {
                    IChannel client = new Channel(f.getSession());
                    return client;
                } else {
                    return null;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void connect2server(InetSocketAddress address, ChannelFutureListener listener) {
    }

    @Override
    public void send(IChannel client, NetEvent event) {
    }

    @Override
    public void setHeadId(byte headid) {
    }
}
