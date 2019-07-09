package test;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 2.��װ��Ӧ����
 * ����HTTPЭ��ĸ�ʽ�������д�롣
 * @author 12159
 *
 */
public class myResponse {
	private OutputStream outputStream;
    
    public myResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(String content) throws IOException {
        StringBuffer httpResponse = new StringBuffer();
        httpResponse.append("HTTP/1.1 200 OK\n")
                .append("Content-Type: text/html; charset=utf-8\n")
                .append("\r\n")
                .append("<html><body>")
                .append(content)
                .append("</body></html>");
        //System.out.println(httpResponse.toString());
        outputStream.write(httpResponse.toString().getBytes());
        outputStream.close();
    }
}
