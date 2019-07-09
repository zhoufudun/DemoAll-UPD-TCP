package test;

import java.io.IOException;
import java.io.InputStream;
/**
 * 1.��װ�������
 * ͨ������������ HTTP Э����н�����
 * �õ��� HTTP ����ͷ�ķ����Լ� URL��
 * @author 12159
 *
 */
public class myRequest {
    private String url;
    private String method;

    public myRequest(InputStream inputStream) throws IOException {
        String httpRequest = "";
        byte[] httpRequestBytes = new byte[1024];
        int length = 0;
        if ((length = inputStream.read(httpRequestBytes)) > 0) {
            httpRequest = new String(httpRequestBytes, 0, length);
        }
       // System.out.println(httpRequest.toString());//
        String httpHead = httpRequest.trim().split("\n")[0];
        System.out.println(httpHead.toString());
        url = httpRequest.trim().split(" ")[1];
        System.out.println(url);
        method = httpRequest.trim().split(" ")[0];
        System.out.println(method);
        System.out.println(this + "----url:" + url + "---method:" + method);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}