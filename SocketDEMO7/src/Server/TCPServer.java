package Server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import Client.Client;
public class TCPServer {
	private final int port;//服务器TCP端口
	private ClientListener mListener;
	private static List<ClientHandler> clientHandlerList=new ArrayList<>();
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
		for(ClientHandler clientHandler:clientHandlerList) {
			clientHandler.exit();
		}
		clientHandlerList.clear();//清楚客户端处理List
	}
	//客户端搜索，监听客户端连接
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
					//等待超时
					continue;
				}
				//
				ClientHandler clientHandler = null;
				try {
					clientHandler = new ClientHandler(client,new ClientHandler.CloseNotify() {
						@Override
						public void SelfClosed(ClientHandler Handler) {
							clientHandlerList.remove(Handler);//列表中移除客户端
						}
					});
					clientHandlerList.add(clientHandler);
					//启动读取
				    clientHandler.readToPrint();
				} catch (IOException e) {
					System.out.println("客户端连接异常!"+e.getMessage());
					e.printStackTrace();
				} 				
				
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
	
	//发送给客户端
	public void boradcast(String str) {		
		for(ClientHandler clientHandler:clientHandlerList) {
			clientHandler.send(str);
		}
	}	
}
