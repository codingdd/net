package com.codingdd.net.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import java.util.List;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * @author : codingdd
 * @date : 2017/12/28
 */
public class WebSocketEncoder extends MessageToMessageEncoder<IoBuffer> {

  @Override
  protected void encode(ChannelHandlerContext channelHandlerContext, IoBuffer buffer,
      List<Object> list) throws Exception {

    BinaryWebSocketFrame frame = new BinaryWebSocketFrame();
    frame.content().writeBytes(buffer.array(), 0, buffer.remaining());
    list.add(frame);
  }
}
