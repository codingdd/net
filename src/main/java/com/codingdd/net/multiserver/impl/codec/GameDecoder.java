package com.codingdd.net.multiserver.impl.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 游戏解码器
 * 
 * @author codingdd
 *
 */
public class GameDecoder extends CumulativeProtocolDecoder {
	private static Logger logger = LoggerFactory.getLogger(GameDecoder.class);
	private static final int PACKAGE_MAX_LENGTH = 5120;
	static StringBuilder policeFile = new StringBuilder();

	protected GameDecoder() {
	}

	protected boolean doDecode(IoSession session, IoBuffer iobuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
//		iobuffer.mark();
//		byte cmd = iobuffer.get();
//		if (cmd == 60) {
//			iobuffer.flip();
//			IoBuffer wb = IoBuffer.allocate(this.xmlcontent.length + 1);
//			wb.put(this.xmlcontent);
//			wb.put((byte) 0);
//			wb.flip();
//			session.write(wb);
//			return false;
//		}
//		iobuffer.reset();
//		try {
//			if (iobuffer.remaining() < 5) {
//				return false;
//			}
//			iobuffer.mark();
//
//			short length = iobuffer.getShort();
//			if ((length < 0) || (length > PACKAGE_MAX_LENGTH)) {
//				session.close(true);
//				return false;
//			}
//			if ((length >= 5) && (length - 2 <= iobuffer.remaining())) {
//				byte beginTag = iobuffer.get();
//
//				if (beginTag != 127) {
//					session.close(true);
//					return false;
//				}
//
//				short code = iobuffer.getShort();
//				int oldLimit2 = iobuffer.limit();
//				iobuffer.limit(iobuffer.position() + length - 5);
//				int bodyLen = iobuffer.remaining();
//				byte[] body = new byte[bodyLen];
//
//				iobuffer.get(body);
//				iobuffer.limit(oldLimit2);
////				try {
////					IMessage message = this.messageFactory.createMessage(code, body);
////					if (message != null)
////						protocolDecoderOutput.write(message);
////					else
////						logger.error("GameDecoder.doDecode() createMessage is null,code is {}", Short.valueOf(code));
////				} catch (Exception e) {
////					e.printStackTrace();
////					logger.error("GameDecoder.doDecode()", e);
////					session.close(true);
////					return false;
////				}
//				return true;
//			}
//			iobuffer.reset();
//			return false;
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error("GameDecoder.doDecode()", e);
//			session.close(true);
//		}
//		return false;
		try {
			if(iobuffer.remaining() >= 3){
				// 
				iobuffer.mark();
				iobuffer.get();
				short len = iobuffer.getShort();
				if(len <= 0 || len >= PACKAGE_MAX_LENGTH){
					session.close(true);
					logger.error("消息长度不合法，长度为：{},断开:{}",len,session.getId());
					return false;
				}
				if(len - 3 > iobuffer.remaining()){
//				    short code = iobuffer.getShort();
					iobuffer.reset();
//					logger.info("消息被分包--------------=======================buff总大小："+iobuffer.capacity()+"消息长度："+len+"消息号：" +code);
					return false;
				}
				iobuffer.reset();
				//
				int currLimit = iobuffer.limit();
				iobuffer.limit(iobuffer.position() + len);
				IoBuffer buffer = IoBuffer.allocate(len).setAutoExpand(true);
				buffer.put(iobuffer);
				buffer.flip();
				protocolDecoderOutput.write(buffer);
				//
				iobuffer.limit(currLimit);
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("gamedecoder error -> ",e);
		}
		return false;
	}
	
}