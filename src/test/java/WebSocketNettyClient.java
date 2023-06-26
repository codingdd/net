import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.stream.ChunkedWriteHandler;
import java.net.URI;

public class WebSocketNettyClient {

  public static void main(String[] args) {

    EventLoopGroup group = new NioEventLoopGroup();
    final ClientHandler handler = new ClientHandler();
    try {
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(group).channel(NioSocketChannel.class)
          .option(ChannelOption.TCP_NODELAY, true)
          .option(ChannelOption.SO_KEEPALIVE, true)
          .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
              ChannelPipeline pipeline = ch.pipeline();
              // 添加一个http的编解码器
              pipeline.addLast(new HttpClientCodec());
              // 添加一个用于支持大数据流的支持
              pipeline.addLast(new ChunkedWriteHandler());
              // 添加一个聚合器，这个聚合器主要是将HttpMessage聚合成FullHttpRequest/Response
              pipeline.addLast(new HttpObjectAggregator(1024 * 64));

              pipeline.addLast(handler);

            }
          });

      URI websocketURI = new URI("ws://localhost:8888");
      HttpHeaders httpHeaders = new DefaultHttpHeaders();
      //进行握手
      WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
          websocketURI, WebSocketVersion.V13, (String) null, true, httpHeaders);
      final Channel channel = bootstrap.connect(websocketURI.getHost(), websocketURI.getPort())
          .sync().channel();
      handler.setHandshaker(handshaker);
      handshaker.handshake(channel);
      //阻塞等待是否握手成功
      handler.handshakeFuture().sync();
      System.out.println("握手成功");

      //发送消息
      var str = "12345678901234567890123456789012345678901234567890123456789012345678901234567890"
          + "12345678901234567890123456789012345678901234567890123456789012345678901234567890"
          + "12345678901234567890123456789012345678901234567890123456789012345678901234567890"
          + "12345678901234567890123456789012345678901234567890123456789012345678901234567890"
          + "12345678901234567890123456789012345678901234567890123456789012345678901234567890"
          + "12345678901234567890123456789012345678901234567890123456789012345678901234567890"
          + "12345678901234567890123456789012345678901234567890123456789012345678901234567890"
          + "12345678901234567890123456789012345678901234567890123456789012345678901234567890"
          + "12345678901234567890123456789012345678901234567890123456789012345678901234567890"
          + "12345678901234567890123456789012345678901234567890123456789012345678901234567890";
      for (int i = 0; i < 10; i++) {
        str += str;
      }
      var buff = Unpooled.buffer().writeBytes(str.getBytes());
      channel.writeAndFlush(new BinaryWebSocketFrame(true, 0, buff));
//      channel.writeAndFlush(new ContinuationWebSocketFrame(true, 0, "client-2"));
//      channel.writeAndFlush(new BinaryWebSocketFrame(buff));
      System.out.println("send len =" + buff.readableBytes());
      // 等待连接被关闭
      channel.closeFuture().sync();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {

      group.shutdownGracefully();
    }

  }

}