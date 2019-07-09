package test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;




/**
 * 8.启动类
 * 这里，你能够看到 Tomcat 的处理流程，即把 URL 对应处理的 Servlet 关系形成，
 * 解析 HTTP 协议，封装请求/响应对象，利用反射实例化具体的 Servlet 进行处理即可。
 * @author 12159
 *
 */
public class myTomcat {
    private int port = 8888;

    private Map<String, String> urlServletMap = new HashMap<String, String>();

    public myTomcat(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        initServletMapping();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("MyTomcat is Start..............");
            while (true) { //循环接受请求
                Socket socket = serverSocket.accept();
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                myRequest myRequest = new myRequest(inputStream);
                myResponse myResponse = new myResponse(outputStream);
                dispathch(myRequest, myResponse);
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initServletMapping(){
        for (ServletMapping servletMapping : ServletMappingConfig.servletMappingList) {
            urlServletMap.put(servletMapping.geturl(),servletMapping.getMyClass());
        }
    }

   
	private void dispathch(myRequest myRequest, myResponse myResponse) throws Exception{
        String clazz = "test."+urlServletMap.get(myRequest.getUrl());
       // String clazz2 = "FindCarServlet";
        if (myRequest.getUrl().equalsIgnoreCase("/favicon.ico")) {
            return;
        }
        try {
        	System.out.println(clazz);
            Class catClass = Class.forName(clazz);//java反射
           
           // myServlet myServlet = (test.myServlet) myServletClass.newInstance();//new一个实例
           // myServlet.service(myRequest,myResponse);
           // 实例化这个类
            Object obj = catClass.newInstance();
            // 获得这个类的所有方法
            Method[] methods = catClass.getMethods();
            // 循环查找想要的方法
            for(Method method : methods) {
                if("service".equals(method.getName())) {
                    // 调用这个方法，invoke第一个参数是类名，后面是方法需要的参数
                    method.invoke(obj, myRequest,myResponse);
                    System.out.println("service方法调用");
                }
                //System.out.println(method.getName());
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
			new myTomcat(8888).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
