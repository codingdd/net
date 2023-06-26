package com.codingdd.net.messagequeue;

import com.codingdd.net.event.NetEvent;
import com.codingdd.net.NameThreadFactory;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageQueue {

  private final ExecutorService executor = Executors
      .newFixedThreadPool(5, new NameThreadFactory("disruptor-"));
  private final Disruptor<NetEvent> disruptor;
  private RingBuffer<NetEvent> rb;

  public MessageQueue(IMessageHandler messageHandler) {
    int buff_size = 2048 * 2 * 2;
    disruptor = new Disruptor<>(new IOEventFactory(), buff_size, DaemonThreadFactory.INSTANCE);
    disruptor.handleEventsWith(messageHandler);
  }

  public void start() {
    rb = disruptor.start();
  }

  public void shutDown() {
    //disruptor.shutdown();
    //executor.shutdown();
  }

  public RingBuffer<NetEvent> getRb() {
    return rb;
  }

  public void publishEvent(byte eventType, Object... obj) {
    long next = rb.next();
    try {
      NetEvent nextObj = rb.get(next);
      if (obj.length == 1) {
        nextObj.setParamater(obj[0]);
      } else if (obj.length == 2) {
        nextObj.setParamater(obj[0]);
        nextObj.setParamater2(obj[1]);
      } else if (obj.length == 3) {
      nextObj.setParamater(obj[0]);
      nextObj.setParamater2(obj[1]);
      nextObj.setParamater3(obj[2]);
    }
      nextObj.setEventType(eventType);
    } finally {
      rb.publish(next);
    }
  }

  static class IOEventFactory implements EventFactory<NetEvent> {

    @Override
    public NetEvent newInstance() {
      return new NetEvent();
    }
  }

}
