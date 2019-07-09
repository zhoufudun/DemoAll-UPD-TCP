package Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;

import constants.UDPConstants;

public class UDPProvider {
	private static Provider PROVIDER_INSTANCE;
	
	public static void start(int port) {
		stop();
		String sn=UUID.randomUUID().toString();
		Provider provider=new Provider(sn,port);
		provider.start();
		PROVIDER_INSTANCE=provider;
	}
	public static void stop() {
		if(PROVIDER_INSTANCE!=null) {
			PROVIDER_INSTANCE.exit();
			PROVIDER_INSTANCE=null;
		}	
	}
	private static class Provider extends Thread{
		private final byte[] sn;//��������sn
		private final int port;//������TCP�˿�
		private boolean done=false;
		private DatagramSocket ds=null;
		//�洢��Ϣ��Buffer
		final byte[] buffer=new byte[128];				
		public Provider(String sn, int port) {
			super();
			this.sn =sn.getBytes();//���byte[]
			this.port = port;
		}
		@Override
		public void run() {
			super.run();
			System.out.println("UDPProvider Started");
			try {
				ds=new DatagramSocket(UDPConstants.UDP_PORT_SERVER);//�����������Ķ˿�-UDP
				DatagramPacket receivePacket=new DatagramPacket(buffer,buffer.length);
				while(!done) {
					//����
					ds.receive(receivePacket);
					//��ӡ�����ߵ���Ϣ�ͷ����ߵ���Ϣ
					String clientIp=receivePacket.getAddress().getHostAddress();
					int clientPort=receivePacket.getPort();
					int clientDataLen=receivePacket.getLength();
					byte[] clientData=receivePacket.getData();
					//2�ֽڵ�short-�û��������������ͣ�1��2����4�ֽڵ�int�������ͻ��˶˿�
					boolean isValid=(clientDataLen>=(UDPConstants.HEADER.length+2+4));
					System.out.println("ServerProvider ���յ�������IP: "+clientIp+" �˿ں�: "+clientPort+" ����Ϣ,��Ϣ��Ч��Ϊ��"+ isValid);
					if(!isValid) {
						//��Ϣ��Ч����
						continue;
					}
					//����������UDP���Ͷ˿�
					int index=UDPConstants.HEADER.length;//ǰ�˸��ֽ�
					short cmd=(short) ((clientData[index++]<<8)|(clientData[index++] & 0xFF));
					int responsePort=(clientData[index++] <<24)|
							((clientData[index++] & 0xff) <<16)|
							((clientData[index++] & 0xff) <<8)|
							((clientData[index++] & 0xff));
					//�жϺϷ���1��ʾ��������
					
					if(cmd==1 && responsePort>0) {
						//����һ�ݻ������� 
						ByteBuffer byteBuffer=ByteBuffer.wrap(buffer);
						byteBuffer.put(UDPConstants.HEADER);
						byteBuffer.putShort((short)2);//2��ʾ������Ϣ
						byteBuffer.putInt(port);//tcp�������˿�
						byteBuffer.put(sn);//������sn��Ψһֵ
						DatagramPacket resposneDP=new DatagramPacket(byteBuffer.array(),
								byteBuffer.array().length,receivePacket.getAddress(),responsePort);
						ds.send(resposneDP);
						System.out.println("ServerProvider ������Ϣ��IP: "+clientIp+" �˿ں�: "+responsePort);
						
					}else {
						System.out.println("���յ�����Ϣ�����⣬�˿ڲ��Ի������������");
					}					
				}
			} catch (Exception e) {
				
			}finally {
				close();
			}		
			System.out.println("UDPProvider Finished");
		}
		public void exit(){
			done=true;
			System.out.println("UDPProvider closeed");
			close();
		}	
		public void close() {
			if(ds!=null) {
				ds.close();
				ds=null;
			}
		}
	}
}