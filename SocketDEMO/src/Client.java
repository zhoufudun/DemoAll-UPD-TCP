import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
public class Client {
	
    public static void main(String[] agr)throws Exception{
        Socket socket=new Socket();
        socket.setSoTimeout(3000);
        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(),2222),3000);
        System.out.println("已发起服务器连接");
        System.out.println("服务器信息"+socket.getInetAddress()+" port:"+socket.getPort());
        System.out.println("客户端信息"+socket.getLocalAddress()+" port:"+socket.getLocalPort());
        try {
            //发送数据
            send(socket);
            System.out.println("异常关闭");
        }catch (Exception e){
        }
        socket.close();
        System.out.println("客户端退出");
    }
    public static void send(Socket client) throws IOException {
        //构建键盘输入流
        InputStream in =System.in;
        BufferedReader input=new BufferedReader(new InputStreamReader(in));

        //得到socket输出流，并且转成打印流
        OutputStream outputStream=client.getOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        //得到socket输入流，并且转成BufferedReader
        InputStream inputStream=client.getInputStream();
        BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));

        boolean flag=true;
        do{
            //键盘读取一行
            String str=input.readLine();//等待数据，阻塞

            //发送到服务器
            printStream.println(str);

            //读取服务器一行信息
            String echo=reader.readLine();
            if("bye".equalsIgnoreCase(echo)){
                System.out.println("服务器退出，正常关闭");
                flag=false;
            }else{
                System.out.println(echo);
            }
        }while(flag);
        //释放资源
        reader.close();
        printStream.close();
    }
   
}
