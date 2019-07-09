package com;

public class MessageCreator {
	private static final String SN="收到暗号，我是（SN）：";
	private static final String PORT="这是暗号，请回电端口（PORT）：";
	
	public static String buildWithPort(int port) {
		return PORT+port;
	}
	public static int parsePort(String data) {
		if(data.startsWith(PORT)) {
			return Integer.parseInt(data.substring(PORT.length()));
		}else {
			return -1;//出错
		}
	}
	public static String buildWithSN(String S) {
		return SN+S;
	}
	public static String parseSN(String data) {
		if(data.startsWith(SN)) {
			return data.substring(SN.length());
		}else {
			return null;//出错
		}
	}
}
