package Server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class TCPServer implements ClientHandler.ClientHandlerCallBack {
	private final int port;//������TCP�˿�
	private ClientListener mListener;
	private final ExecutorService forwardingThreadPoolExecutor;//Ϊʲô����������������
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
			return false;//����������ʧ��
		}
		return true;
	}
	public void stop(){
		if(mListener!=null) {
			mListener.exit();
		}
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
		private ServerSocket server;
		private boolean done=false;			
		public ClientListener(int port) throws IOException {
			server=new ServerSocket(port);
			System.out.println("��������Ϣ��IP="+server.getInetAddress().getHostAddress()+",port="+server.getLocalPort());
		}
		@Override
		public void run() {			
			super.run();
			System.out.println("������׼������...");
			do {
				Socket client;
				try {
					client=server.accept();//�߳���������ȴ��ͻ�������
				} catch (Exception e) {
					//�ȴ���ʱ
					continue;
				}
				//
				try {
					ClientHandler clientHandler = new ClientHandler(client,TCPServer.this);
					synchronized (TCPServer.this) {
						clientHandlerList.add(clientHandler);
					}
					//�յ���Ϣ��ת���������ͻ���
				    clientHandler.readToPrint();
				} catch (IOException e) {
					System.out.println("�ͻ��������쳣!"+e.getMessage());
					e.printStackTrace();
				} 				
				
			}while(!done);
			System.out.println("�������ѹر�!");
		}
		public void exit() {
			if(done!=true) {
				done=true;
				try {
					server.close();
				} catch (IOException e) {
					System.out.println("�������ѹر��쳣!");
					e.printStackTrace();
				}
			}
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
	//ClientHandler �����ر��Լ�����
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
					    //����������ClientHandler
						if(!handler.equals(clientHandler)) {
							clientHandler.send(msg);
						}
					}	
				}
					
			}
		});
		//��ӡ����Ļ��
		System.out.println("�յ���Ϣ:"+"["+msg+"]"+"{"+handler.getClientInfo()+"}");
	}	
}