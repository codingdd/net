package com.codingdd.net.multiserver.nettyimpl;

import com.codingdd.net.event.NetEvent;
import com.codingdd.net.messagequeue.MessageQueue;
import com.codingdd.net.IChannel;
import com.codingdd.net.INetService;
import com.codingdd.net.IServer;
import com.codingdd.net.channel.impl.NettyChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : codingdd
 * @date : 2017/12/28
 */
public class NettyServerIoHandler extends ChannelInboundHandlerAdapter {

  public static AttributeKey<String> IP = AttributeKey.valueOf("client_remote_ip");
  public static AtomicLong num = new AtomicLong(0);
  private static final Logger logger = LoggerFactory.getLogger(NettyServerIoHandler.class);
  private MessageQueue mq;
  private boolean timeoutclose;
  private short headid;
  private IServer server;

  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    try {
      String clientIP = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress()
          .getHostAddress();
      ctx.channel().attr(IP).set(clientIP);
      logger.info("channelActive:" + ctx);
      logger.info("channelActive: ip:" + clientIP);
      Set<String> black_ips = server.getPara(INetService.config_black_ip);
      if (black_ips != null) {
        if (black_ips.contains(clientIP)) {
          logger.info("channelActive.create.channel,ip is in black list:" + clientIP);
          ctx.close();
        }
      }
    } catch (Exception e) {
      logger.info("channelActive.no_ip:" + ctx);
    }
    try {
      Boolean isFrontServerCheck = server.getPara(INetService.is_limit_access);
      if (isFrontServerCheck) {
        HashSet<String> ips = server.getPara(INetService.config_ip_list);
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        String ip = address.getAddress().getHostAddress();
        // 仅有列表中的IP可以连接
        if (ips == null || ips.isEmpty() || !ips.contains(ip)) {
          ctx.close();
          return;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    IChannel client = new NettyChannel(ctx.channel());
    logger.info("channelActive.create.channel,id:" + client.getId());
    mq.publishEvent(NetEvent.connect, client);
  }

  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    IChannel channel = ctx.channel().attr(NettyChannel.CHANNEL).get();
    var channelId = Optional.ofNullable(channel).map(IChannel::getId).orElse("");
    logger.info("channelInactive:" + ctx + " channelId:" + channelId);
    if (channel != null) {
      mq.publishEvent(NetEvent.close, channel);
    } else {

    }
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent event) {  // 2
      if (timeoutclose) {
        if (event.state() == IdleState.READER_IDLE || event.state() == IdleState.WRITER_IDLE
            || event.state() == IdleState.ALL_IDLE) {
          ctx.close();
        }
      }
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    try {
      logger.info("exceptionCaught:" + ctx);
      logger.error(cause.getMessage());
      ctx.channel().close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    IChannel channel = ctx.channel().attr(NettyChannel.CHANNEL).get();
    if (channel == null) {
      ctx.channel().close();
    } else {
      mq.publishEvent(NetEvent.message, channel, msg);
    }
  }

  public void setMq(MessageQueue mq) {
    this.mq = mq;
  }

  public void setTimeoutclose(boolean timeoutclose) {
    this.timeoutclose = timeoutclose;
  }

  public void setHeadid(short headid) {
    this.headid = headid;
  }

  public IServer getServer() {
    return server;
  }

  public void setServer(IServer server) {
    this.server = server;
  }
}
