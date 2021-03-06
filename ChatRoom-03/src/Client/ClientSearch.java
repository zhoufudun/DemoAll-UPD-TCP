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
	//UDP监听端口
	private static final int UDP_LISTEN_PORT=UDPConstants.UDP_PORT_CLIENT_RESPONSE;	
	
	public static ServerInfo searchServer(int timeout) {
		System.out.println("UDPSearcher Started...");
		//成功收到回送栅栏
		CountDownLatch receiveLatch=new CountDownLatch(1);
		Listener listen=null;
		try {
			listen=listen(receiveLatch);//必须先监听在发送，如先发送可能对方回送消息后，我们这边的端口都还没开启
			sendBroadcast();//发送后countDown
			receiveLatch.await(timeout, TimeUnit.MICROSECONDS);//阻塞10秒后receiveLatch.getCount()!=0; 抛出异常		
		} catch (Exception e) {
			e.printStackTrace();
		}
		//完成
		System.out.println("lintener Finished");
		if(listen==null) {
			System.out.println("UDPSearcher 线程启动失败");
			return null;
		}else {			
			return listen.getServerAndClose().get(0);			
		}
		
	}
	private static void sendBroadcast() throws Exception {
		System.out.println("搜索者发送广播开始");
    	DatagramSocket ds=new DatagramSocket();//搜索者端口系统自己分配   	
    	//构建一份发送数据
       // byte[] b=new byte[128];
		ByteBuffer byteBuffer=ByteBuffer.allocate(128);
		byteBuffer.put(UDPConstants.HEADER);
		byteBuffer.putShort((short)1);
		byteBuffer.putInt(UDPConstants.UDP_PORT_CLIENT_RESPONSE);
		//????????????
    	DatagramPacket sendPack=new DatagramPacket(byteBuffer.array(), byteBuffer.position()+1);
    	sendPack.setAddress(InetAddress.getByName("255.255.255.255"));//设置信息发送的ip地址,255.255.255.255受限的局域网
    	sendPack.setPort(UDPConstants.UDP_PORT_SERVER);//设置信息发送到的端口
    	ds.send(sendPack);
    	ds.close();//发送完关闭即可，接下来就是监听30000端口 	
    	System.out.println("搜索者发送广播结束");
	}
	private static Listener listen(CountDownLatch receiveLatch) throws InterruptedException {
		System.out.println("UDPSearcher start Listen");
		CountDownLatch startLatch=new CountDownLatch(1);
		Listener listener=new Listener(UDP_LISTEN_PORT,receiveLatch,startLatch);
		listener.start();
		startLatch.await();//线程阻塞在这直到startLatch.getCount()==0
		return listener;
	}
	public static class Listener extends Thread{
		private int port;//客户端监听的UDP端口
		private CountDownLatch receiveLatch;
		private CountDownLatch startLatch;
		private List<ServerInfo> ServerInfoList=new ArrayList<>();
		private DatagramSocket ds=null;
		byte[] buffer=new byte[128];//接收消息
		private static final int minLen=UDPConstants.HEADER.length+2+4;
		private boolean done=false;
		
		public Listener(int port, CountDownLatch receiveLatch, CountDownLatch startLatch) {
			super();
			this.port = port;//客户端监听的UDP端口
			this.receiveLatch = receiveLatch;
			this.startLatch = startLatch;
		}

		@Override
		public void run() {
			super.run();
			//通知已经启动
			startLatch.countDown();//减一
			try {
				ds=new DatagramSocket(port);//监听客户端的端口-UDP	
				DatagramPacket receivePacket=new DatagramPacket(buffer,buffer.length);			
				while(!done) {
					//接收
					ds.receive(receivePacket);
					//打印接收者的信息和发送者的信息
					String serverIp=receivePacket.getAddress().getHostAddress();
					int serverPort=receivePacket.getPort();
					int serverDataLen=receivePacket.getLength();
					byte[] serverData=receivePacket.getData();
					//2字节的short-用户，代表命令类型（1，2），4字节的int，代表服务器TCP端口
					boolean isValid=(serverDataLen>=(UDPConstants.HEADER.length+2+4));
					System.out.println("UDPSearcher 接收到了来自IP: "+serverIp+" 端口号: "+serverPort+" 的消息,消息有效性为："+ isValid);
					if(!isValid) {
						//消息无效继续
						continue;
					} 			
					ByteBuffer byteBuffer=ByteBuffer.wrap(serverData,UDPConstants.HEADER.length,serverDataLen-UDPConstants.HEADER.length);					
					short cmd=byteBuffer.getShort();
			        int port=byteBuffer.getInt();	
			        if(cmd!=2|| port!=TCPConstants.TCP_PORT_SERVER) {
			    		System.out.println("cmd出错或者TCP端口不对");
			        	continue;
			        }		   
					//解析sn	
					String sn=new String(buffer,minLen,serverDataLen-minLen);
			        if(sn!=null) {
			        	ServerInfo serverInfo=new ServerInfo(sn,port,serverIp);
			        	ServerInfoList.add(serverInfo);
			        	//System.out.println("server:"+serverInfo);
			        	//通知已经启动
			        	receiveLatch.countDown();
			        }	
			        
				}
			}catch(Exception e) {
				//忽略
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
