package Server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import constants.TCPConstants;

public class Server {
	public static void main(String[] agr)throws Exception{
		TCPServer tcpServer=new TCPServer(TCPConstants.TCP_PORT_SERVER);
		boolean isValid=tcpServer.start();
		if(!isValid) {
			System.out.println("Start TCP Server failed");
			return ;
		}
		///
		UDPProvider.start(TCPConstants.TCP_PORT_SERVER);//�����TCP�˿�		
		//��������������
		BufferedReader bfr=new BufferedReader(new InputStreamReader(System.in));
		String str;
		do{
			str=bfr.readLine();//��ȡһ����Ϣ
			tcpServer.boradcast(str);
		}while(!"00bye00".equalsIgnoreCase(str));
		
		//�˳�UDP
		UDPProvider.stop();
		///
		tcpServer.stop();
		
	}
}