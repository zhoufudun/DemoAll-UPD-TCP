package Server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import Utils.CloseUtils;
//这里为什么不继承Thread？？？？？？？？？？
//这里是收发并行线程，收线程和发送线程分开写
public class ClientHandler {
	  private final SocketChannel socketChannel;    
      private final ClientReadHandler clientReadHandler;
      private final ClientWriteHandler clientWriterHandler;
      private final ClientHandlerCallBack clientHandlerCallBack;
      private final String clientInfo;//记录该客户端的信息
      ClientHandler(SocketChannel socketChannel,ClientHandlerCallBack clientHandlerCallBack) throws IOException{
          this.socketChannel=socketChannel;
          //设置非阻塞模式
          socketChannel.configureBlocking(false);
          //
          Selector readSelector=Selector.open();
          //注册读取事件
          socketChannel.register(readSelector,SelectionKey.OP_READ);
          this.clientReadHandler=new ClientReadHandler(readSelector);
          //
          Selector writeSelector=Selector.open();
          //注册读取事件
          socketChannel.register(writeSelector,SelectionKey.OP_WRITE);
          this.clientWriterHandler=new ClientWriteHandler(writeSelector);
          //
          this.clientHandlerCallBack=clientHandlerCallBack;
          this.clientInfo=socketChannel.getLocalAddress().toString();
          System.out.println("新客户端信息："+clientInfo);
	     
      }
      public String getClientInfo() {
    	  return clientInfo;
      }
      //外层调用退出
      public void exit() {
    	  //退出自己的clientReadHandler
    	  clientReadHandler.exit();  
    	  //退出自己的clientWriterHandler
    	  clientWriterHandler.exit();  
    	  //释放资源	    
          CloseUtils.CloseAll(socketChannel); 
          //
    	  System.out.println("客户端退出，客户端信息为"+clientInfo);	
      }
      //自己退出自己
      private void exitByeSelf() {
    	  exit();
    	  //通知外面自己把自己关闭了
    	  clientHandlerCallBack.SelfClosed(ClientHandler.this);
      }
      //发送信息
      public void send(String str) {
    	  clientWriterHandler.send(str);
      }
      //读取客户端信息并且打印
      public void readToPrint() {
    	  clientReadHandler.start();//启动线程
	  }
      //callback返回，告知外面我自己已经关闭，需要在外面将客户端处理List中取出我这和客户端信息
      public static interface ClientHandlerCallBack{
    	  //自身关闭通知
    	  void SelfClosed(ClientHandler Handler);
    	  //收到消息时通知，为了防止阻塞，要如何设计？？？？ 
    	  void onNewMessageArrived(ClientHandler handler,String msg);
      }
      //读取线程
      class ClientReadHandler extends Thread{
    	  private boolean done=false;
    	  private final Selector selector;
    	  private final ByteBuffer byteBuffer;
    	  ClientReadHandler(Selector selector){
    		  this.selector=selector;
    		  this.byteBuffer=ByteBuffer.allocate(256);
    	  }
		@Override
		public void run() {
			super.run();
		     try {	          
	              do{
	                  //拿到客户端的一条数据
	                  if(selector.select()==0) {
	                	  //退出
	                	  if(done) {
	                		  break;
	                	  }
	                	  //没退出
	                	  continue;
	                  }
	                  //有就绪的事件时
					  Iterator<SelectionKey> iterator=selector.selectedKeys().iterator();
					  while(iterator.hasNext()) {
							if(done) {
								break;
							}
							SelectionKey key=iterator.next();
							//移除处理过的
							iterator.remove();
							//检查当前key的状态是否时我们关注的
							//客户端达到状态
							if(key.isReadable()) {
								//拿到我们注册的事件
								SocketChannel socketChannel=(SocketChannel) key.channel();
								//清空之前的指针位置，指针回到0
								byteBuffer.clear();
								//非阻塞状态拿到数据
								int readLen=socketChannel.read(byteBuffer);
								//有数据
								if(readLen>0) {
									//丢弃换行符
									String str=new String(byteBuffer.array(),0,readLen-1);
									if(!"00bye00".equalsIgnoreCase(str)) {
										//外面调用发送方法
										//回调给外面，在外面实现，通知到TCPServer
										clientHandlerCallBack.onNewMessageArrived(ClientHandler.this, str);
									}else {
										System.out.println("客户端退出了");
										continue;
									}
								}else {
									//读取超时或者异常
									System.out.println("无法读取客户端数据！");
									//退出客户端,自己退出自己
									ClientHandler.this.exitByeSelf();                	  
									break;
								}
					 		}
						}
	              }while(!done);	                      	             
	          }catch (Exception e){
	        	  if(!done) {
	        		   System.out.println("连接异常断开");
	                   e.printStackTrace();
	        		   ClientHandler.this.exitByeSelf();
	        	  }	        
	          }finally {
	        	  //释放资源	    
	        	  CloseUtils.CloseAll(selector);
	          }
		}  
		void exit() {
			done=true;
			//唤醒当前的阻塞
			selector.wakeup();
			CloseUtils.CloseAll(selector);
		}
      }
      //发送数据
      class ClientWriteHandler{
    	  private boolean done=false;
    	  private final Selector selector;
    	  private final ByteBuffer byteBuffer;
    	  private ExecutorService executorService;
    	  ClientWriteHandler(Selector selector){
    		  this.selector=selector;
    		  //分配256个字节
    		  this.byteBuffer=ByteBuffer.allocate(256);
    		  executorService=Executors.newSingleThreadExecutor();//单例线程池
    	  }
    	  void exit() {
    		  done=true;
    		  executorService.shutdownNow();//线程池立即关闭
    		  CloseUtils.CloseAll(selector);
    	  } 
    	  public void send(String str) {
    		  //立即执行线程
    		  if(done) {
    			  return ;
    		  }
			  executorService.execute(new WriteRunnable(str));
		  }
    	  class WriteRunnable implements Runnable{
    		  private final String mes;  		
			  public WriteRunnable(String str) {
				  this.mes=str;
			  }
			  @Override
			  public void run() {
				  //已经退出
				  if(ClientWriteHandler.this.done) {
					  return;
				  }
				  try {	
					  //清空之前的指针位置
					  byteBuffer.clear();
					  byteBuffer.put(mes.getBytes());
					  //反转操作，这里是重点，通过这个操作可以发送有效的数据，否者发送byteBuffer的所有内容，有一部分为空
					  byteBuffer.flip();
					  //未结束而且byteBuffer有内容没有发送
					  while(!done&&byteBuffer.hasRemaining()) {
						try {
							//写
							int len=socketChannel.write(byteBuffer);
							//len=0合法
							if(len<0) {
								System.out.println("客户端已无法发送数据！");
								ClientHandler.this.exitByeSelf();
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					  }
 
				  } catch (Exception e) {
					  e.printStackTrace();
				  }
			 }			    		  
    	  }	
      }
}
