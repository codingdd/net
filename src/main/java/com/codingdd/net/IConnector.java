package com.codingdd.net;

import com.codingdd.net.event.NetEvent;
import io.netty.channel.ChannelFutureListener;
import java.net.InetSocketAddress;

/**
 * 管理IClient的接口
 *
 * @author codingdd
 */
public interface IConnector extends IOService {

  void send(IChannel client, NetEvent event);

  IChannel connect2server(InetSocketAddress address);

  void connect2server(InetSocketAddress address, ChannelFutureListener listener);

  /**
   * 设置生成连接时的ID头
   */
  void setHeadId(byte headid);

}
