package com.codingdd.net.util;

import com.codingdd.net.event.NetEvent;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetUtil {

  private static final Logger logger = LoggerFactory.getLogger(NetUtil.class);

  @SuppressWarnings("unchecked")
  public static <T> T getParamater(NetEvent event) {
    return (T) event.getParamater();
  }

  @SuppressWarnings("unchecked")
  public static <T> T getParameter2(NetEvent event) {
    return (T) event.getParamater2();
  }

  @SuppressWarnings("unchecked")
  public static <T> T getParamater3(NetEvent event) {
    return (T) event.getParamater3();
  }

  public static boolean isUsedPort(int port) {
    try {
      tryBindPort("0.0.0.0", port);
      return false;
    } catch (Exception e) {
      logger.error("端口-" + port + "已经被占用");
    }
    return true;
  }

  private static void tryBindPort(String host, int port) throws Exception {
    Socket s = new Socket();
    s.setSoLinger(false, 0);
    s.bind(new InetSocketAddress(host, port));
    s.close();
  }

}
