package com.UDP.test;

import java.net.DatagramSocket;
import java.net.SocketException;

public class Chatmain {
    public static void main(String[] args) throws SocketException {
        DatagramSocket send = new DatagramSocket();
        DatagramSocket rece = new DatagramSocket(8888);
 
        new Thread(new ChatClient(send)).start();
        new Thread(new ChatServer(rece)).start();
 
    }
}

