import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	  
	  private static final int LOCAL_PORT=22222;//Զ�˶˿�
	  public static void main(String[] agr)throws Exception{
	        ServerSocket serverSocket=createSocket();
	        initServerSocket(serverSocket);	   
			serverSocket.bind(new InetSocketAddress(Inet4Address.getLocalHost(),LOCAL_PORT),50);//�󶨱��ص�ַ,����ȴ�50�����Ӷ��У�������50���ͻ���
	        System.out.println("������׼������~");
	        System.out.println("��������Ϣ"+serverSocket.getInetAddress()+" port:"+serverSocket.getLocalPort());
	        //����ѭ��
	        for(;;){
	            //�õ��ͻ���
	        	Socket client =serverSocket.accept();//�������ȴ��ͻ�������
	            //�ͻ��˹����첽�߳�
	            ClientHandler handler=new ClientHandler(client);
	            //�����߳�
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
	          System.out.println("�¿ͻ�����Ϣ��Ϣ��"+client.getInetAddress()+" port:"+client.getPort());
	          try {
	
	              //�õ���ӡ�������������������������������ʹ��
	              PrintStream serverOutput=new PrintStream(client.getOutputStream());
	              //�õ����������������ݽ���
	              BufferedReader serverInput=new BufferedReader(new InputStreamReader(client.getInputStream()));
	
	              do{
	                  //�õ��ͻ��˵�һ������
	                  String str=serverInput.readLine();//�ȴ����ݣ�����
	                  if("bye".equalsIgnoreCase(str)){
	                      flag=false;
	                      //���͸��ͻ���
	                      serverOutput.println("bye");
	                  }else{
	                      System.out.println(str);
	                      serverOutput.println("��Ϣ���ԣ�"+str);
	                  }
	
	              }while(flag);
	              //�ͷ���Դ
	              serverOutput.close();
	              serverInput.close();
	
	          }catch (Exception e){
	              System.out.println("�����쳣�Ͽ�");
	              e.printStackTrace();
	          }
	
	      }
	  }
	  public static ServerSocket createSocket() throws IOException {   	
		  ServerSocket serverSocket=new ServerSocket();	  	
	      return serverSocket;	
	  }
	  public static void initServerSocket(ServerSocket serverSocket)throws Exception{
	  	//����serverSocket��accept��ʱʱ��Ϊ3��
		//serverSocket.setSoTimeout(3000);//�����������ĵط���3����ʱʱ�䣬����3���׳��쳣
	  	//�Ƿ�����ȫ�رյ�socket��ַ������ָ��bind��������׽�����Ч
		serverSocket.setReuseAddress(true);
	  	//�Ƿ���Nagle�㷨,()???
		//serverSocket.setTcpNoDelay(false);
	  	//�Ƿ���Ҫ�ڳ�ʱ��ʱ��������Ӧʱ����ȷ�����ݣ���������������ʱ��Ϊ��Լ2Сʱ
		//serverSocket.setKeepAlive(true);
	  	//����close�رղ�����Ϊ���������Ĵ���Ĭ��Ϊfalse,0
	  	//false,0:Ĭ��������ر�ʱ��������,�ײ�ϵͳ�ӹ�����������������ڵ����ݷ������
	  	//true,0:�ر�ʱ�������أ�����������������ֱ�ӷ���RST��������Է������辭��2MSL�ȴ�
	  	//true,200:�ر�ʱ�����200ms����󰴵ڶ����������
		//serverSocket.setSoLinger(true, 20);
	  	//�Ƿ��ý�������������Ĭ��false:��������ͨ��socket.sendUrgentData(1)������
		//serverSocket.setOOBInline(true);
	  	//���ý��պͷ��ͻ�������С�����õõ���socket�Ĵ�С
		serverSocket.setReceiveBufferSize(64*1024*1024);	
	  	
	  	//�������ܲ����������ӣ��ӳ٣�����������Ҫ��
		serverSocket.setPerformancePreferences(1, 1, 1 );
	  }

}
