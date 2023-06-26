package com.codingdd.net.Statistics;

import com.codingdd.net.messagequeue.MessageQueue;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.mina.core.service.IoServiceStatistics;

public class StatisticsV2 {
    private IoServiceStatistics netStatistics;
    
    private MessageQueue mq;
    
    public StatisticsV2(MessageQueue mq){
        this.mq = mq;
    }
    
    /**
     * 最小队列剩余的空格
     */
    private long remaingSlot;

    /**
     * 处理的消息总数
     */
    private AtomicLong allmessages = new AtomicLong();

    /**
     * 一段时间内处理的消息总数
     */
    private AtomicLong messages = new AtomicLong();

    private long lastUpdateTime;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void increaseAllMsgs() {
        allmessages.incrementAndGet();
    }

    public void increaseMsgs() {
        messages.incrementAndGet();
    }

    public void setRemaingSlot(long num) {
        if (remaingSlot == 0 || num < remaingSlot) {
            remaingSlot = num;
        }
    }

    public long getRemaingSlot() {
        return remaingSlot;
    }

    public long getMessages() {
        return allmessages.get();
    }

    private long messagesTPS;

    public long getMessagesTPS() {
        return messagesTPS;
    }

    public void setNetStatistics(IoServiceStatistics netStatistics) {
        this.netStatistics = netStatistics;
    }

    public double getReadBytesThroughput(){
        return netStatistics.getReadBytesThroughput();
    }
    
    public double getWrittenBytesThroughput(){
        return netStatistics.getWrittenBytesThroughput();
    }
    
    public double getReadBytes(){
        return netStatistics.getReadBytes();
    }
    
    public double getWriteBytes(){
        return netStatistics.getWrittenBytes();
    }
    
    public void update(long time) {
        if (netStatistics != null) {
            netStatistics.updateThroughput(time);
        }
        int interval = (int) (time - this.lastUpdateTime);
        messagesTPS = messages.get() / (interval * 1000l);
        messages.set(0);
        
        this.setRemaingSlot(mq.getRb().remainingCapacity());
        
        this.lastUpdateTime = time;
    }

}
