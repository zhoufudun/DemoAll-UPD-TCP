package test;

import java.util.ArrayList;
import java.util.List;

/**
 * 6.servletӳ�������ļ�
 * ģ�� web.xml��
	ָ���ĸ� URL �����ĸ� servlet ���д���
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
