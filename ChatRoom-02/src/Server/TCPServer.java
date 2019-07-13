package Server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class TCPServer implements ClientHandler.ClientHandlerCallBack {
	private final int port;//服务器TCP端口
	private ClientListener mListener;
	private final ExecutorService forwardingThreadPoolExecutor;//为什么？？？？？不明白
	private static List<ClientHandler> clientHandlerList=new ArrayList<>();
	public TCPServer(int port) {
		super();
		this.forwardingThreadPoolExecutor=Executors.newSingleThreadExecutor();
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
		//保证顺序执行
		synchronized (TCPServer.this) {
			for(ClientHandler clientHandler:clientHandlerList) {
				clientHandler.exit();
			}
			clientHandlerList.clear();//清楚客户端处理List
		}
		//关闭线程池
		forwardingThreadPoolExecutor.shutdownNow();
		
	}
	//客户端搜索，监听客户端连接
	public class ClientListener extends Thread{
		private ServerSocket server;
		private boolean done=false;			
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
				try {
					ClientHandler clientHandler = new ClientHandler(client,TCPServer.this);
					synchronized (TCPServer.this) {
						clientHandlerList.add(clientHandler);
					}
					//收到信息后转发给其他客户端
				    clientHandler.readToPrint();
				} catch (IOException e) {
					System.out.println("客户端连接异常!"+e.getMessage());
					e.printStackTrace();
				} 				
				
			}while(!done);
			System.out.println("服务器已关闭!");
		}
		public void exit() {
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
		synchronized (TCPServer.this) {
			for(ClientHandler clientHandler:clientHandlerList) {
				clientHandler.send(str);
			}
		}
	}
	//ClientHandler 自身关闭自己处理
	@Override
	public synchronized void SelfClosed(ClientHandler Handler) {
		clientHandlerList.remove(Handler);
	}
	//ClientHandler 收到消息，消息处理
	@Override
	public void onNewMessageArrived(ClientHandler handler, String msg) {
		//异步提交转发任务  
		forwardingThreadPoolExecutor.execute(new Runnable() {			
			@Override
			public void run() {			
				synchronized (TCPServer.this) {
					for(ClientHandler clientHandler:clientHandlerList) {
					    //跳过自身的ClientHandler
						if(!handler.equals(clientHandler)) {
							clientHandler.send(msg);
						}
					}	
				}
					
			}
		});
		//打印到屏幕上
		System.out.println("收到消息:"+"["+msg+"]"+"{"+handler.getClientInfo()+"}");
	}	
}
