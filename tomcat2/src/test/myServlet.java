package test;
/**
 * 3.Tomcat是满足Servlet规范的容器，
 * 那么自然Tomcat需要提供API。常见的doGet/doPost/service方法。
 * @author 12159
 *
 */
public interface myServlet {
    public void doGet(myRequest myRequest, myResponse myResponse);

    public void doPost(myRequest myRequest, myResponse myResponse);

    public void service(myRequest myRequest, myResponse myResponse);
}
