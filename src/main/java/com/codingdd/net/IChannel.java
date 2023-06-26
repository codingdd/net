package com.codingdd.net;

import io.netty.channel.ChannelFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * 单独的一个连接的接口
 *
 * @author codingdd
 */
public interface IChannel {

  void send(IoBuffer buffer);

  ChannelFuture sendFuture(IoBuffer buffer);

  //session的ID
  String getId();

  /**
   * @return 连接绑定的角色ID
   */
  long getUid();

  void setUid(long uid);

  boolean closeChannel();

  boolean isConnected();

  String getLoginIp();

  AtomicInteger getStatus();
}
