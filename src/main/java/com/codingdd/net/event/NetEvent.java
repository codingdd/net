package com.codingdd.net.event;

/**
 * 网络事件封装类
 * 
 * <pre>
 * 包括事件类型和事件参数 取得事件参数 <BR>
 * 1.message -> IoBuffer <BR>
 * 2.connect -> IChannel <BR>
 * 3.close -> client id<Br>
 * 
 * @author codingdd
 * 
 */
public class NetEvent {
	/**
	 * 消息
	 */
	public static final byte message = 1;
	/**
	 * 连接事件
	 */
	public static final byte connect = 2;
	/**
	 * 关闭事件
	 */
	public static final byte close = 3;

	/**
	 * 事件类型
	 */
	private byte eventType;
	/**
	 * 事件参数
	 */
	private Object paramater;

	private Object paramater2;

	private Object paramater3;

	/**
	 * 取得事件参数 <BR>
	 * 1.message -> IoBuffer <BR>
	 * 2.connect -> IClient <BR>
	 * 3.close -> client id<Br>
	 * 
	 * @return
	 */
	public Object getParamater() {
		return paramater;
	}

	public void setParamater(Object paramater) {
		this.paramater = paramater;
	}

	public byte getEventType() {
		return eventType;
	}

	public void setEventType(byte eventType) {
		this.eventType = eventType;
	}

	public Object getParamater2() {
		return paramater2;
	}

	public void setParamater2(Object paramater2) {
		this.paramater2 = paramater2;
	}

	public Object getParamater3() {
		return paramater3;
	}

	public void setParamater3(Object paramater3) {
		this.paramater3 = paramater3;
	}

}
