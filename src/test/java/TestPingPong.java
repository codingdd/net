import com.codingdd.net.event.NetEvent;
import com.codingdd.net.messagequeue.IMessageHandler;
import com.codingdd.net.IChannel;
import com.codingdd.net.INetService;
import com.codingdd.net.NetService;
import com.codingdd.net.channel.impl.Channel;
import com.codingdd.net.util.NetUtil;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

public class TestPingPong {

	private String ip = "127.0.0.1";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExecutorService e = Executors.newCachedThreadPool();
		INetService service = new NetService();
//		service.startMutiServer(new InetSocketAddress("127.0.0.1", 12346), new ServerIOHandler(), IServer.SERVER_BACK,(byte)1);
//		IConnector connector = service.startConnector(new ConnectIOHandler());
//		for (int i = 0; i < 4; i++) {
//			final IChannel client = connector.connect2server(new InetSocketAddress("188.188.1.183", 12345));
//			final long l = 1l + (i + 1) * 100000;
//			e.execute(new Runnable() {
//
//				@Override
//				public void run() {
//					long num = l;
//					while (true) {
//						for(int j = 0; j < 300; j++) {
//							IoBuffer buffer = IoBuffer.allocate(8);
//							buffer.putLong(0, num++);
//							client.send(buffer);
//						}
//						try {
//							Thread.sleep(1l);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			});
//		}
	}

	static class ConnectIOHandler implements IMessageHandler {

		@Override
		public void onEvent(NetEvent event, long sequence, boolean endOfBatch) throws Exception {
			System.out.println("connector 收到消息 ：" + event);
		}

        @Override
        public void startStatistics() {
            
        }

	}

	static class ServerIOHandler implements IMessageHandler {
		public static IChannel c;
		long ctime = 0l;
		AtomicLong num = new AtomicLong(0);

		@Override
		public void onEvent(NetEvent event, long sequence, boolean endOfBatch) throws Exception {
			if(sequence % 1000 == 0){
				System.out.println(new Date() + "读到位置:" + sequence + "最后一个：" + endOfBatch);
			}
			switch (event.getEventType()) {
			case NetEvent.connect:
				IoSession s = NetUtil.getParamater(event);
				c = new Channel(s);
				break;
			case NetEvent.message:
				if (num.get() == 0) {
					ctime = System.currentTimeMillis();
				}
				num.addAndGet(1);
				IoBuffer b = NetUtil.getParamater(event);
				long value = b.getLong();
//				System.out.println("server receive is : " + value);
				if (num.get() == 1000000) {
					System.out.println("time is :" + (System.currentTimeMillis() - ctime));
					System.out.println("num is : " + num.get());
					System.out.println("TPS is : " + num.get() * 1000 / (System.currentTimeMillis() - ctime));
				}
				// IoBuffer buffer = IoBuffer.allocate(8);
				// buffer.putLong(value);
				// buffer.flip();
				// c.send(buffer);
			}
		}

        @Override
        public void startStatistics() {
            // TODO Auto-generated method stub
            
        }

	}
}
