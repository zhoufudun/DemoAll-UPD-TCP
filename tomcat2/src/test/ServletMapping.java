package test;
/**
 * 6.servletÓ³ÉäÎÄ¼þ
 * @author 12159
 *
 */
public class ServletMapping {
	private String servletName;
    private String url;
    private String MyClass;
    
	public ServletMapping(String servletName, String url, String myClass) {
		super();
		this.servletName = servletName;
		this.url = url;
		MyClass = myClass;
	}
	public String getServletName() {
		return servletName;
	}
	public void setServletName(String servletName) {
		this.servletName = servletName;
	}
	public String geturl() {
		return url;
	}
	public void seturl(String url) {
		this.url = url;
	}
	public String getMyClass() {
		return MyClass;
	}
	public void setMyClass(String MyClass) {
		this.MyClass = MyClass;
	}
    
}
