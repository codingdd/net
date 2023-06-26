package com.codingdd.net.multiserver.impl;

import com.codingdd.net.event.NetEvent;
import com.codingdd.net.messagequeue.MessageQueue;
import com.codingdd.net.IChannel;
import com.codingdd.net.INetService;
import com.codingdd.net.IServer;
import com.codingdd.net.channel.impl.Channel;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IO 数据流的处理接口
 *
 * @author codingdd
 */
public class MultiServerIOHandler implements IoHandler {
    public static AtomicLong num = new AtomicLong(0);
    static Logger logger = LoggerFactory.getLogger(MultiServerIOHandler.class);
    public long time;
    private MessageQueue mq;
    private boolean timeoutclose;
    private short headid;
    private IServer server;

    protected MultiServerIOHandler(MessageQueue mq, boolean timeoutclose, short headid) {
        this.mq = mq;
        this.timeoutclose = timeoutclose;
        this.headid = headid;
    }

    @Override
    public void exceptionCaught(IoSession arg0, Throwable arg1) throws Exception {
        logger.error("exceptionCaught:" + arg0, arg1);
    }

    @Override
    public void messageReceived(IoSession arg0, Object arg1) throws Exception {
        long id = (long) arg0.getAttribute("channelid");
        mq.publishEvent(NetEvent.message, id, arg1);
    }

    @Override
    public void messageSent(IoSession arg0, Object arg1) throws Exception {

    }

    @Override
    public void sessionClosed(IoSession arg0) throws Exception {
        Object attri = arg0.getAttribute("channelid");
        logger.info("sessionClosed:" + arg0 + " channelId:" + attri);
        if (attri != null) {
            long id = (long) attri;
            mq.publishEvent(NetEvent.close, id);
        }
    }

    @Override
    public void sessionCreated(IoSession arg0) throws Exception {
        try {
            String clientIP = ((InetSocketAddress) arg0.getRemoteAddress()).getAddress().getHostAddress();
            arg0.setAttribute("KEY_SESSION_CLIENT_IP", clientIP);
            logger.info("sessionCreated:" + arg0);
            logger.info("sessionCreated: ip:" + clientIP);
            Set<String> black_ips = server.getPara(INetService.config_black_ip);
            if (black_ips != null) {
                for (String ip : black_ips) {
                    if (ip.equals(clientIP)) {
                        arg0.close(true);
                    }
                }
            }
        } catch (Exception e) {
            logger.info("sessionCreated.no_ip:" + arg0);
        }
        Boolean isFrontServerCheck = server.getPara(INetService.is_limit_access);
        if (isFrontServerCheck) {
            HashSet<String> ips = server.getPara(INetService.config_ip_list);
            InetSocketAddress address = (InetSocketAddress) arg0.getRemoteAddress();
            String ip = address.getAddress().getHostAddress();
            // 仅有列表中的IP可以连接
            if (ips == null || ips.isEmpty() || !ips.contains(ip)) {
                arg0.close(true);
                return;
            }
        }

        IChannel client = new Channel(arg0);
        long id = arg0.getId() | ((long) headid << 48);
        logger.info("sessionCreated.create.channel,id:" + id);
        arg0.setAttribute("channelid", id);
        mq.publishEvent(NetEvent.connect, client);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus idleStatus) throws Exception {
        if (timeoutclose) {
            logger.info("sessionIdle:空闲关闭" + session);
            session.close(true);
        }

    }

    protected IServer getServer() {
        return server;
    }

    protected void setServer(IServer server) {
        this.server = server;
    }

    @Override
    public void sessionOpened(IoSession arg0) throws Exception {

    }

    public MessageQueue getHandler() {
        return mq;
    }

    public void setHandler(MessageQueue handler) {
        this.mq = handler;
    }

}
