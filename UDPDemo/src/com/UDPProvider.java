package com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.UUID;

/***
 * UDPProvider提供者实现，用于提供服务
 ***/
public class UDPProvider {
     
    public static void main(String[] args) throws Exception {
    	String sn=UUID.randomUUID().toString();
    	ProviderThread providerThread=new ProviderThread(sn);
    	providerThread.start();
    	
    	System.in.read();//键盘输入任何内容 退出；
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
			System.out.println("信息提供者开始监听端口");
		    try {
				ds=new DatagramSocket(20000);//监听20000端口
				while(!done) {
					//作为接收者制定一个端口用于接收信息
			    	final byte[] buf=new byte[1024];
			    	DatagramPacket receivePack=new DatagramPacket(buf, buf.length);
			    	//接收信息
			    	System.out.println("receive函数以下代码阻塞，但是函数外的不会阻塞，阻塞是指该线程阻塞");
			    	ds.receive(receivePack);
			    	System.out.println("接收数据成功");
			    	//打印接收者接收到的信息与发送者的信息
			    	//发送者ip
			    	String ip=receivePack.getAddress().getHostAddress();//ip
			    	int port=receivePack.getPort();//port
			    	int dataLen=receivePack.getLength();
			    	String data=new String(receivePack.getData(),0,dataLen);
			        System.out.println("UDP提供者接收到的数据为 ："+data+" 对方的ip为："+ip+" 对方的端口为:"+port+" 数据长度为:"+dataLen);
			    	
		       
			        //解析端口
			        int responsePort=MessageCreator.parsePort(data);//回电的端口30000
			        System.out.println("回电端口为："+responsePort);
			        //DatagramSocket ds2=new DatagramSocket();//
			        if(responsePort!=-1) {
			        	//构建一份回送数据
			        	 String responseData=MessageCreator.buildWithSN(SN);//
				         byte[] resposneDataBytes=responseData.getBytes();
				         DatagramPacket sendPack=new DatagramPacket(resposneDataBytes, resposneDataBytes.length);
				         
				         sendPack.setAddress(InetAddress.getByName(ip));
				     	 sendPack.setPort(responsePort);//设置回电端口30000
				     	 ds.send(sendPack);	
				     	// ds2.close();
				    	 System.out.println("信息提供者回送消息成功");
			        }
			       
				}
			} catch (Exception ignored) {				
				
			}finally {
				close();
			}
		    System.out.println("信息提供者任务完成 ");    			
		}
		
		
		/**
		 * 退出
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
