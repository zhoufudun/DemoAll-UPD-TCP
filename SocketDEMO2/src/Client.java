import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

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
        //��������������
        InputStream in =System.in;
        BufferedReader input=new BufferedReader(new InputStreamReader(in));

        //�õ�socket�����������ת�ɴ�ӡ��
        OutputStream outputStream=client.getOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        //�õ�socket������������ת��BufferedReader
        InputStream inputStream=client.getInputStream();
        BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));

        boolean flag=true;
        do{
            //���̶�ȡһ��
            String str=input.readLine();//�ȴ����ݣ������߳�

            //���͵�������
            printStream.println(str);

            //��ȡ������һ����Ϣ
            String echo=reader.readLine();
            if("bye".equalsIgnoreCase(echo)){
                System.out.println("�������˳��������ر�");
                flag=false;
            }else{
                System.out.println(echo);
            }
        }while(flag);
        //�ͷ���Դ
        reader.close();
        printStream.close();
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