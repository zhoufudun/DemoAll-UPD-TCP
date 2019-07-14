package Server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import Utils.CloseUtils;
//����Ϊʲô���̳�Thread��������������������
//�������շ������̣߳����̺߳ͷ����̷ֿ߳�д
public class ClientHandler {
	  private final SocketChannel socketChannel;    
      private final ClientReadHandler clientReadHandler;
      private final ClientWriteHandler clientWriterHandler;
      private final ClientHandlerCallBack clientHandlerCallBack;
      private final String clientInfo;//��¼�ÿͻ��˵���Ϣ
      ClientHandler(SocketChannel socketChannel,ClientHandlerCallBack clientHandlerCallBack) throws IOException{
          this.socketChannel=socketChannel;
          //���÷�����ģʽ
          socketChannel.configureBlocking(false);
          //
          Selector readSelector=Selector.open();
          //ע���ȡ�¼�
          socketChannel.register(readSelector,SelectionKey.OP_READ);
          this.clientReadHandler=new ClientReadHandler(readSelector);
          //
          Selector writeSelector=Selector.open();
          //ע���ȡ�¼�
          socketChannel.register(writeSelector,SelectionKey.OP_WRITE);
          this.clientWriterHandler=new ClientWriteHandler(writeSelector);
          //
          this.clientHandlerCallBack=clientHandlerCallBack;
          this.clientInfo=socketChannel.getLocalAddress().toString();
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
          CloseUtils.CloseAll(socketChannel); 
          //
    	  System.out.println("�ͻ����˳����ͻ�����ϢΪ"+clientInfo);	
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
    	  //����ر�֪ͨ
    	  void SelfClosed(ClientHandler Handler);
    	  //�յ���Ϣʱ֪ͨ��Ϊ�˷�ֹ������Ҫ�����ƣ������� 
    	  void onNewMessageArrived(ClientHandler handler,String msg);
      }
      //��ȡ�߳�
      class ClientReadHandler extends Thread{
    	  private boolean done=false;
    	  private final Selector selector;
    	  private final ByteBuffer byteBuffer;
    	  ClientReadHandler(Selector selector){
    		  this.selector=selector;
    		  this.byteBuffer=ByteBuffer.allocate(256);
    	  }
		@Override
		public void run() {
			super.run();
		     try {	          
	              do{
	                  //�õ��ͻ��˵�һ������
	                  if(selector.select()==0) {
	                	  //�˳�
	                	  if(done) {
	                		  break;
	                	  }
	                	  //û�˳�
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
							if(key.isReadable()) {
								//�õ�����ע����¼�
								SocketChannel socketChannel=(SocketChannel) key.channel();
								//���֮ǰ��ָ��λ�ã�ָ��ص�0
								byteBuffer.clear();
								//������״̬�õ�����
								int readLen=socketChannel.read(byteBuffer);
								//������
								if(readLen>0) {
									//�������з�
									String str=new String(byteBuffer.array(),0,readLen-1);
									if(!"00bye00".equalsIgnoreCase(str)) {
										//������÷��ͷ���
										//�ص������棬������ʵ�֣�֪ͨ��TCPServer
										clientHandlerCallBack.onNewMessageArrived(ClientHandler.this, str);
									}else {
										System.out.println("�ͻ����˳���");
										continue;
									}
								}else {
									//��ȡ��ʱ�����쳣
									System.out.println("�޷���ȡ�ͻ������ݣ�");
									//�˳��ͻ���,�Լ��˳��Լ�
									ClientHandler.this.exitByeSelf();                	  
									break;
								}
					 		}
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
	        	  CloseUtils.CloseAll(selector);
	          }
		}  
		void exit() {
			done=true;
			//���ѵ�ǰ������
			selector.wakeup();
			CloseUtils.CloseAll(selector);
		}
      }
      //��������
      class ClientWriteHandler{
    	  private boolean done=false;
    	  private final Selector selector;
    	  private final ByteBuffer byteBuffer;
    	  private ExecutorService executorService;
    	  ClientWriteHandler(Selector selector){
    		  this.selector=selector;
    		  //����256���ֽ�
    		  this.byteBuffer=ByteBuffer.allocate(256);
    		  executorService=Executors.newSingleThreadExecutor();//�����̳߳�
    	  }
    	  void exit() {
    		  done=true;
    		  executorService.shutdownNow();//�̳߳������ر�
    		  CloseUtils.CloseAll(selector);
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
					  //���֮ǰ��ָ��λ��
					  byteBuffer.clear();
					  byteBuffer.put(mes.getBytes());
					  //��ת�������������ص㣬ͨ������������Է�����Ч�����ݣ����߷���byteBuffer���������ݣ���һ����Ϊ��
					  byteBuffer.flip();
					  //δ��������byteBuffer������û�з���
					  while(!done&&byteBuffer.hasRemaining()) {
						try {
							//д
							int len=socketChannel.write(byteBuffer);
							//len=0�Ϸ�
							if(len<0) {
								System.out.println("�ͻ������޷��������ݣ�");
								ClientHandler.this.exitByeSelf();
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					  }
 
				  } catch (Exception e) {
					  e.printStackTrace();
				  }
			 }			    		  
    	  }	
      }
}
