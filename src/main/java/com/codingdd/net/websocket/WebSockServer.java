package com.codingdd.net.websocket;

import com.codingdd.net.IServer;
import com.codingdd.net.Statistics.StatisticsV2;
import com.codingdd.net.messagequeue.IMessageHandler;
import com.codingdd.net.messagequeue.MessageQueue;
import com.codingdd.net.util.NetUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import java.net.InetSocketAddress;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : codingdd
 * @date : 2023/4/11
 */
public class WebSockServer implements IServer {

  private static final Logger logger = LoggerFactory.getLogger(WebSockServer.class);
  private MessageQueue mq;

  @Override
  public void addChannelListener(ChannelFutureListener listener) {

  }

  @Override
  public void startNetListen(InetSocketAddress address, IMessageHandler handler) {
    if (NetUtil.isUsedPort(address.getPort())) {
      logger.error("the port:" + address.getPort() + "is used");
      return;
    }
    mq = new MessageQueue(handler);
    mq.start();
    NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
    NioEventLoopGroup workGroup = new NioEventLoopGroup();
    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.group(bossGroup, workGroup)
        .channel(NioServerSocketChannel.class)
        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          public void initChannel(SocketChannel ch) throws Exception {
            WebSocketHandler webSocketHandler = new WebSocketHandler();
            webSocketHandler.setMq(mq);
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new HttpServerCodec()); // HTTP 协议解析，用于握手阶段
            pipeline.addLast(new HttpObjectAggregator(65536000)); // HTTP 协议解析，用于握手阶段
            pipeline.addLast(new WebSocketServerCompressionHandler()); // WebSocket 数据压缩扩展
            pipeline.addLast(
                new WebSocketServerProtocolHandler("/", null, true,
                    65536000)); // WebSocket 握手、控制帧处理
            pipeline.addLast(new WebSocketEncoder());
            pipeline.addLast(webSocketHandler);
          }
        });
    bindConnectionOptions(bootstrap);
    ChannelFuture future = bootstrap.bind(address);
    logger.info("start server: " + address);
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
  @Override
  public void stopNetListen() {

  }

  @Override
  public boolean isClientChannelEmpty() {
    return false;
  }

  @Override
  public void changePara(String key, Object val) {

  }

  @Override
  public Map<String, Object> getParas() {
    return null;
  }

  @Override
  public <T> T getPara(String key) {
    return null;
  }

  @Override
  public void setPara(Map<String, Object> paras) {

  }

  public static void main(String[] args) throws InterruptedException {
//    EventLoopGroup bossGroup = new NioEventLoopGroup();
//    EventLoopGroup workerGroup = new NioEventLoopGroup();
//    try {
//      ServerBootstrap b = new ServerBootstrap();
//      b.group(bossGroup, workerGroup)
//          .channel(NioServerSocketChannel.class)
//          .option(ChannelOption.TCP_NODELAY, true)
//          .childHandler(new ChannelInitializer<SocketChannel>() {
//            @Override
//            public void initChannel(SocketChannel ch) throws Exception {
//              ChannelPipeline pipeline = ch.pipeline();
//              pipeline.addLast(new HttpServerCodec()); // HTTP 协议解析，用于握手阶段
//              pipeline.addLast(new HttpObjectAggregator(65536000)); // HTTP 协议解析，用于握手阶段
//              pipeline.addLast(new WebSocketServerCompressionHandler()); // WebSocket 数据压缩扩展
//              pipeline.addLast(
//                  new WebSocketServerProtocolHandler("/", null, true,
//                      65536000)); // WebSocket 握手、控制帧处理
//              pipeline.addLast(new WebSocketHandler());
//            }
//          });
//      ChannelFuture f = b.bind(8082).sync();
//      f.channel().closeFuture().sync();
//    } finally {
//      workerGroup.shutdownGracefully();
//      bossGroup.shutdownGracefully();
//  }
  }

  @Override
  public void setStatistics(StatisticsV2 statistics) {

  }

  @Override
  public StatisticsV2 getStatistics() {
    return null;
  }
}
