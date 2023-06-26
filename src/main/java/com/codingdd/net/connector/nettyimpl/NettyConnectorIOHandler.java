package com.codingdd.net.connector.nettyimpl;

import com.codingdd.net.event.NetEvent;
import com.codingdd.net.messagequeue.MessageQueue;
import com.codingdd.net.IChannel;
import com.codingdd.net.channel.impl.NettyChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : codingdd
 * @date : 2018/01/04
 */
public class NettyConnectorIOHandler extends ChannelInboundHandlerAdapter {

  public static AtomicLong num = new AtomicLong(0);
  private static Logger logger = LoggerFactory.getLogger(NettyConnectorIOHandler.class);
  private MessageQueue mq;
  private short head_id;

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    Attribute<IChannel> a = ctx.channel().attr(NettyChannel.CHANNEL);
    if (a.get() == null) {
      long id = num.getAndIncrement() | ((long) head_id << 48);
      logger.info("NettyConnectIOHandler.channelRegistered.channel,id:" + id);
      new NettyChannel(ctx.channel());
    }
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    Attribute<IChannel> a = ctx.channel().attr(NettyChannel.CHANNEL);
    if (a.get() == null) {
      long id = num.getAndIncrement() | ((long) head_id << 48);
      new NettyChannel(ctx.channel());
      logger.info("NettyConnectIOHandler.channelActive.channel,id:" + id);
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    try {
      IChannel channel = ctx.channel().attr(NettyChannel.CHANNEL).get();
      if (channel != null) {
        logger.info("sessionClosed:" + ctx + " channelId:" + channel.getId());
        mq.publishEvent(NetEvent.close, channel.getId());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    try {
      IChannel channel = ctx.channel().attr(NettyChannel.CHANNEL).get();
      mq.publishEvent(NetEvent.message, channel.getId(), msg);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    try {
      logger.error("exceptionCaught:" + ctx, cause);
      ctx.channel().close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setMq(MessageQueue mq) {
    this.mq = mq;
  }

  public void setHead_id(short head_id) {
    this.head_id = head_id;
  }
}
