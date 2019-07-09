
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
 * 使用byteBuffer
 * @author 12159
 *
 */
public class Client {

    private static final int PORT=22222;//远端端口
    private static final int LOCAL_PORT=22220;//本地端口
    public static void main(String[] agr)throws Exception{
    	Socket socket =createSocket();
    	initSocket(socket);
    	//连接到22222端口
    	socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(),PORT), 30000);
        System.out.println("已发起服务器连接");
        System.out.println("服务器信息"+socket.getInetAddress()+" port:"+socket.getPort());
        System.out.println("客户端信息"+socket.getLocalAddress()+" port:"+socket.getLocalPort());
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
        
        //得到socket输出流，并且转成打印流
        OutputStream outputStream=client.getOutputStream();
   
        //得到socket输入流，并且转成BufferedReader
        InputStream inputStream=client.getInputStream();
    
        boolean flag=true;
        do{ 
        	
        	ByteBuffer bf=ByteBuffer.allocate(1024);//分配1024字节空间
        	//byte  1字节
        	bf.put((byte)-128);//一个byte存储整数值大小为-128~127
        	
        	//char 1字节
        	char c= 'a';
        	bf.putChar(c);
        	
        	//float 4字节
        	float f=(float) 2.1;
        	bf.putFloat(f);
        	
        	//int 4字节
        	int i=121212;
        	bf.putInt(i);
        	
        	//double 8字节
        	double d=21.23554;
        	bf.putDouble(d);
        	
        	//boolean 
        	boolean b=true;
        	bf.put((byte) (b?1:0));
        	
        	
        	//Long 8字节
        	long l=2235545;
        	bf.putLong(l);
        	
        	//Short 2字节
        	Short s=555;
        	bf.putShort(s);
        	
        	//String   String放在最后，应为前面的基础数据都是固定的String长度不固定。
        	String  str=" ByteBuffer测试";
        	bf.put(str.getBytes());
        	
        	//发送到服务器 ,position为bye数组的指针位置，每添加一个数，指针往前移动一个
        	outputStream.write(bf.array(),0,bf.position()+1);//发送         
            //读取信息
        	byte[] buffer=new byte[1024];
            int len=inputStream.read(buffer);//读取数据放入buffer
            ByteBuffer bf2=ByteBuffer.wrap(buffer, 0, len);
            if(len>0) {
            	//System.out.println("收到数据为： "+new String(buffer,0,len));//会转化出错
            	//System.out.println("收到数据为："+Array.getByte(buffer, 0));
            	System.out.println("服务器回送数据为："+new String(buffer,0,len));
            }
            flag=false;
        }while(flag);
        //释放资源
        outputStream.close();
        inputStream.close();
    }
    public static Socket createSocket() throws IOException {   	
    	Socket socket=new Socket();  	
        socket.bind(new InetSocketAddress(Inet4Address.getLocalHost(),LOCAL_PORT));//绑定本地端口22220
        return socket;	
    }
    public static void initSocket(Socket socket)throws Exception{
    	//设置读取超时时间为两秒
    	socket.setSoTimeout(3000);//设置有阻塞的地方，3秒延时时间，超过3秒抛出异常
    	//是否复用完全关闭的socket地址，对于指定bind操作后的套接字有效
    	socket.setReuseAddress(true);
    	//是否开启Nagle算法,()???
    	socket.setTcpNoDelay(false);
    	//是否需要在长时间时无数据响应时发送确认数据（类似心跳包），时间为大约2小时
    	socket.setKeepAlive(true);
    	//对于close关闭操作行为进行怎样的处理，默认为false,0
    	//false,0:默认情况，关闭时立即返回,底层系统接管输出流，将缓冲区内的数据发送完成
    	//true,0:关闭时立即返回，缓冲区数据抛弃，直接发送RST结束命令到对方，无需经过2MSL等待
    	//true,200:关闭时最长阻塞200ms，随后按第二种情况处理。
    	socket.setSoLinger(true, 20);
    	//是否让紧急数据内敛，默认false:紧急数据通过socket.sendUrgentData(1)；发送
    	socket.setOOBInline(true);
    	//设置接收和发送缓冲区大小
    	socket.setReceiveBufferSize(64*1024*1024);
    	socket.setSendBufferSize(64*1024*1024);
    	
    	//设置性能参数：短链接，延迟，带宽的相对重要性
    	socket.setPerformancePreferences(1, 1, 1 );
    }
}
