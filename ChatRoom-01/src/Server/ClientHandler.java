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
//����Ϊʲô���̳�Thread��������������������
//�������շ������̣߳����̺߳ͷ����̷ֿ߳�д
public class ClientHandler {
	  private final Socket client;    
      private final ClientReadHandler clientReadHandler;
      private final ClientWriteHandler clientWriterHandler;
      private final ClientHandlerCallBack clientHandlerCallBack;
      private final String clientInfo;//��¼�ÿͻ��˵���Ϣ
      ClientHandler(Socket client,ClientHandlerCallBack clientHandlerCallBack) throws IOException{
          this.client=client;
          this.clientReadHandler=new ClientReadHandler(client.getInputStream());
          this.clientWriterHandler=new ClientWriteHandler(client.getOutputStream());
          this.clientHandlerCallBack=clientHandlerCallBack;
          this.clientInfo="IP["+client.getInetAddress().getHostAddress()+"]" + "port["+client.getPort()+"]";
          System.out.println("�¿ͻ�����Ϣ��"+clientInfo);
	     
      }
      public String getClientInfo() {
    	  return clientInfo;
      }
      //�������˳�
      public void exit() {
    	  //�˳��Լ���clientReadHandler
    	  clientReadHandler.exit();  
    	  //�˳��Լ���clientWriterHandler
    	  clientWriterHandler.exit();  
    	  //�ͷ���Դ	    
          CloseUtils.CloseAll(client); 
          //
    	  //System.out.println("�������رգ��Ͽ���ͻ������ӣ��ͻ�����ϢΪ"+client.getInetAddress()+" port:"+client.getPort());	
      }
      //�Լ��˳��Լ�
      private void exitByeSelf() {
    	  exit();
    	  //֪ͨ�����Լ����Լ��ر���
    	  clientHandlerCallBack.SelfClosed(ClientHandler.this);
      }
      //������Ϣ
      public void send(String str) {
    	  clientWriterHandler.send(str);
      }
      //��ȡ�ͻ�����Ϣ���Ҵ�ӡ
      public void readToPrint() {
    	  clientReadHandler.start();//�����߳�
	  }
      //callback���أ���֪�������Լ��Ѿ��رգ���Ҫ�����潫�ͻ��˴���List��ȡ������Ϳͻ�����Ϣ
      public static interface ClientHandlerCallBack{
    	  //�����ر�֪ͨ
    	  void SelfClosed(ClientHandler Handler);
    	  //�յ���Ϣʱ֪ͨ��Ϊ�˷�ֹ������Ҫ�����ƣ������� 
    	  void onNewMessageArrived(ClientHandler handler,String msg);
      }
      //��ȡ�߳�
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
	              //�õ����������������ݽ���
	              BufferedReader serverInput=new BufferedReader(new InputStreamReader(inputStream));
	              do{
	                  //�õ��ͻ��˵�һ������
	                  String str=serverInput.readLine();//�ȴ����ݣ�����
	                  if(str==null) {
	                	  //��ȡ��ʱ�����쳣
	                	  System.out.println("�޷���ȡ�ͻ������ݣ�");
	                	  //�˳��ͻ���,�Լ��˳��Լ�
	                	  ClientHandler.this.exitByeSelf();                	  
	                	  break;
	                  }else if(!"00bye00".equalsIgnoreCase(str)) {
	                	  //�ͻ���ͬʱ���Ͷ������ݵ�������ʱ�����������������ܶ�ʧ���ݣ�������ﲻ��������������Ҫ��ʱִ�У������첽
	                	  //�ص������棬������ʵ�֣�֪ͨ��TCPServer
	                	  clientHandlerCallBack.onNewMessageArrived(ClientHandler.this, str);
	                  }
	              }while(!done);	                      	             
	          }catch (Exception e){
	        	  if(!done) {
	        		   System.out.println("�����쳣�Ͽ�");
	                   e.printStackTrace();
	        		   ClientHandler.this.exitByeSelf();
	        	  }	        
	          }finally {
	        	  //�ͷ���Դ	    
	        	  CloseUtils.CloseAll(inputStream);
	          }
		}  
		void exit() {
			done=true;
			CloseUtils.CloseAll(inputStream);
		}
      }
      //��������
      class ClientWriteHandler{
    	  private boolean done=false;
    	  private final PrintStream printStream;
    	  private ExecutorService executorService;
    	  ClientWriteHandler(OutputStream outputStream){
    		  this.printStream=new PrintStream(outputStream);
    		  executorService=Executors.newSingleThreadExecutor();//�����̳߳�
    	  }
    	  void exit() {
    		  done=true;
    		  executorService.shutdownNow();//�̳߳������ر�
    		  CloseUtils.CloseAll(printStream);
    	  } 
    	  public void send(String str) {
    		  //����ִ���߳�
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
				  //�Ѿ��˳�
				  if(ClientWriteHandler.this.done) {
					  return;
				  }
				  try {	
					  //���͸��ͻ���
					  ClientWriteHandler.this.printStream.println(mes);			
 
				  } catch (Exception e) {
					  e.printStackTrace();
				  }
			 }			    		  
    	  }	
    	   
      }
  
}