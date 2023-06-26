package com.codingdd.net.multiserver.nettyimpl;

import com.codingdd.net.Statistics.StatisticsV2;
import com.codingdd.net.messagequeue.IMessageHandler;
import com.codingdd.net.messagequeue.MessageQueue;
import com.codingdd.net.INetService;
import com.codingdd.net.IServer;
import com.codingdd.net.multiserver.impl.MultiServer;
import com.codingdd.net.multiserver.nettyimpl.codec.DecoderNetty;
import com.codingdd.net.multiserver.nettyimpl.codec.EncoderNetty;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : codingdd
 * @date : 2017/12/28
 */
public class NettyServer implements IServer {

  private static final Logger logger = LoggerFactory.getLogger(MultiServer.class);

  private MessageQueue mq;
  private EventLoopGroup bossGroup;
  private EventLoopGroup workGroup;

  private Map<String, Object> defaultparas = new HashMap<>();
  // 监听器
  private ConcurrentLinkedQueue<ChannelFutureListener> listeners = new ConcurrentLinkedQueue<>();

  public NettyServer() {

  }

  public NettyServer(Map<String, Object> paras) {
    defaultparas.putAll(paras);
  }

  public static void main(String[] args) {

  }

  private static void bindConnectionOptions(ServerBootstrap bootstrap) {
    bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
    bootstrap.option(ChannelOption.SO_RCVBUF, 10240);
    bootstrap.option(ChannelOption.SO_SNDBUF, 20480);
    bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    bootstrap.childOption(ChannelOption.SO_SNDBUF, 20480);
    bootstrap.childOption(ChannelOption.SO_RCVBUF, 10240);
    bootstrap.childOption(ChannelOption.SO_LINGER, 0);
    bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
//        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true); //调试用
//        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true); //心跳机制暂时使用TCP选项，之后再自己实现
    bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

  }

  /**
   * 尝试绑定指定的端口
   *
   * @throws Exception 如果商品被占用抛出异常
   */
  private static void tryBindPort(String host, int port) throws Exception {
    Socket s = new Socket();
    s.setSoLinger(false, 0);
    s.bind(new InetSocketAddress(host, port));
    s.close();
  }

  /**
   * 端口是否被绑定
   */
  private static boolean isUsedPort(int port) {
    try {
      tryBindPort("0.0.0.0", port);

      return false;
    } catch (Exception e) {
      logger.error("端口-" + port + "已经被占用");
    }
    return true;
  }

  @Override
  public void addChannelListener(ChannelFutureListener listener) {
    listeners.add(listener);
  }

  /**
   * 在指定的端口开启监听服务
   */
  public void startNetListen(InetSocketAddress address, IMessageHandler handler) throws Exception {
    if (isUsedPort(address.getPort())) {
      logger.error("the port:" + address.getPort() + "is used");
      return;
    }

    try {
      // 启动消息队列
      mq = new MessageQueue(handler);
      mq.start();

      final boolean timeout_close = getPara(INetService.config_timeout_close);
      final int head_id = getPara(INetService.config_head_id);

      bossGroup = new NioEventLoopGroup(1);
      workGroup = new NioEventLoopGroup();

      ServerBootstrap bootstrap = new ServerBootstrap().group(bossGroup, workGroup)
          .channel(NioServerSocketChannel.class)
          .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
              channel.id();

              NettyServerIoHandler serverIoHandler = new NettyServerIoHandler();
              serverIoHandler.setHeadid((short) head_id);
              serverIoHandler.setTimeoutclose(timeout_close);
              serverIoHandler.setMq(mq);
              serverIoHandler.setServer(NettyServer.this);

              ChannelPipeline pipeline = channel.pipeline();
              Integer timeout = NettyServer.this.getPara("timeout");
              if (timeout != null) {
                pipeline.addLast("idleHandler", new IdleStateHandler(timeout, timeout, timeout));
              }
              pipeline.addLast("MessageDecoder", new DecoderNetty());
              pipeline.addLast("MessageEncoder", new EncoderNetty());
              pipeline.addLast("ClientMessageHandler", serverIoHandler);
            }
          });

      bindConnectionOptions(bootstrap);
      //InetAddress inetAddress = InetAddress.getByName(address.getHostName());
      //bootstrap.option(ChannelOption.IP_MULTICAST_IF, NetworkInterface.getByInetAddress(inetAddress));
      ChannelFuture future = bootstrap.bind(address);
      for (ChannelFutureListener listener : listeners) {
        future.addListener(listener);
      }

      logger.info("start server: " + address);
    } catch (Throwable e) {
      logger.error("启动监听端口错误：" + address.getPort(), e);
      System.exit(1);
    }
  }

  @Override
  public void stopNetListen() {
    if (bossGroup != null) {
      bossGroup.shutdownGracefully();
    }
    if (workGroup != null) {
      workGroup.shutdownGracefully();
    }
  }

  @Override
  public boolean isClientChannelEmpty() {
    return workGroup.isShutdown() && bossGroup.isShutdown();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getPara(String key) {
    Object val = defaultparas.get(key);
    if (val == null) {
      return null;
    }
    return (T) val;
  }

  @Override
  public void changePara(String key, Object val) {
    defaultparas.put(key, val);
  }

  @Override
  public Map<String, Object> getParas() {
    return defaultparas;
  }

  @Override
  public void setPara(Map<String, Object> paras) {
    defaultparas = paras;
  }

  @Override
  public StatisticsV2 getStatistics() {
    return null;
  }

  @Override
  public void setStatistics(StatisticsV2 statistics) {

  }
}
