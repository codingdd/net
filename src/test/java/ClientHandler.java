import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
 
 
//客户端业务处理类
public class ClientHandler   extends SimpleChannelInboundHandler<Object> {
 
   private  WebSocketClientHandshaker handshaker;
    ChannelPromise handshakeFuture;
 
    /**
     * 当客户端主动链接服务端的链接后，调用此方法
     *
     * @param channelHandlerContext ChannelHandlerContext
     */
    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) {
        System.out.println("客户端Active .....");
        handlerAdded(channelHandlerContext);
    }
 
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("\n\t⌜⎓⎓⎓⎓⎓⎓exception⎓⎓⎓⎓⎓⎓⎓⎓⎓\n" +
              cause.getMessage());
        ctx.close();
    }
 
    public void setHandshaker(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.handshakeFuture = ctx.newPromise();
    }
    public ChannelFuture handshakeFuture() {
        return this.handshakeFuture;
    }
 
    protected void channelRead0(ChannelHandlerContext ctx, Object o) throws Exception {
        System.out.println("22222");
 
        // 握手协议返回，设置结束握手
        if (!this.handshaker.isHandshakeComplete()){
            FullHttpResponse response = (FullHttpResponse)o;
            this.handshaker.finishHandshake(ctx.channel(), response);
            this.handshakeFuture.setSuccess();
            System.out.println("WebSocketClientHandler::channelRead0 HandshakeComplete...");
            return;
        }
       else  if (o instanceof TextWebSocketFrame)
        {
                TextWebSocketFrame textFrame = (TextWebSocketFrame)o;
                System.out.println("WebSocketClientHandler::channelRead0 textFrame: " + textFrame.text());
        } else   if (o instanceof CloseWebSocketFrame){
            System.out.println("WebSocketClientHandler::channelRead0 CloseWebSocketFrame");
        } else if (o instanceof BinaryWebSocketFrame) {
          System.out.println((((BinaryWebSocketFrame) o).content().readableBytes()));
        }
 
    }
}