package com.UDP.test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
public class ChatServer implements Runnable{
    private DatagramSocket ds;
    public ChatServer(DatagramSocket ds){
        this.ds = ds;
    }
    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            while(true){
                //2 建立数据包
                byte[] buf = new byte[1024];
                DatagramPacket dp = new DatagramPacket(buf,buf.length);
 
                //3 使用接收方法将数据存储到数据包中
                ds.receive(dp);
 
                //4 通过数据包的方法解析数据包中的数据 解析其中的数据，比如，地址、端口、数据内容
                String ip = dp.getAddress().getHostAddress();
                int port = dp.getPort();
                String text= new String(dp.getData(),0,dp.getLength());
                dp.getData();
                System.out.println(ip+":"+port+"发来消息:"+text);
                if(text.equals("over")){
                    System.out.println(ip+"退出聊天室");
                }
                //5 关闭资源
                //ds.close();
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
