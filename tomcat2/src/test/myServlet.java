package test;
/**
 * 3.Tomcat������Servlet�淶��������
 * ��ô��ȻTomcat��Ҫ�ṩAPI��������doGet/doPost/service������
 * @author 12159
 *
 */
public interface myServlet {
    public void doGet(myRequest myRequest, myResponse myResponse);

    public void doPost(myRequest myRequest, myResponse myResponse);

    public void service(myRequest myRequest, myResponse myResponse);
}
