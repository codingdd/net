package com.codingdd.net.multiserver.nettyimpl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * @author : codingdd
 * @date : 2017/12/28
 */
public class EncoderNetty extends MessageToByteEncoder<IoBuffer>{
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, IoBuffer o, ByteBuf byteBuf) throws Exception {
            byte[] array = new byte[o.remaining()];
            o.get(array);
            byteBuf.writeBytes(array);
    }
}
