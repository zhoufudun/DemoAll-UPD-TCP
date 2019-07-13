package Utils;

import java.io.Closeable;
import java.io.IOException;

public class CloseUtils {
	public static void CloseAll(Closeable...Closeables) {
		if(Closeables==null) {
			return;
		}
		for(Closeable close:Closeables) {
			try {
				close.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
