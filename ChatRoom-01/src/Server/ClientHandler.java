package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Utils.CloseUtils;
//这里为什么不继承Thread？？？？？？？？？？
//这里是收发并行线程，收线程和发送线程分开写
public class ClientHandler {
	  private final Socket client;    
      private final ClientReadHandler clientReadHandler;
      private final ClientWriteHandler clientWriterHandler;
      private final ClientHandlerCallBack clientHandlerCallBack;
      private final String clientInfo;//记录该客户端的信息
      ClientHandler(Socket client,ClientHandlerCallBack clientHandlerCallBack) throws IOException{
          this.client=client;
          this.clientReadHandler=new ClientReadHandler(client.getInputStream());
          this.clientWriterHandler=new ClientWriteHandler(client.getOutputStream());
          this.clientHandlerCallBack=clientHandlerCallBack;
          this.clientInfo="IP["+client.getInetAddress().getHostAddress()+"]" + "port["+client.getPort()+"]";
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
          CloseUtils.CloseAll(client); 
          //
    	  //System.out.println("服务器关闭，断开与客户端连接，客户端信息为"+client.getInetAddress()+" port:"+client.getPort());	
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
    	  private final InputStream inputStream;
    	  ClientReadHandler(InputStream inputStream){
    		  this.inputStream=inputStream;
    	  }
		@Override
		public void run() {
			super.run();
		     try {	          
	              //得到输入流，用于数据接收
	              BufferedReader serverInput=new BufferedReader(new InputStreamReader(inputStream));
	              do{
	                  //拿到客户端的一条数据
	                  String str=serverInput.readLine();//等待数据，阻塞
	                  if(str==null) {
	                	  //读取超时或者异常
	                	  System.out.println("无法读取客户端数据！");
	                	  //退出客户端,自己退出自己
	                	  ClientHandler.this.exitByeSelf();                	  
	                	  break;
	                  }else if(!"00bye00".equalsIgnoreCase(str)) {
	                	  //客户端同时发送多条数据到服务器时，若这里诸塞，可能丢失数据，因此这里不能阻塞，这里需要及时执行，就是异步
	                	  //回调给外面，在外面实现，通知到TCPServer
	                	  clientHandlerCallBack.onNewMessageArrived(ClientHandler.this, str);
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
	        	  CloseUtils.CloseAll(inputStream);
	          }
		}  
		void exit() {
			done=true;
			CloseUtils.CloseAll(inputStream);
		}
      }
      //发送数据
      class ClientWriteHandler{
    	  private boolean done=false;
    	  private final PrintStream printStream;
    	  private ExecutorService executorService;
    	  ClientWriteHandler(OutputStream outputStream){
    		  this.printStream=new PrintStream(outputStream);
    		  executorService=Executors.newSingleThreadExecutor();//单例线程池
    	  }
    	  void exit() {
    		  done=true;
    		  executorService.shutdownNow();//线程池立即关闭
    		  CloseUtils.CloseAll(printStream);
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
					  //发送给客户端
					  ClientWriteHandler.this.printStream.println(mes);			
 
				  } catch (Exception e) {
					  e.printStackTrace();
				  }
			 }			    		  
    	  }	
    	   
      }
  
}
