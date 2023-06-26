package com.codingdd.net.connector.impl;

import com.codingdd.net.event.NetEvent;
import com.codingdd.net.messagequeue.MessageQueue;
import com.codingdd.net.IChannel;
import com.codingdd.net.channel.impl.Channel;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class ConnectorIOHandler implements IoHandler {

    private MessageQueue mq;
    private final short headid;

    public ConnectorIOHandler(MessageQueue mq, short headid) {
        this.mq = mq;
        this.headid = headid;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        IChannel client = new Channel(session);
        long id = session.getId() | ((long) headid << 46);
        session.setAttribute("channelid", id);
        mq.publishEvent(NetEvent.connect, client);

    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        long id = (long) session.getAttribute("channelid");
        mq.publishEvent(NetEvent.close, id);

    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        // TODO Auto-generated method stub
        cause.printStackTrace();
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        long id = (long) session.getAttribute("channelid");
        mq.publishEvent(NetEvent.message, id, message);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {

    }

}
