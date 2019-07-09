/**
 * 娴嬭瘯
 */
package test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import java.nio.charset.Charset;
import org.beykery.jkcp.KcpOnUdp;
import org.beykery.jkcp.KcpServer;
import org.junit.Test;
/**
 *
 * @author beykery
 */
public class TestServer extends KcpServer
{

    public TestServer(int port, int workerSize)
    {
        super(port, workerSize);
    }

    @Override //接受来自客户端的消息
    public void handleReceive(ByteBuf bb, KcpOnUdp kcp)
    {
//        if (c == 0)
//        {
//            start = System.currentTimeMillis();
//        }
//        c++;
        String content = bb.toString(Charset.forName("utf-8"));
        ByteBuf msgBuf = PooledByteBufAllocator.DEFAULT.buffer(48);
		String msg="您好！"+content+",服务器收到消息了";
		msgBuf.writeBytes(msg.getBytes());
        System.out.println("msg:" + msg + " kcp--> " + kcp);//调用toString()
        
        kcp.send(msgBuf);//echo,发送给客户端
//        if (c <=1){      
//            kcp.send(msgBuf);//echo,发送给客户端
//        }else {     
//            System.out.println("cost:" + (System.currentTimeMillis() - start));
//        }
//        while(true) {
//        	kcp.send(headerBuf);//echo
//        }
    }

    @Override //服务器异常
    public void handleException(Throwable ex, KcpOnUdp kcp)
    {
        System.out.println(ex);
    }

    @Override  //服务器离开
    public void handleClose(KcpOnUdp kcp)
    {
        System.out.println("服务器离开了:" + kcp);
        System.out.println("waitSnd:" + kcp.getKcp().waitSnd());
    }

    private static long start;
    private static int c = 0;

    /**
     * 启动
     *
     * @param args
     */
   @Test
    public static void main(String[] args)
    {
        TestServer s = new TestServer(8888, 1); //表示一个线程
        s.noDelay(1, 10, 2, 1); //极速模式：
        s.setMinRto(10);//最小RTO 10ms
        s.wndSize(64, 64);//最大发送窗口和最大接收窗口大小，默认为32. 这个可以理解为 TCP的 SND_BUF 和 RCV_BUF，这个单位是包
        s.setTimeout(10 * 10000000); //设置延迟时间 100000s
        s.setMtu(512); //最大传输单元，纯算法协议并不负责探测 MTU，默认 mtu是1400字节，可以使用ikcp_setmtu来设置该值。该值将会影响数据包归并及分片时候的最大传输单元。
        s.start();//启动
    }
}
