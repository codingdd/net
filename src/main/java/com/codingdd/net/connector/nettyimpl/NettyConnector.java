package com.codingdd.net.connector.nettyimpl;

import com.codingdd.net.Statistics.StatisticsV2;
import com.codingdd.net.event.NetEvent;
import com.codingdd.net.messagequeue.IMessageHandler;
import com.codingdd.net.messagequeue.MessageQueue;
import com.codingdd.net.IChannel;
import com.codingdd.net.IConnector;
import com.codingdd.net.channel.impl.NettyChannel;
import com.codingdd.net.multiserver.nettyimpl.codec.DecoderNetty;
import com.codingdd.net.multiserver.nettyimpl.codec.EncoderNetty;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : codingdd
 * @date : 2018/01/04
 */
public class NettyConnector implements IConnector {

  static Logger logger = LoggerFactory.getLogger(NettyConnector.class);
  Bootstrap bootstrap;

  public NettyConnector(IMessageHandler messageHandler, final short head_id) {
    NioEventLoopGroup group = new NioEventLoopGroup(1);

    final MessageQueue mq = new MessageQueue(messageHandler);
    mq.start();
    // 改为Bootstrap，且构造函数变化很大，这里用无参构造。
    bootstrap = new Bootstrap();
    // 指定EventLoopGroup
    bootstrap.group(group);
    // 指定channel类型
    bootstrap.channel(NioSocketChannel.class);
    // 指定Handler
    bootstrap.handler(new ChannelInitializer<SocketChannel>() {
      @Override
      protected void initChannel(SocketChannel ch) throws Exception {
        //解码
        ch.pipeline().addLast(new EncoderNetty());
        ch.pipeline().addLast(new DecoderNetty());
        NettyConnectorIOHandler handler = new NettyConnectorIOHandler();
        handler.setHead_id(head_id);
        handler.setMq(mq);
        ch.pipeline().addLast(handler);
      }
    });
    //设置TCP协议的属性
    bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
    bootstrap.option(ChannelOption.TCP_NODELAY, true);
    bootstrap.option(ChannelOption.SO_RCVBUF, 20480);
    bootstrap.option(ChannelOption.SO_SNDBUF, 10240);

  }

  public static void main(String[] args) {
    ListeningExecutorService service = MoreExecutors
        .listeningDecorator(Executors.newFixedThreadPool(2));
    ListenableFuture future = service.submit(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(2000L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName());
        System.out.println("");
      }
    });
//    Futures.addCallback(future, new FutureCallback<Integer>() {
//      @Override
//      public void onSuccess(Integer s) {
//        System.out.println(s);
//        System.out.println(Thread.currentThread().getName());
//
//      }
//
//      @Override
//      public void onFailure(Throwable throwable) {
//
//      }
//    });
  }

  @Override
  public void send(IChannel client, NetEvent event) {

  }

  @Override
  public IChannel connect2server(final InetSocketAddress address) {
    try {
      final ChannelFuture future = bootstrap.connect(address);
      future.await(2000L);
      if (future.isSuccess()) {
        logger.info("connect success : " + address);
        return new NettyChannel(future.channel());
      } else {
        logger.info("connect timeout : " + address);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void connect2server(final InetSocketAddress address, ChannelFutureListener listener) {
    try {
      final ChannelFuture future = bootstrap.connect(address);
      future.addListener(listener);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 设置生成连接时的ID头
   */
  @Override
  public void setHeadId(byte headid) {

  }

  @Override
  public StatisticsV2 getStatistics() {
    return null;
  }

  @Override
  public void setStatistics(StatisticsV2 statistics) {

  }
}
