import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
public class Client {
	
    public static void main(String[] agr)throws Exception{
        Socket socket=new Socket();
        socket.setSoTimeout(3000);
        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(),2222),3000);
        System.out.println("�ѷ������������");
        System.out.println("��������Ϣ"+socket.getInetAddress()+" port:"+socket.getPort());
        System.out.println("�ͻ�����Ϣ"+socket.getLocalAddress()+" port:"+socket.getLocalPort());
        try {
            //��������
            send(socket);
            System.out.println("�쳣�ر�");
        }catch (Exception e){
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
            String str=input.readLine();//�ȴ����ݣ�����

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
   
}
