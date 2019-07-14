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
	private final int port;//������TCP�˿�
	private ClientListener mListener;
	private final ExecutorService forwardingThreadPoolExecutor;//Ϊʲô����������������
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
			//����Ϊ������
			serverSocketChannel.configureBlocking(false);
			//�󶨱��ض˿�
			serverSocketChannel.socket().bind(new InetSocketAddress(port));
			//ע��ͻ��������¼�
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			//
			System.out.println("��������Ϣ:"+serverSocketChannel.getLocalAddress().toString());
			//
			ClientListener clientListener=new ClientListener();
			mListener=clientListener;
			mListener.start();
		} catch (Exception e) {
			return false;//����������ʧ��
		}
		return true;
	}
	public void stop(){
		if(mListener!=null) {
			mListener.exit();
		}
		CloseUtils.CloseAll(selector);
		CloseUtils.CloseAll(serverSocketChannel);
		//��֤˳��ִ��
		synchronized (TCPServer.this) {
			for(ClientHandler clientHandler:clientHandlerList) {
				clientHandler.exit();
			}
			clientHandlerList.clear();//����ͻ��˴���List
		}
		//�ر��̳߳�
		forwardingThreadPoolExecutor.shutdownNow();
		
	}
	//�ͻ��������������ͻ�������
	public class ClientListener extends Thread{		
		private boolean done=false;			
		public ClientListener() throws IOException {
		}
		@Override
		public void run() {			
			super.run();
			Selector selector=TCPServer.selector;
			System.out.println("������׼������...");
			//�ȴ��ͻ�������
			do {
				//�õ��ͻ���
			    Socket client;
				try {
					//0��ʾû�о������¼����������¼���
					if(selector.select()==0) {
						//���״̬����
						if(done) {
							break;
						}
						//����ɼ�����һ��ѭ��
						continue;
					}
					//�о������¼�ʱ
					Iterator<SelectionKey> iterator=selector.selectedKeys().iterator();
					while(iterator.hasNext()) {
						if(done) {
							break;
						}
						SelectionKey key=iterator.next();
						//�Ƴ��������
						iterator.remove();
						//��鵱ǰkey��״̬�Ƿ�ʱ���ǹ�ע��
						//�ͻ��˴ﵽ״̬
						if(key.isAcceptable()) {
							//�õ�����ע����¼�
							ServerSocketChannel serverSocketChannel=(ServerSocketChannel) key.channel();
							//������״̬�õ��ͻ������ӣ���acceptִ�к���¼��Ͳ������ˣ�
							SocketChannel socketChannel=serverSocketChannel.accept();
							try {
								ClientHandler clientHandler = new ClientHandler(socketChannel,TCPServer.this);
								//�յ���Ϣ��ת���������ͻ���
							    clientHandler.readToPrint();
							    //���ͬ������
								synchronized (TCPServer.this) {
									clientHandlerList.add(clientHandler);
								}
							} catch (Exception e) {
								System.out.println("�ͻ��������쳣!"+e.getMessage());
								e.printStackTrace();
							} 
				 		}
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			} while(!done);
			System.out.println("�������ѹر�!");
		}
		public void exit() {
			if(done!=true) {
				done=true;
			}
			//���ѵ�ǰ������
			selector.wakeup();
		}
	}
	
	//���͸��ͻ���
	public void boradcast(String str) {		
		synchronized (TCPServer.this) {
			for(ClientHandler clientHandler:clientHandlerList) {
				clientHandler.send(str);
			}
		}
	}
	//ClientHandler ����ر��Լ�����
	@Override
	public synchronized void SelfClosed(ClientHandler Handler) {
		clientHandlerList.remove(Handler);
	}
	//ClientHandler �յ���Ϣ����Ϣ����
	@Override
	public void onNewMessageArrived(ClientHandler handler, String msg) {
		//�첽�ύת������  
		forwardingThreadPoolExecutor.execute(new Runnable() {			
			@Override
			public void run() {			
				synchronized (TCPServer.this) {
					for(ClientHandler clientHandler:clientHandlerList) {
					    //���������ClientHandler
						if(!handler.equals(clientHandler)) {
							clientHandler.send(msg);
						}
					}	
				}
					
			}
		});
		//��ӡ����Ļ��
		System.out.println("�������յ���Ϣ:"+"["+msg+"]"+"{"+handler.getClientInfo()+"}");
	}	
}
