package entity;

public class ServerInfo {
	private String sn;//服务器sn
	private int port;//服务器的TCP端口
	private String address;//服务器的地址
	public ServerInfo(String sn, int port, String address) {		
		this.sn = sn;
		this.port = port;
		this.address = address;
	}
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	@Override
	public String toString() {
		return "ServerInfo [sn=" + sn + ", port=" + port + ", address=" + address + "]";
	}
	
	
}
