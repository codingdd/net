package com.codingdd.net.websocket;

import com.codingdd.net.IChannel;
import com.codingdd.net.channel.impl.NettyChannel;
import com.codingdd.net.event.NetEvent;
import com.codingdd.net.messagequeue.MessageQueue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.util.Optional;
import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : codingdd
 * @date : 2023/4/20
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

  public static AttributeKey<String> IP = AttributeKey.valueOf("client_remote_ip");
  private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
  private MessageQueue mq;
  private static final AttributeKey<ByteBuf> key = AttributeKey.valueOf("buff");

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
    IChannel channel = ctx.channel().attr(NettyChannel.CHANNEL).get();
    if (channel == null) {
      ctx.channel().close();
      return;
    }
    if (frame instanceof BinaryWebSocketFrame || frame instanceof TextWebSocketFrame
        || frame instanceof ContinuationWebSocketFrame) {
      boolean isFinale = frame.isFinalFragment();
      var readable = frame.content().readableBytes();
      ByteBuf buf = ctx.channel().attr(key).get();
      if (isFinale) {
        int allocate = readable;
        if (buf != null) {
          allocate = readable + buf.readableBytes();
        }
        var newBuf = IoBuffer.allocate(allocate);
        if (buf != null) {
          buf.readBytes(newBuf.array(), 0, buf.readableBytes());
          buf.release();
        }
        frame.content().readBytes(newBuf.array(), allocate - readable, readable);
        mq.publishEvent(NetEvent.message, channel, newBuf);
      } else {
        if (buf == null) {
          buf = Unpooled.buffer(readable);
          ctx.channel().attr(key).set(buf);
        }
        buf.writeBytes(frame.content());
      }
    }
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    try {
      String clientIP = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress()
          .getHostAddress();
      ctx.channel().attr(IP).set(clientIP);
      logger.info("channelActive:" + ctx);
      logger.info("channelActive: ip:" + clientIP);
    } catch (Exception e) {
      logger.info("channelActive.no_ip:" + ctx);
    }
    IChannel client = new NettyChannel(ctx.channel());
    mq.publishEvent(NetEvent.connect, client);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    IChannel channel = ctx.channel().attr(NettyChannel.CHANNEL).get();
    var channelId = Optional.ofNullable(channel).map(IChannel::getId).orElse("");
    logger.info("channelInactive:" + ctx + " channelId:" + channelId);
    if (channel != null) {
      mq.publishEvent(NetEvent.close, channel);
    } else {

    }
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent event) {  // 2
//      if (timeoutclose) {
//        if (event.state() == IdleState.READER_IDLE || event.state() == IdleState.WRITER_IDLE
//            || event.state() == IdleState.ALL_IDLE) {
//          ctx.close();
//        }
//      }
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    try {
      logger.info("exceptionCaught:" + ctx);
      logger.error(cause.getMessage());
      ctx.channel().close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public MessageQueue getMq() {
    return mq;
  }

  public void setMq(MessageQueue mq) {
    this.mq = mq;
  }
}
