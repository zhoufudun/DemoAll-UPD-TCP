import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
  public static void main(String[] agr)throws Exception{
        ServerSocket server=new ServerSocket(2222);//�������˿�

        System.out.print("������׼������~");
        System.out.print("��������Ϣ"+server.getInetAddress()+" port:"+server.getLocalPort());
        //����ѭ��
        for(;;){
            //�õ��ͻ���
            Socket client =server.accept();//�������ȴ��ͻ�������
            //�ͻ��˹����첽�߳�
            ClientHandler handler=new ClientHandler(client);
            //�����߳�
            handler.start();//String str=serverInput.readLine();//�ȴ����ݣ������߳�
            System.out.println("   KKK");//һ������run�����������������������ִ��
        }


  }
  private static class ClientHandler extends Thread{
      private Socket client;
      private boolean flag=true;

      ClientHandler(Socket client){
          this.client=client;
      }
      @Override
      public void run() {
          super.run();
          System.out.println("�¿ͻ�����Ϣ��"+client.getInetAddress()+" port:"+client.getPort());
          try {

              //�õ���ӡ�������������������������������ʹ��
              PrintStream serverOutput=new PrintStream(client.getOutputStream());
              //�õ����������������ݽ���
              BufferedReader serverInput=new BufferedReader(new InputStreamReader(client.getInputStream()));

              do{
                  //�õ��ͻ��˵�һ������
                  String str=serverInput.readLine();//�ȴ����ݣ�����
                  if("bye".equalsIgnoreCase(str)){
                      flag=false;
                      //���͸��ͻ���
                      serverOutput.println("bye");
                  }else{
                      System.out.println(str);
                      serverOutput.println("��Ϣ���ԣ�"+str);
                  }

              }while(flag);
              //�ͷ���Դ
              serverOutput.close();
              serverInput.close();

          }catch (Exception e){
              System.out.println("�����쳣�Ͽ�");
              e.printStackTrace();
          }

      }
  }

}