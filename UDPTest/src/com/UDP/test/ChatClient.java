package com.UDP.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ChatClient implements Runnable{
	    private DatagramSocket ds;
	    public ChatClient(DatagramSocket ds){
	        this.ds=ds;
	    }	 
	    @Override
	    public void run() {	       
	        try {
	            BufferedReader bufr = new BufferedReader(new InputStreamReader(System.in));
	            String line = null;	 
	            while((line=bufr.readLine())!=null){	 
	                byte[] buf = line.getBytes();	 
	                //使用DatagramPacket将数据封装到该对象包中
	                DatagramPacket dp =
	                        new DatagramPacket(buf,buf.length,InetAddress.getByName("localhost"),8888);
	                ds.send(dp);
	                if("over".equals(line)) {
	                	break;
	                }	                    
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
}
