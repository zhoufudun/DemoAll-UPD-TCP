import java.io.*;
import java.lang.reflect.Array;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

import Util.Tool;
/**
 * ����byte[]���鴫��
 * @author 12159
 *
 */
public class Client {

    private static final int PORT=22222;//Զ�˶˿�
    private static final int LOCAL_PORT=22220;//���ض˿�
    public static void main(String[] agr)throws Exception{
    	Socket socket =createSocket();
    	initSocket(socket);
    	//���ӵ�22222�˿�
    	socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(),PORT), 30000);
        System.out.println("�ѷ������������");
        System.out.println("��������Ϣ"+socket.getInetAddress()+" port:"+socket.getPort());
        System.out.println("�ͻ�����Ϣ"+socket.getLocalAddress()+" port:"+socket.getLocalPort());
        try {
            //��������
            send(socket);
            
        }catch (Exception e){
        	System.out.println("�쳣�ر�");
        }
        socket.close();
        System.out.println("�ͻ����˳�");
    }
    public static void send(Socket client) throws IOException {
        
        //�õ�socket�����������ת�ɴ�ӡ��
        OutputStream outputStream=client.getOutputStream();
   
        //�õ�socket������������ת��BufferedReader
        InputStream inputStream=client.getInputStream();
    
        boolean flag=true;
        do{ 
        	//���͵�������
        	outputStream.write(Tool.IntTransferToByteArray(127));//����127          
            //��ȡ��Ϣ
        	byte[] buffer=new byte[1024];
            int len=inputStream.read(buffer);//��ȡ���ݷ���buffer
            if(len>0) {
            	//System.out.println("�յ�����Ϊ�� "+new String(buffer,0,len));//��ת������
            	//System.out.println("�յ�����Ϊ��"+Array.getByte(buffer, 0));
            	System.out.println("��������������Ϊ��"+Tool.ByteArrayTransferToInt(buffer));
            }
            flag=false;
        }while(flag);
        //�ͷ���Դ
        outputStream.close();
        inputStream.close();
    }
    public static Socket createSocket() throws IOException {   	
    	Socket socket=new Socket();  	
        socket.bind(new InetSocketAddress(Inet4Address.getLocalHost(),LOCAL_PORT));//�󶨱��ض˿�22220
        return socket;	
    }
    public static void initSocket(Socket socket)throws Exception{
    	//���ö�ȡ��ʱʱ��Ϊ����
    	socket.setSoTimeout(3000);//�����������ĵط���3����ʱʱ�䣬����3���׳��쳣
    	//�Ƿ�����ȫ�رյ�socket��ַ������ָ��bind��������׽�����Ч
    	socket.setReuseAddress(true);
    	//�Ƿ���Nagle�㷨,()???
    	socket.setTcpNoDelay(false);
    	//�Ƿ���Ҫ�ڳ�ʱ��ʱ��������Ӧʱ����ȷ�����ݣ���������������ʱ��Ϊ��Լ2Сʱ
    	socket.setKeepAlive(true);
    	//����close�رղ�����Ϊ���������Ĵ�����Ĭ��Ϊfalse,0
    	//false,0:Ĭ��������ر�ʱ��������,�ײ�ϵͳ�ӹ�����������������ڵ����ݷ������
    	//true,0:�ر�ʱ�������أ�����������������ֱ�ӷ���RST��������Է������辭��2MSL�ȴ�
    	//true,200:�ر�ʱ�����200ms����󰴵ڶ������������
    	socket.setSoLinger(true, 20);
    	//�Ƿ��ý�������������Ĭ��false:��������ͨ��socket.sendUrgentData(1)������
    	socket.setOOBInline(true);
    	//���ý��պͷ��ͻ�������С
    	socket.setReceiveBufferSize(64*1024*1024);
    	socket.setSendBufferSize(64*1024*1024);
    	
    	//�������ܲ����������ӣ��ӳ٣������������Ҫ��
    	socket.setPerformancePreferences(1, 1, 1 );
    }
}