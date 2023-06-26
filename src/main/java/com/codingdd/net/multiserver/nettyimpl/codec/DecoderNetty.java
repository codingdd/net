package com.codingdd.net.multiserver.nettyimpl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : codingdd
 * @date : 2017/12/28
 */
public class DecoderNetty extends ByteToMessageDecoder {

  private static final int PACKAGE_MAX_LENGTH = Short.MAX_VALUE;
  private static final Logger logger = LoggerFactory.getLogger(DecoderNetty.class);

  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf,
      List<Object> list) throws Exception {

    try {
      if (byteBuf.readableBytes() >= 3) {
        //
        byteBuf.markReaderIndex();
        byteBuf.readByte();
        short len = byteBuf.readShort();
        if (len <= 0 || len >= PACKAGE_MAX_LENGTH) {
          byteBuf.clear();
          channelHandlerContext.close();
          logger.error("消息长度不合法，长度为：{},断开:", len);
          return;
        }
        if (len - 3 > byteBuf.readableBytes()) {
          byteBuf.resetReaderIndex();
          return;
        }

        byteBuf.resetReaderIndex();
        IoBuffer buffer = IoBuffer.allocate(len).setAutoExpand(true);
        byteBuf.readBytes(buffer.array());
        buffer.position(len);
        buffer.flip();
        list.add(buffer);

      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("gameDecoder error -> ", e);
    }
  }
}
