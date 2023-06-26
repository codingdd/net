package com.codingdd.net;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可以自定义名字的线程工厂
 * 
 * @author codingdd
 * 
 */
public class NameThreadFactory implements ThreadFactory {

	private final ThreadGroup group;
	private final String namePrefix;
	final AtomicInteger threadNumber = new AtomicInteger(1);

	public NameThreadFactory(String namePrefix) {
//		SecurityManager s = System.getSecurityManager();
		group =  Thread.currentThread().getThreadGroup();
		this.namePrefix = namePrefix;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
		if (t.isDaemon())
			t.setDaemon(false);
		if (t.getPriority() != Thread.NORM_PRIORITY)
			t.setPriority(Thread.NORM_PRIORITY);
		return t;
	}

}
