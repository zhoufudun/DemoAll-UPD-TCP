import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	  
	  private static final int LOCAL_PORT=22222;//远端端口
	  public static void main(String[] agr)throws Exception{
	        ServerSocket serverSocket=createSocket();
	        initServerSocket(serverSocket);	   
			serverSocket.bind(new InetSocketAddress(Inet4Address.getLocalHost(),LOCAL_PORT),50);//绑定本地地址,允许等待50个连接队列，并不是50个客户端
	        System.out.println("服务器准备就绪~");
	        System.out.println("服务器信息"+serverSocket.getInetAddress()+" port:"+serverSocket.getLocalPort());
	        //无限循环
	        for(;;){
	            //得到客户端
	        	Socket client =serverSocket.accept();//服务器等待客户端连接
	            //客户端构建异步线程
	            ClientHandler handler=new ClientHandler(client);
	            //启动线程
	            handler.start();
	        }
	  }
	  private static class ClientHandler extends Thread{
	      private Socket client;
	      private boolean flag=true;
	
	      ClientHandler(Socket client){
	          this.client=client;
	      }
	      @Override
	      public void run() {
	          super.run();
	          System.out.println("新客户端信息信息："+client.getInetAddress()+" port:"+client.getPort());
	          try {
	
	              //得到打印流，用于数据输出，服务器回送数据使用
	              PrintStream serverOutput=new PrintStream(client.getOutputStream());
	              //得到输入流，用于数据接收
	              BufferedReader serverInput=new BufferedReader(new InputStreamReader(client.getInputStream()));
	
	              do{
	                  //拿到客户端的一条数据
	                  String str=serverInput.readLine();//等待数据，阻塞
	                  if("bye".equalsIgnoreCase(str)){
	                      flag=false;
	                      //回送给客户端
	                      serverOutput.println("bye");
	                  }else{
	                      System.out.println(str);
	                      serverOutput.println("消息回显，"+str);
	                  }
	
	              }while(flag);
	              //释放资源
	              serverOutput.close();
	              serverInput.close();
	
	          }catch (Exception e){
	              System.out.println("连接异常断开");
	              e.printStackTrace();
	          }
	
	      }
	  }
	  public static ServerSocket createSocket() throws IOException {   	
		  ServerSocket serverSocket=new ServerSocket();	  	
	      return serverSocket;	
	  }
	  public static void initServerSocket(ServerSocket serverSocket)throws Exception{
	  	//设置serverSocket的accept超时时间为3秒
		//serverSocket.setSoTimeout(3000);//设置有阻塞的地方，3秒延时时间，超过3秒抛出异常
	  	//是否复用完全关闭的socket地址，对于指定bind操作后的套接字有效
		serverSocket.setReuseAddress(true);
	  	//是否开启Nagle算法,()???
		//serverSocket.setTcpNoDelay(false);
	  	//是否需要在长时间时无数据响应时发送确认数据（类似心跳包），时间为大约2小时
		//serverSocket.setKeepAlive(true);
	  	//对于close关闭操作行为进行怎样的处理，默认为false,0
	  	//false,0:默认情况，关闭时立即返回,底层系统接管输出流，将缓冲区内的数据发送完成
	  	//true,0:关闭时立即返回，缓冲区数据抛弃，直接发送RST结束命令到对方，无需经过2MSL等待
	  	//true,200:关闭时最长阻塞200ms，随后按第二种情况处理。
		//serverSocket.setSoLinger(true, 20);
	  	//是否让紧急数据内敛，默认false:紧急数据通过socket.sendUrgentData(1)；发送
		//serverSocket.setOOBInline(true);
	  	//设置接收和发送缓冲区大小，设置得到的socket的大小
		serverSocket.setReceiveBufferSize(64*1024*1024);	
	  	
	  	//设置性能参数：短链接，延迟，带宽的相对重要性
		serverSocket.setPerformancePreferences(1, 1, 1 );
	  }

}
