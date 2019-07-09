package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

import constants.TCPConstants;
import entity.ServerInfo;

public class TCPClient {
	private static Socket socket;
    public static void linkWith(ServerInfo info)throws Exception {    	
    	socket=new Socket();
    	initSocket(socket);
    	//连接到30401端口
    	socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()),info.getPort()), 3000);
        System.out.println("已发起TCP服务器连接");
        System.out.println("TCP服务器信息"+socket.getInetAddress()+" port:"+socket.getPort());
        System.out.println("TCP客户端信息"+socket.getLocalAddress()+" port:"+socket.getLocalPort());
        try {
            //发送数据
            send(socket);   
        }catch (Exception e){
        	System.out.println("异常关闭");
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
            String str=input.readLine();//等待数据，阻塞线程

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
    public static void initSocket(Socket socket)throws Exception{
    	//设置读取超时时间为两秒
    	socket.setSoTimeout(3000);//设置有阻塞的地方，3秒延时时间，超过3秒抛出异常
    	//是否复用完全关闭的socket地址，对于指定bind操作后的套接字有效
    	//socket.setReuseAddress(true);
    	//是否开启Nagle算法,()???
    	//socket.setTcpNoDelay(false);
    	//是否需要在长时间时无数据响应时发送确认数据（类似心跳包），时间为大约2小时
    	//socket.setKeepAlive(true);
    	//对于close关闭操作行为进行怎样的处理，默认为false,0
    	//false,0:默认情况，关闭时立即返回,底层系统接管输出流，将缓冲区内的数据发送完成
    	//true,0:关闭时立即返回，缓冲区数据抛弃，直接发送RST结束命令到对方，无需经过2MSL等待
    	//true,200:关闭时最长阻塞200ms，随后按第二种情况处理。
    	//socket.setSoLinger(true, 20);
    	//是否让紧急数据内敛，默认false:紧急数据通过socket.sendUrgentData(1)；发送
    	//socket.setOOBInline(true);
    	//设置接收和发送缓冲区大小
    	socket.setReceiveBufferSize(64*1024*1024);
    	socket.setSendBufferSize(64*1024*1024);
    	//绑定本地端口30402
    	//socket.bind(new InetSocketAddress(Inet4Address.getLocalHost(), TCPConstants.TCP_PORT_CLIENT));//绑定本地端口30402;
    	//设置性能参数：短链接，延迟，带宽的相对重要性
    	//socket.setPerformancePreferences(1, 1, 1 );
    }
}
