package com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.UUID;

/***
 * UDPProvider�ṩ��ʵ�֣������ṩ����
 ***/
public class UDPProvider {
     
    public static void main(String[] args) throws Exception {
    	String sn=UUID.randomUUID().toString();
    	ProviderThread providerThread=new ProviderThread(sn);
    	providerThread.start();
    	
    	System.in.read();//���������κ����� �˳���
    	providerThread.exit();
    }
    private static class ProviderThread extends Thread{    	
    	private String SN;
    	private boolean done=false;
    	private DatagramSocket ds=null;
		public ProviderThread(String S) {
			super();
			this.SN=S;
		}

		@Override
		public void run() {
			super.run();
			System.out.println("��Ϣ�ṩ�߿�ʼ�����˿�");
		    try {
				ds=new DatagramSocket(20000);//����20000�˿�
				while(!done) {
					//��Ϊ�������ƶ�һ���˿����ڽ�����Ϣ
			    	final byte[] buf=new byte[1024];
			    	DatagramPacket receivePack=new DatagramPacket(buf, buf.length);
			    	//������Ϣ
			    	System.out.println("receive�������´������������Ǻ�����Ĳ���������������ָ���߳�����");
			    	ds.receive(receivePack);
			    	System.out.println("�������ݳɹ�");
			    	//��ӡ�����߽��յ�����Ϣ�뷢���ߵ���Ϣ
			    	//������ip
			    	String ip=receivePack.getAddress().getHostAddress();//ip
			    	int port=receivePack.getPort();//port
			    	int dataLen=receivePack.getLength();
			    	String data=new String(receivePack.getData(),0,dataLen);
			        System.out.println("UDP�ṩ�߽��յ�������Ϊ ��"+data+" �Է���ipΪ��"+ip+" �Է��Ķ˿�Ϊ:"+port+" ���ݳ���Ϊ:"+dataLen);
			    	
		       
			        //�����˿�
			        int responsePort=MessageCreator.parsePort(data);//�ص�Ķ˿�30000
			        System.out.println("�ص�˿�Ϊ��"+responsePort);
			        //DatagramSocket ds2=new DatagramSocket();//
			        if(responsePort!=-1) {
			        	//����һ�ݻ�������
			        	 String responseData=MessageCreator.buildWithSN(SN);//
				         byte[] resposneDataBytes=responseData.getBytes();
				         DatagramPacket sendPack=new DatagramPacket(resposneDataBytes, resposneDataBytes.length);
				         
				         sendPack.setAddress(InetAddress.getByName(ip));
				     	 sendPack.setPort(responsePort);//���ûص�˿�30000
				     	 ds.send(sendPack);	
				     	// ds2.close();
				    	 System.out.println("��Ϣ�ṩ�߻�����Ϣ�ɹ�");
			        }
			       
				}
			} catch (Exception ignored) {				
				
			}finally {
				close();
			}
		    System.out.println("��Ϣ�ṩ��������� ");    			
		}
		
		
		/**
		 * �˳�
		 * 
		 */
		public void exit() {
			done=true;
			close();
		}
		private void close() {
			if(ds!=null) {
				ds.close();
				ds=null;
			}
		}
    	
    }
}
