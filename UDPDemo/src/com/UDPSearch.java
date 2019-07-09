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
 * UDPSearch,服务搜素者，用于搜索服务
 ***/
public class UDPSearch {    
    private static final int LISTER_PORT = 30000;//监听端口
    private static CountDownLatch countDownLatch=null;
	public static void main(String[] args) throws Exception {  		
		//不会阻塞吗？？？
		Listener listener=listen();//必须先监听在发送，如先发送可能对方回送消息后，我们这边的端口都还没开启
		sendBroadcast();//发送广播
		countDownLatch.await();//countDown=0才能执行以下操作
		System.in.read();//阻塞在这里直到键盘输入信息
		List<Devices> deviceList =listener.getDevicesAndClose();
		for(Devices device:deviceList) {
			System.out.println(device.toString());
		}
		listener.close();
    }
    private static void sendBroadcast() throws Exception {    	
    	System.out.println("搜索者发送广播开始");
    	DatagramSocket ds=new DatagramSocket();//搜索者端口系统自己分配   	
    	//构建一份发送数据
        String sendData=MessageCreator.buildWithPort(LISTER_PORT);
        byte[] sendDataBytes=sendData.getBytes();
    	DatagramPacket sendPack=new DatagramPacket(sendDataBytes, sendDataBytes.length);
    	sendPack.setAddress(InetAddress.getByName("255.255.255.255"));//设置信息发送的ip地址,255.255.255.255受限的局域网
    	sendPack.setPort(20000);//设置信息发送到的端口
    	ds.send(sendPack);
    	ds.close();//发送完关闭即可，接下来就是监听30000端口 	
    	System.out.println("搜索者发送广播结束");
    }
    private static Listener listen() {
    	countDownLatch=new CountDownLatch(1);
    	Listener listener=new Listener(LISTER_PORT,countDownLatch);
    	listener.start();  
    	return listener;
    }
    /**
     * 设备信息
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
    	private int port;//监听的端口
    	private CountDownLatch countDownLatch;//并行
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
			countDownLatch.countDown();//-1操作
			try {
				ds=new DatagramSocket(port);//监听30000端口
				while(!done) {
					//作为接收者制定一个端口用于接收信息
			    	final byte[] buf=new byte[1024];
			    	DatagramPacket receivePack=new DatagramPacket(buf, buf.length);
			    	//接收信息
			    	//System.out.println("v'v'v'v");
			    	ds.receive(receivePack);
			    	System.out.println("信息搜索者接收到提供者的消息了");
			    	//打印接收者接收到的信息与发送者的信息
			    	//发送者ip
			    	String ip=receivePack.getAddress().getHostAddress();//ip
			    	int port=receivePack.getPort();//port
			    	int dataLen=receivePack.getLength();
			    	String data=new String(receivePack.getData(),0,dataLen);
			        System.out.println("信息搜索者接收到的数据为 ："+data+" 对方的ip为："+ip+" 对方的端口为:"+port+" 数据长度为:"+dataLen);
			    	       		      
			        //解析sn
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
			System.out.println("信息搜索已经完成！");
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
