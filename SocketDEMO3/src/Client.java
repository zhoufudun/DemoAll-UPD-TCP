
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.tools.Tool;
/**
 * ʹ��byteBuffer
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
        	
        	ByteBuffer bf=ByteBuffer.allocate(1024);//����1024�ֽڿռ�
        	//byte  1�ֽ�
        	bf.put((byte)-128);//һ��byte�洢����ֵ��СΪ-128~127
        	
        	//char 1�ֽ�
        	char c= 'a';
        	bf.putChar(c);
        	
        	//float 4�ֽ�
        	float f=(float) 2.1;
        	bf.putFloat(f);
        	
        	//int 4�ֽ�
        	int i=121212;
        	bf.putInt(i);
        	
        	//double 8�ֽ�
        	double d=21.23554;
        	bf.putDouble(d);
        	
        	//boolean 
        	boolean b=true;
        	bf.put((byte) (b?1:0));
        	
        	
        	//Long 8�ֽ�
        	long l=2235545;
        	bf.putLong(l);
        	
        	//Short 2�ֽ�
        	Short s=555;
        	bf.putShort(s);
        	
        	//String   String�������ӦΪǰ��Ļ������ݶ��ǹ̶���String���Ȳ��̶���
        	String  str=" ByteBuffer����";
        	bf.put(str.getBytes());
        	
        	//���͵������� ,positionΪbye�����ָ��λ�ã�ÿ���һ������ָ����ǰ�ƶ�һ��
        	outputStream.write(bf.array(),0,bf.position()+1);//����         
            //��ȡ��Ϣ
        	byte[] buffer=new byte[1024];
            int len=inputStream.read(buffer);//��ȡ���ݷ���buffer
            ByteBuffer bf2=ByteBuffer.wrap(buffer, 0, len);
            if(len>0) {
            	//System.out.println("�յ�����Ϊ�� "+new String(buffer,0,len));//��ת������
            	//System.out.println("�յ�����Ϊ��"+Array.getByte(buffer, 0));
            	System.out.println("��������������Ϊ��"+new String(buffer,0,len));
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
    	//����close�رղ�����Ϊ���������Ĵ���Ĭ��Ϊfalse,0
    	//false,0:Ĭ��������ر�ʱ��������,�ײ�ϵͳ�ӹ�����������������ڵ����ݷ������
    	//true,0:�ر�ʱ�������أ�����������������ֱ�ӷ���RST��������Է������辭��2MSL�ȴ�
    	//true,200:�ر�ʱ�����200ms����󰴵ڶ����������
    	socket.setSoLinger(true, 20);
    	//�Ƿ��ý�������������Ĭ��false:��������ͨ��socket.sendUrgentData(1)������
    	socket.setOOBInline(true);
    	//���ý��պͷ��ͻ�������С
    	socket.setReceiveBufferSize(64*1024*1024);
    	socket.setSendBufferSize(64*1024*1024);
    	
    	//�������ܲ����������ӣ��ӳ٣�����������Ҫ��
    	socket.setPerformancePreferences(1, 1, 1 );
    }
}
