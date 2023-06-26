package com.codingdd.net.multiserver.impl.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class GameEncoder extends ProtocolEncoderAdapter {
	protected GameEncoder(){
		
	}
	public void encode(IoSession arg0, Object arg1, ProtocolEncoderOutput arg2)
			throws Exception {
		if ((arg1 instanceof IoBuffer))
			arg2.write(IoBuffer.class.cast(arg1));
	}
}