package Util;

public class Tool {
	 //32λ��intת����byte����
     public static byte[] IntTransferToByteArray(int i) {
    	 return new byte[] {
    		 (byte)(i>>24 & 0xFF),
    		 (byte)(i>>16 & 0xFF),
    		 (byte)(i>>8 & 0xFF),
    		 (byte)(i>>0 & 0xFF)
    	 };
     }
     //byte����ת����32λ��int
     public static int ByteArrayTransferToInt(byte[] b) {
    	return  (b[0] & 0xFF)<<24 |
    			(b[1] & 0xFF)<<16 |
    			(b[2] & 0xFF)<<8 |
    			(b[3] & 0xFF)<<0 ;
     }
}
