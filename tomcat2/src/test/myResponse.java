package test;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 2.封装响应对象
 * 基于HTTP协议的格式进行输出写入。
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
                .append("Content-Type: text/html\n")
                .append("\r\n")
                .append("<html><body>")
                .append(content)
                .append("</body></html>");
        System.out.println(httpResponse.toString());
        outputStream.write(httpResponse.toString().getBytes());
        outputStream.close();
    }
}
