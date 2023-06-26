package com.codingdd.net.channel.impl;

import com.codingdd.net.IChannel;
import io.netty.channel.ChannelFuture;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.session.IoSession;

/**
 * <pre>
 *                +-------------------+                       +--------+
 * remoteNodeId ->|     channel       |                       |  Node  |
 *                +-------------------+                       +--------+
 *                          |                                      ^
 *                          |    routeNodeId                       |
 *                          +--------------------------------------+
 *  channel 有三个节点ID
 *
 *   remoteNodeId : channel另一端所连接的节点
 *   currNodeId :   持有该channel对象的节点
 *   routeNodeId:   channel可以路由到的节点
 *
 * @author codingdd
 */
public class Channel implements IChannel {

  private byte remoteNodeId;

  private byte routeNodeId;

  private long uid;

  private long accountID;

  private final IoSession session;

  public Channel(IoSession session) {
    this.session = session;
  }

  @Override
  public void send(IoBuffer buffer) {
    session.write(buffer);
  }

  @Override
  public ChannelFuture sendFuture(IoBuffer buffer) {
    return null;
  }

  public byte getRemoteNodeId() {
    return remoteNodeId;
  }

  public void setRemoteNodeId(byte remoteNodeId) {
    this.remoteNodeId = remoteNodeId;
  }

  public byte getRouteNodeId() {
    return routeNodeId;
  }

  public void setRouteNodeId(byte routeNodeId) {
    this.routeNodeId = routeNodeId;
  }

  @Override
  public String getId() {
    return this.session.getAttribute("channelid").toString();
  }

  public long getUid() {
    return uid;
  }

  public void setUid(long uid) {
    this.uid = uid;
  }

  public boolean isConnected() {
    return session != null && session.isConnected();
  }

  @Override
  public String toString() {
    return this.session.toString();
  }

  @Override
  public boolean closeChannel() {
    try {
      // 连接已经关闭
      if (!session.isConnected()) {
        return false;
      }
      CloseFuture f = session.close(true);
      if (f.await(100)) {
        return f.isClosed();
      }
      return f.isClosed();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public String getLoginIp() {
    String result = "";
    if (session != null) {
      Object ipObj = session.getAttribute("KEY_SESSION_CLIENT_IP");
      if (ipObj != null) {
        return (String) ipObj;
      }
    }
    if (session != null && session.isConnected()) {

      SocketAddress address = session.getRemoteAddress();
      if (address instanceof InetSocketAddress inetAddress) {
        result = inetAddress.getHostString();
      }
    }
    return result;
  }

  @Override
  public AtomicInteger getStatus() {
    return null;
  }

}
