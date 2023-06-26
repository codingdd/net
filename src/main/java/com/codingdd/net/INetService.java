package com.codingdd.net;

import com.codingdd.net.messagequeue.IMessageHandler;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * 网络服务层的接口
 *
 * @author codingdd
 */
public interface INetService {

    String config_timeout = "timeout";

    String config_timeout_close = "timeoutclose";

    String config_head_id = "headid";
//    public static String config_check_ip_list = "check_ip_list";

    String config_ip_list = "ip_list";

    //后端服务器不需要限制连接
    String is_limit_access = "is_limit_access";

    String config_black_ip = "black_ip";

    /**
     * 启动一个处理大量连接的socket服务,指定监听地址
     *
     * @param address
     * @param handler 发送到该服务的消息的处理器
     *
     * @return
     */
    IServer startMutiServer(InetSocketAddress address, IMessageHandler handler,
        Map<String, Object> paras)
        throws Exception;

    IServer startMutiServer(Map<String, Object> paras)
        throws Exception;

    /**
     * 启动一个连接器，负责连接服务端
     *
     * @param handler
     *
     * @return
     */
    IConnector startConnector(IMessageHandler handler, short headid);

    /**
     * 使用指定连接器连接指定地址
     *
     * @param address
     *
     * @return
     */
    IChannel connect2Server(IConnector connector, InetSocketAddress address);

    /**
     * @param client
     * @param buffer
     */
    void send(IChannel client, IoBuffer buffer);

    void broadcast(Collection<IChannel> list, IoBuffer buffer);

    void stopMutiServer();

    /**
     * 增加参数
     *
     * @param key
     * @param val
     */
    void addPara(String key, Object val);
}
