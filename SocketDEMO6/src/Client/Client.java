package Client;

import entity.ServerInfo;
/**
 * UDP辅助TCP实现点对点传输
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
