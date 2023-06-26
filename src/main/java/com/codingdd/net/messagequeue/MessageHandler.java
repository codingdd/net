package com.codingdd.net.messagequeue;

import com.codingdd.net.event.NetEvent;


public class MessageHandler implements IMessageHandler{

	@Override
	public void onEvent(NetEvent event, long sequence, boolean endOfBatch) throws Exception {
	}

    @Override
    public void startStatistics() {
        
    }


}
