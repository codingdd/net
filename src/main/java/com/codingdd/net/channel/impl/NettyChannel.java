package com.codingdd.net.channel.impl;

import com.codingdd.net.IChannel;
import com.codingdd.net.multiserver.nettyimpl.NettyServerIoHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.mina.core.buffer.IoBuffer;
import io.netty.channel.Channel;


/**
 * @author : codingdd
 * @date : 2017/12/28
 */
public class NettyChannel implements IChannel {

  public final static AttributeKey<IChannel> CHANNEL = AttributeKey.valueOf("channel");
  private long uid;

  private final AtomicInteger status = new AtomicInteger(0);

  private final Channel session;

  public NettyChannel(Channel session) {
    this.session = session;
    if (session != null) {
      session.attr(NettyChannel.CHANNEL).set(NettyChannel.this);
    }
  }

  @Override
  public void send(IoBuffer buffer) {
    if (session.isActive() && session.isWritable()) {
      session.writeAndFlush(buffer);
    }
  }

  @Override
  public ChannelFuture sendFuture(IoBuffer buffer) {
    if (session.isActive() && session.isWritable()) {
      return session.writeAndFlush(buffer);
    }
    return null;
  }

  @Override
  public String getId() {
    return session.id().asShortText();
  }

  public long getUid() {
    return uid;
  }

  public void setUid(long uid) {
    this.uid = uid;
  }

  public boolean isConnected() {
    return session != null && session.isOpen();
  }

  @Override
  public String toString() {
    return this.session.toString();
  }

  @Override
  public boolean closeChannel() {
    try {
      // 连接已经关闭
      if (!session.isOpen()) {
        return false;
      }
      ChannelFuture f = session.close();
      if (f.await(100)) {
        return f.isCancelled();
      }
      return f.isCancelled();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public String getLoginIp() {
    String result = "";
    if (session != null) {
      Object ipObj = session.attr(NettyServerIoHandler.IP).get();
//            Object ipObj = session.getAttribute("KEY_SESSION_CLIENT_IP");
      if (ipObj != null) {
        return (String) ipObj;
      }
    }
    if (session != null && session.isOpen()) {

      SocketAddress address = session.remoteAddress();
      if (address != null && address instanceof InetSocketAddress) {
        InetSocketAddress inetAddress = (InetSocketAddress) address;
        result = inetAddress.getHostString();
      }
    }
    return result;
  }

  @Override
  public AtomicInteger getStatus() {
    return status;
  }
}
