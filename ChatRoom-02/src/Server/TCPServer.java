package Server;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Utils.CloseUtils;
public class TCPServer implements ClientHandler.ClientHandlerCallBack {
	private final int port;//服务器TCP端口
	private ClientListener mListener;
	private final ExecutorService forwardingThreadPoolExecutor;//为什么？？？？？不明白
	private static List<ClientHandler> clientHandlerList=new ArrayList<>();
	private static Selector selector;
	private static ServerSocketChannel serverSocketChannel;
	public TCPServer(int port) {
		super();
		this.forwardingThreadPoolExecutor=Executors.newSingleThreadExecutor();
		this.port = port;
	}
	public boolean start() {
		try {
			selector=Selector.open();
			serverSocketChannel=ServerSocketChannel.open();
			//设置为非阻塞
			serverSocketChannel.configureBlocking(false);
			//绑定本地端口
			serverSocketChannel.socket().bind(new InetSocketAddress(port));
			//注册客户端连接事件
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			//
			System.out.println("服务器信息:"+serverSocketChannel.getLocalAddress().toString());
			//
			ClientListener clientListener=new ClientListener();
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
		CloseUtils.CloseAll(selector);
		CloseUtils.CloseAll(serverSocketChannel);
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
		private boolean done=false;			
		public ClientListener() throws IOException {
		}
		@Override
		public void run() {			
			super.run();
			Selector selector=TCPServer.selector;
			System.out.println("服务器准备就绪...");
			//等待客户端连接
			do {
				//得到客户端
			    Socket client;
				try {
					//0表示没有就绪的事件（阻塞的事件）
					if(selector.select()==0) {
						//完成状态跳出
						if(done) {
							break;
						}
						//非完成继续下一次循环
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
						if(key.isAcceptable()) {
							//拿到我们注册的事件
							ServerSocketChannel serverSocketChannel=(ServerSocketChannel) key.channel();
							//非阻塞状态拿到客户端连接，（accept执行后该事件就不阻塞了）
							SocketChannel socketChannel=serverSocketChannel.accept();
							try {
								ClientHandler clientHandler = new ClientHandler(socketChannel,TCPServer.this);
								//收到信息后转发给其他客户端
							    clientHandler.readToPrint();
							    //添加同步处理
								synchronized (TCPServer.this) {
									clientHandlerList.add(clientHandler);
								}
							} catch (Exception e) {
								System.out.println("客户端连接异常!"+e.getMessage());
								e.printStackTrace();
							} 
				 		}
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			} while(!done);
			System.out.println("服务器已关闭!");
		}
		public void exit() {
			if(done!=true) {
				done=true;
			}
			//唤醒当前的阻塞
			selector.wakeup();
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
		System.out.println("服务器收到消息:"+"["+msg+"]"+"{"+handler.getClientInfo()+"}");
	}	
}
