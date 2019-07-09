package com;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/***
 * UDPSearch,���������ߣ�������������
 ***/
public class UDPSearch {    
    private static final int LISTER_PORT = 30000;//�����˿�
    private static CountDownLatch countDownLatch=null;
	public static void main(String[] args) throws Exception {  		
		//���������𣿣���
		Listener listener=listen();//�����ȼ����ڷ��ͣ����ȷ��Ϳ��ܶԷ�������Ϣ��������ߵĶ˿ڶ���û����
		sendBroadcast();//���͹㲥
		countDownLatch.await();//countDown=0����ִ�����²���
		System.in.read();//����������ֱ������������Ϣ
		List<Devices> deviceList =listener.getDevicesAndClose();
		for(Devices device:deviceList) {
			System.out.println(device.toString());
		}
		listener.close();
    }
    private static void sendBroadcast() throws Exception {    	
    	System.out.println("�����߷��͹㲥��ʼ");
    	DatagramSocket ds=new DatagramSocket();//�����߶˿�ϵͳ�Լ�����   	
    	//����һ�ݷ�������
        String sendData=MessageCreator.buildWithPort(LISTER_PORT);
        byte[] sendDataBytes=sendData.getBytes();
    	DatagramPacket sendPack=new DatagramPacket(sendDataBytes, sendDataBytes.length);
    	sendPack.setAddress(InetAddress.getByName("255.255.255.255"));//������Ϣ���͵�ip��ַ,255.255.255.255���޵ľ�����
    	sendPack.setPort(20000);//������Ϣ���͵��Ķ˿�
    	ds.send(sendPack);
    	ds.close();//������رռ��ɣ����������Ǽ���30000�˿� 	
    	System.out.println("�����߷��͹㲥����");
    }
    private static Listener listen() {
    	countDownLatch=new CountDownLatch(1);
    	Listener listener=new Listener(LISTER_PORT,countDownLatch);
    	listener.start();  
    	return listener;
    }
    /**
     * �豸��Ϣ
     * @author 12159
     *
     */
    private static class Devices{
    	private int port;
    	private String sn;
    	private String ip;
    	
		public Devices(int port, String sn, String ip) {
			super();
			this.port = port;
			this.sn = sn;
			this.ip = ip;
		}
		public Devices() {
			super();			
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
		public String getSn() {
			return sn;
		}
		public void setSn(String sn) {
			this.sn = sn;
		}
		public String getIp() {
			return ip;
		}
		public void setIp(String ip) {
			this.ip = ip;
		}
		@Override
		public String toString() {
			return "Devices [port=" + port + ", sn=" + sn + ", ip=" + ip + "]";
		}
    	
    }
    private static class Listener extends Thread{
    	private int port;//�����Ķ˿�
    	private CountDownLatch countDownLatch;//����
    	private List<Devices> DeviceList=new ArrayList<Devices>();
    	private boolean done=false;
    	private DatagramSocket ds=null;
		public Listener(int port,CountDownLatch countDownLatch) {
			super();
			this.port=port;
			this.countDownLatch=countDownLatch;
		}

		@Override
		public void run() {
			super.run();
			countDownLatch.countDown();//-1����
			try {
				ds=new DatagramSocket(port);//����30000�˿�
				while(!done) {
					//��Ϊ�������ƶ�һ���˿����ڽ�����Ϣ
			    	final byte[] buf=new byte[1024];
			    	DatagramPacket receivePack=new DatagramPacket(buf, buf.length);
			    	//������Ϣ
			    	//System.out.println("v'v'v'v");
			    	ds.receive(receivePack);
			    	System.out.println("��Ϣ�����߽��յ��ṩ�ߵ���Ϣ��");
			    	//��ӡ�����߽��յ�����Ϣ�뷢���ߵ���Ϣ
			    	//������ip
			    	String ip=receivePack.getAddress().getHostAddress();//ip
			    	int port=receivePack.getPort();//port
			    	int dataLen=receivePack.getLength();
			    	String data=new String(receivePack.getData(),0,dataLen);
			        System.out.println("��Ϣ�����߽��յ�������Ϊ ��"+data+" �Է���ipΪ��"+ip+" �Է��Ķ˿�Ϊ:"+port+" ���ݳ���Ϊ:"+dataLen);
			    	       		      
			        //����sn
			        String sn=MessageCreator.parseSN(data);
			        if(sn!=null) {
			        	Devices devices=new Devices(port,sn,ip);
			        	DeviceList.add(devices);
			        }			       
				}
			} catch (Exception e) {
				
			}finally {
				close();
			}
			System.out.println("��Ϣ�����Ѿ���ɣ�");
		}
		private void close() {
			if(ds!=null) {
				ds.close();
				ds=null;
			}
		}
		private List<Devices> getDevicesAndClose(){
			done=true;
			close();
			return DeviceList;
		}
    	
    }
}
