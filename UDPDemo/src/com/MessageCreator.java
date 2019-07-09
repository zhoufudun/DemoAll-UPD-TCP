package com;

public class MessageCreator {
	private static final String SN="�յ����ţ����ǣ�SN����";
	private static final String PORT="���ǰ��ţ���ص�˿ڣ�PORT����";
	
	public static String buildWithPort(int port) {
		return PORT+port;
	}
	public static int parsePort(String data) {
		if(data.startsWith(PORT)) {
			return Integer.parseInt(data.substring(PORT.length()));
		}else {
			return -1;//����
		}
	}
	public static String buildWithSN(String S) {
		return SN+S;
	}
	public static String parseSN(String data) {
		if(data.startsWith(SN)) {
			return data.substring(SN.length());
		}else {
			return null;//����
		}
	}
}
