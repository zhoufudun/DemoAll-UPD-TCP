package test;

import java.util.ArrayList;
import java.util.List;

/**
 * 6.servlet映射配置文件
 * 模拟 web.xml，
	指定哪个 URL 交给哪个 servlet 进行处理。
 * @author 12159
 *
 */
public class ServletMappingConfig {
    
    public static List<ServletMapping> servletMappingList = new ArrayList<ServletMapping>();
    static {
        servletMappingList.add(new ServletMapping("findCar","/car","FindCarServlet"));
        servletMappingList.add(new ServletMapping("helloMyTomcat","/hello","HelloMyTomCatServlet"));
    }
	public static List<ServletMapping> getServletMappingList() {
		return servletMappingList;
	}
	public static void setServletMappingList(List<ServletMapping> servletMappingList) {
		ServletMappingConfig.servletMappingList = servletMappingList;
	}
    
}
