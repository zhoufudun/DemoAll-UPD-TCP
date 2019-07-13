package Client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import constants.TCPConstants;
import constants.UDPConstants;
import entity.ServerInfo;
public class ClientSearch {
	//UDP�����˿�
	private static final int UDP_LISTEN_PORT=UDPConstants.UDP_PORT_CLIENT_RESPONSE;	
	
	public static ServerInfo searchServer(int timeout) {
		System.out.println("UDPSearcher Started...");
		//�ɹ��յ�����դ��
		CountDownLatch receiveLatch=new CountDownLatch(1);
		Listener listen=null;
		try {
			listen=listen(receiveLatch);//�����ȼ����ڷ��ͣ����ȷ��Ϳ��ܶԷ�������Ϣ��������ߵĶ˿ڶ���û����
			sendBroadcast();//���ͺ�countDown
			receiveLatch.await(timeout, TimeUnit.MICROSECONDS);//����10���receiveLatch.getCount()!=0; �׳��쳣		
		} catch (Exception e) {
			e.printStackTrace();
		}
		//���
		System.out.println("lintener Finished");
		if(listen==null) {
			System.out.println("UDPSearcher �߳�����ʧ��");
			return null;
		}else {			
			return listen.getServerAndClose().get(0);			
		}
		
	}
	private static void sendBroadcast() throws Exception {
		System.out.println("�����߷��͹㲥��ʼ");
    	DatagramSocket ds=new DatagramSocket();//�����߶˿�ϵͳ�Լ�����   	
    	//����һ�ݷ�������
       // byte[] b=new byte[128];
		ByteBuffer byteBuffer=ByteBuffer.allocate(128);
		byteBuffer.put(UDPConstants.HEADER);
		byteBuffer.putShort((short)1);
		byteBuffer.putInt(UDPConstants.UDP_PORT_CLIENT_RESPONSE);
		//????????????
    	DatagramPacket sendPack=new DatagramPacket(byteBuffer.array(), byteBuffer.position()+1);
    	sendPack.setAddress(InetAddress.getByName("255.255.255.255"));//������Ϣ���͵�ip��ַ,255.255.255.255���޵ľ�����
    	sendPack.setPort(UDPConstants.UDP_PORT_SERVER);//������Ϣ���͵��Ķ˿�
    	ds.send(sendPack);
    	ds.close();//������رռ��ɣ����������Ǽ���30000�˿� 	
    	System.out.println("�����߷��͹㲥����");
	}
	private static Listener listen(CountDownLatch receiveLatch) throws InterruptedException {
		System.out.println("UDPSearcher start Listen");
		CountDownLatch startLatch=new CountDownLatch(1);
		Listener listener=new Listener(UDP_LISTEN_PORT,receiveLatch,startLatch);
		listener.start();
		startLatch.await();//�߳���������ֱ��startLatch.getCount()==0
		return listener;
	}
	public static class Listener extends Thread{
		private int port;//�ͻ��˼�����UDP�˿�
		private CountDownLatch receiveLatch;
		private CountDownLatch startLatch;
		private List<ServerInfo> ServerInfoList=new ArrayList<>();
		private DatagramSocket ds=null;
		byte[] buffer=new byte[128];//������Ϣ
		private static final int minLen=UDPConstants.HEADER.length+2+4;
		private boolean done=false;
		
		public Listener(int port, CountDownLatch receiveLatch, CountDownLatch startLatch) {
			super();
			this.port = port;//�ͻ��˼�����UDP�˿�
			this.receiveLatch = receiveLatch;
			this.startLatch = startLatch;
		}

		@Override
		public void run() {
			super.run();
			//֪ͨ�Ѿ�����
			startLatch.countDown();//��һ
			try {
				ds=new DatagramSocket(port);//�����ͻ��˵Ķ˿�-UDP	
				DatagramPacket receivePacket=new DatagramPacket(buffer,buffer.length);			
				while(!done) {
					//����
					ds.receive(receivePacket);
					//��ӡ�����ߵ���Ϣ�ͷ����ߵ���Ϣ
					String serverIp=receivePacket.getAddress().getHostAddress();
					int serverPort=receivePacket.getPort();
					int serverDataLen=receivePacket.getLength();
					byte[] serverData=receivePacket.getData();
					//2�ֽڵ�short-�û��������������ͣ�1��2����4�ֽڵ�int������������TCP�˿�
					boolean isValid=(serverDataLen>=(UDPConstants.HEADER.length+2+4));
					System.out.println("UDPSearcher ���յ�������IP: "+serverIp+" �˿ں�: "+serverPort+" ����Ϣ,��Ϣ��Ч��Ϊ��"+ isValid);
					if(!isValid) {
						//��Ϣ��Ч����
						continue;
					} 			
					ByteBuffer byteBuffer=ByteBuffer.wrap(serverData,UDPConstants.HEADER.length,serverDataLen-UDPConstants.HEADER.length);					
					short cmd=byteBuffer.getShort();
			        int port=byteBuffer.getInt();	
			        if(cmd!=2|| port!=TCPConstants.TCP_PORT_SERVER) {
			    		System.out.println("cmd��������TCP�˿ڲ���");
			        	continue;
			        }		   
					//����sn	
					String sn=new String(buffer,minLen,serverDataLen-minLen);
			        if(sn!=null) {
			        	ServerInfo serverInfo=new ServerInfo(sn,port,serverIp);
			        	ServerInfoList.add(serverInfo);
			        	//System.out.println("server:"+serverInfo);
			        	//֪ͨ�Ѿ�����
			        	receiveLatch.countDown();
			        }	
			        
				}
			}catch(Exception e) {
				//����
			}finally {
				close();
			}
			System.out.println("UDPSearcher Finished");
			
		}
		public List<ServerInfo> getServerAndClose(){
			done=true;
			close();
			return ServerInfoList;
		}	
		public void close() {
			if(ds!=null) {
				ds.close();
				ds=null;
			}
		}
		
	}
}	