package com.codingdd.net;//package com;
//
//import java.util.concurrent.atomic.AtomicLong;
//
//import org.apache.mina.core.service.IoServiceStatistics;
//
//public class Statistics {
//    private static long serverLastUpdateTime;
//
//    private static long serverUpdateTime;
//
//    private static long serverPutPosion;
//
//    private static long serverGetPosion;
//    
//    private static float serverMessageQueueThroughput;
//    
//    private static long remaingSlot;
//    private static long lastserverprocessnum;
//    private static AtomicLong serverProcessNum = new AtomicLong();
//
//    // FS到BS的吞吐量
//    private static IoServiceStatistics conectorStatistics;
//    
//    private static IoServiceStatistics acceptorStatistics;
//    
//    static {
//        new Thread() {
//            @Override
//            public void run() {
//                while (true) {
//                    Statistics.update(System.currentTimeMillis());
//                    try {
//                        Thread.sleep(5000l);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }.start();
//    }
//
//
//    public static void setFsToBS(IoServiceStatistics fsToBS) {
//        Statistics.conectorStatistics = fsToBS;
//    }
//
//    public static void setClientToFS(IoServiceStatistics clientToFS) {
//        Statistics.acceptorStatistics = clientToFS;
//    }
//
//    public static long getServerLastUpdateTime() {
//        return serverLastUpdateTime;
//    }
//
//
//    public static long getRemaingSlot() {
//        return remaingSlot;
//    }
//
//
//    public static void setRemaingSlot(long remaingSlot) {
//        Statistics.remaingSlot = remaingSlot;
//    }
//
//
//    public static void setServerLastUpdateTime(long serverLastUpdateTime) {
//        Statistics.serverLastUpdateTime = serverLastUpdateTime;
//    }
//
//
//    public static long getServerUpdateTime() {
//        return serverUpdateTime;
//    }
//
//
//    public static void setServerUpdateTime(long serverUpdateTime) {
//        Statistics.serverUpdateTime = serverUpdateTime;
//    }
//
//
//    public static long getServerPutPosion() {
//        return serverPutPosion;
//    }
//
//
//    public static void setServerPutPosion(long serverPutPosion) {
//        Statistics.serverPutPosion = serverPutPosion;
//    }
//
//
//    public static long getServerGetPosion() {
//        return serverGetPosion;
//    }
//
//
//    public static void setServerGetPosion(long serverGetPosion) {
//        Statistics.serverGetPosion = serverGetPosion;
//    }
//
//
//    public static float getServerMessageQueueThroughput() {
//        return serverMessageQueueThroughput;
//    }
//
//
//    public static void setServerMessageQueueThroughput(float serverMessageQueueThroughput) {
//        Statistics.serverMessageQueueThroughput = serverMessageQueueThroughput;
//    }
//
//
//    public static AtomicLong getServerProcessNum() {
//        return serverProcessNum;
//    }
//
//
//    public static void incrServerProcessNum() {
//        Statistics.serverProcessNum.incrementAndGet();
//    }
//
//
//    public static void update(long time) {
//        serverLastUpdateTime = serverUpdateTime;
//        serverUpdateTime = time;
//        if (serverLastUpdateTime != 0) {
//            serverMessageQueueThroughput = (serverProcessNum.get() - lastserverprocessnum) * 1f / ((serverUpdateTime - serverLastUpdateTime) / 1000);
//        }
//        lastserverprocessnum = serverProcessNum.get();
//        System.out.println("put:" + serverPutPosion +", get:" + serverGetPosion + ", remaingSlot:"+remaingSlot+", mqtp:" + serverMessageQueueThroughput);
//        // 网关吞吐量
//        if(Statistics.conectorStatistics != null){
//            conectorStatistics.updateThroughput(time);
//            double fsToBsReadTP = Statistics.conectorStatistics.getReadBytesThroughput();
//            double fsToBsWriteTP = Statistics.conectorStatistics.getWrittenBytesThroughput();
////            System.out.println("fs     readTP:" + (long)fsToBsReadTP +", fs     writeTP:" + (long)fsToBsWriteTP);
//        }
//        
//        if(Statistics.acceptorStatistics != null){
//            acceptorStatistics.updateThroughput(time);
//            double clientToFsReadTP = Statistics.acceptorStatistics.getReadBytesThroughput();
//            double clientToBsWriteTP = Statistics.acceptorStatistics.getWrittenBytesThroughput();
//            System.out.println("client readTP:" + (long)clientToFsReadTP +", client writeTP:" + (long)clientToBsWriteTP);
//        }
//        
//    }
//
//}
