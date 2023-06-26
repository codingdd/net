package com.codingdd.net;

import com.codingdd.net.messagequeue.IMessageHandler;
import com.codingdd.net.connector.nettyimpl.NettyConnector;
import com.codingdd.net.multiserver.nettyimpl.NettyServer;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetService implements INetService {

  final Logger logger = LoggerFactory.getLogger(NetService.class);
  IServer server = null;

  @Override
  public IServer startMutiServer(InetSocketAddress address, IMessageHandler handler,
      Map<String, Object> paras)
      throws Exception {
//        server = new MultiServer(paras);
    server = new NettyServer(paras);
    server.startNetListen(address, handler);
    return server;
  }

  @Override
  public IServer startMutiServer(Map<String, Object> paras)
      throws Exception {
    server = new NettyServer(paras);
    return server;
  }

  @Override
  public IConnector startConnector(IMessageHandler handler, short headid) {
    IConnector connector = new NettyConnector(handler, headid);
    return connector;
  }

  @Override
  public IChannel connect2Server(IConnector connector, InetSocketAddress address) {
    return connector.connect2server(address);
  }

  @Override
  public void send(IChannel client, IoBuffer buffer) {
    if (client != null) {
      client.send(buffer);
    } else {
    }
  }

  @Override
  public void broadcast(Collection<IChannel> list, IoBuffer buffer) {
    for (IChannel channel : list) {
      this.send(channel, buffer);
    }
  }

  /**
   * 关闭acceptor,关闭IoProcessor,断开连接
   */
  @Override
  public void stopMutiServer() {
    logger.info("关闭server:" + server.toString());
    server.stopNetListen();
  }

  @Override
  public void addPara(String key, Object val) {
    if (server != null) {
      server.changePara(key, val);
    }
  }
}
