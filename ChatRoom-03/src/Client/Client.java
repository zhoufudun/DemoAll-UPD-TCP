package Client;

import entity.ServerInfo;
/**
 * UDP????TCP????????
 * @author 12159
 *
 */
public class Client {
	public static void main(String[] args) {
		ServerInfo info=ClientSearch.searchServer(10000);
		System.out.println("server:"+info);
		if(info!=null) {
			 try {
				TCPClient.linkWith(info);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
