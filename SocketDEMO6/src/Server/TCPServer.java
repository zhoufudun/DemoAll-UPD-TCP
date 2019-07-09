package Server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import Client.Client;
public class TCPServer {
	private final int port;//服务器TCP端口
	private ClientListener mListener;
	public TCPServer(int port) {
		super();
		this.port = port;
	}
	public boolean start() {
		try {
			ClientListener clientListener=new ClientListener(port);
			mListener=clientListener;
			mListener.start();
		} catch (Exception e) {
			return false;//服务器启动失败
		}
		return true;
	}
	public void stop(){
		if(mListener!=null) {
			mListener.exit();
		}
	}
	public static class ClientListener extends Thread{
		private static ServerSocket server;
		private static boolean done=false;			
		public ClientListener(int port) throws IOException {
			server=new ServerSocket(port);
			System.out.println("服务器信息：IP="+server.getInetAddress().getHostAddress()+",port="+server.getLocalPort());
		}
		@Override
		public void run() {			
			super.run();
			System.out.println("服务器准备就绪...");
			do {
				Socket client;
				try {
					client=server.accept();//线程阻塞在这等待客户端连接
				} catch (Exception e) {
					//关闭时这里报错为socket closed，忽略
					continue;
				}
				//客户端构建异步线程
				ClientHandler clientHandler=new ClientHandler(client); 				
				//启动线程
				clientHandler.start();
			}while(!done);
			System.out.println("服务器已关闭!");
		}
		public static void exit() {
			if(done!=true) {
				done=true;
				try {
					server.close();
				} catch (IOException e) {
					System.out.println("服务器已关闭异常!");
					e.printStackTrace();
				}
			}
		}
	}
	public static class ClientHandler extends Thread{
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
	                  }else if(!" ".equalsIgnoreCase(str)){
	                	  System.out.println(str);
	                      serverOutput.println("消息回显，"+str+"\n");
	                  }else { //空值不打印
	                	  continue;
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
}
