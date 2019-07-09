package test;

import java.io.IOException;
/**
 * 4.ÊµÏÖmyServlet£¬Ä£ÄâServletHttp
 * @author 12159
 *
 */
public class HelloMyTomCatServlet implements myServlet{

	@Override
	public void doGet(myRequest myRequest, myResponse myResponse) {
		 try {
             myResponse.write("get hello Success");
         } catch (IOException e) {
             e.printStackTrace();
         }
	}

	@Override
	public void doPost(myRequest myRequest, myResponse myResponse) {
		try {
            myResponse.write("post hello Success");
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	@Override
	public void service(myRequest myRequest, myResponse myResponse) {
		if (myRequest.getMethod().equalsIgnoreCase("POST")) {
            doPost(myRequest, myResponse);
        } else if (myRequest.getMethod().equalsIgnoreCase("GET")) {
            doGet(myRequest, myResponse);
        }
		
	}

}
