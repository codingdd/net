package com.codingdd.net.multiserver.impl;

import com.codingdd.net.Statistics.StatisticsV2;
import com.codingdd.net.messagequeue.IMessageHandler;
import com.codingdd.net.messagequeue.MessageQueue;
import com.codingdd.net.AbstractIOService;
import com.codingdd.net.INetService;
import com.codingdd.net.IServer;
import com.codingdd.net.NameThreadFactory;
import com.codingdd.net.multiserver.impl.codec.CodecFactory;
import io.netty.channel.ChannelFutureListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiServer extends AbstractIOService implements IServer {

  private static Logger logger = LoggerFactory.getLogger(MultiServer.class);
  private MessageQueue mq;
  private NioSocketAcceptor acceptor;
  /**
   * 处理除写事件外所有事件的线程池
   */
  private OrderedThreadPoolExecutor readThreadPool;
  /**
   * 处理写事件的线程池
   */
  private OrderedThreadPoolExecutor writeThreadPool;
  private boolean listening = false;

  private Map<String, Object> defaultparas = new HashMap<>();

  public MultiServer() {
    init();
  }

  public MultiServer(Map<String, Object> paras) {
    defaultparas.putAll(paras);
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

  private void init() {
    defaultparas = new HashMap<>();
    defaultparas.put(INetService.config_timeout, 10);
    defaultparas.put(INetService.config_timeout_close, true);
    defaultparas.put(INetService.config_head_id, 0);
  }

  public void stopNetListen() {
    logger.info("shutdown server:" + acceptor.getLocalAddress());
    if (this.acceptor != null) {
      this.acceptor.dispose();
    }
    this.readThreadPool.shutdown();
    this.writeThreadPool.shutdown();
    mq.shutDown();
    logger.info("shutdown server over:" + acceptor.getLocalAddress());
  }

  public void destroy() {
    if (!this.listening) {
      return;
    }

    if (this.acceptor != null) {
      this.acceptor.dispose();
    }
  }

  public boolean isClientChannelEmpty() {
    return acceptor.getManagedSessionCount() == 0;
  }

  @Override
  public void addChannelListener(ChannelFutureListener listener) {

  }

  /**
   * 在指定的端口开启监听服务
   */
  public void startNetListen(InetSocketAddress address, IMessageHandler handler) {
    if (isUsedPort(address.getPort())) {
      logger.error("the port:" + address.getPort() + "is used");
      return;
    }
    ProtocolCodecFactory pcf = new CodecFactory();
    try {
      this.listening = true;
      this.acceptor = new NioSocketAcceptor();
      /*
        设置端口监听的最大值为6000
       */
      this.acceptor.setBacklog(6000);
      this.acceptor.setReuseAddress(true);

      // 启动消息队列
      mq = new MessageQueue(handler);
      mq.start();

      boolean timeoutclose = getPara(INetService.config_timeout_close);
      int headid = getPara(INetService.config_head_id);
      /*
       * 设置消息处理器
       */
      MultiServerIOHandler ioHandler = new MultiServerIOHandler(mq, timeoutclose, (short) headid);
      ioHandler.setServer(MultiServer.this);
      this.acceptor.setHandler(ioHandler);

      DefaultIoFilterChainBuilder fcb = this.acceptor.getFilterChain();
      /*
       * 线程池过滤器,write事件不会被该过滤器执行，message_received,message_sent等事件会被该过滤器执行 该
       * 过滤器处于I/O边缘，可以防止IO线程阴塞
       */
      int readPoolCoreSize = Integer.valueOf(3);
      int readPoolMaxSize = Integer.valueOf(4);
      readThreadPool =
          new OrderedThreadPoolExecutor(readPoolCoreSize, readPoolMaxSize, 30, TimeUnit.SECONDS,
              new NameThreadFactory("read_pool-"));
      fcb.addLast("threadPool", new ExecutorFilter(readThreadPool));
      /**
       * 解码，编码过滤器
       */
      IoFilter protocol = new ProtocolCodecFilter(pcf);
      fcb.addLast("codec", protocol);
      /**
       * 专门处理写事件的过滤器
       */
      int writePoolCoreSize = Integer.valueOf(20);
      int writePoolMaxSize = Integer.valueOf(100);
      writeThreadPool =
          new OrderedThreadPoolExecutor(writePoolCoreSize, writePoolMaxSize, 30, TimeUnit.SECONDS,
              new NameThreadFactory("write_pool-"));
      fcb.addLast("write_thread_pool", new ExecutorFilter(writeThreadPool, IoEventType.WRITE));
      /**
       * 设置缓冲区的大小
       */
      int recsize = Integer.valueOf(10240);
      int sendsize = Integer.valueOf(20480);
      int timeout = this.getPara("timeout");

      SocketSessionConfig sessionConfig = acceptor.getSessionConfig();
      sessionConfig.setReuseAddress(true);
      sessionConfig.setReceiveBufferSize(recsize);
      sessionConfig.setSendBufferSize(sendsize);
      sessionConfig.setTcpNoDelay(true);
      sessionConfig.setSoLinger(0);
      sessionConfig.setIdleTime(IdleStatus.READER_IDLE, timeout);

      // 监控统计
      StatisticsV2 statistics = new StatisticsV2(mq);
      statistics.setNetStatistics(acceptor.getStatistics());
      this.setStatistics(statistics);

      this.acceptor.bind(address);
      logger.info("start server: " + address);
    } catch (IOException e) {
      logger.error("启动监听端口错误：" + address.getPort(), e);
      System.exit(1);
    }
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

}
