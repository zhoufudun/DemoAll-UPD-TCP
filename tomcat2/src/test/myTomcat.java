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
 * 8.������
 * ������ܹ����� Tomcat �Ĵ������̣����� URL ��Ӧ����� Servlet ��ϵ�γɣ�
 * ���� HTTP Э�飬��װ����/��Ӧ�������÷���ʵ��������� Servlet ���д����ɡ�
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
            while (true) { //ѭ����������
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
            Class catClass = Class.forName(clazz);//java����
           
           // myServlet myServlet = (test.myServlet) myServletClass.newInstance();//newһ��ʵ��
           // myServlet.service(myRequest,myResponse);
           // ʵ���������
            Object obj = catClass.newInstance();
            // ������������з���
            Method[] methods = catClass.getMethods();
            // ѭ��������Ҫ�ķ���
            for(Method method : methods) {
                if("service".equals(method.getName())) {
                    // �������������invoke��һ�������������������Ƿ�����Ҫ�Ĳ���
                    method.invoke(obj, myRequest,myResponse);
                    System.out.println("service��������");
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
