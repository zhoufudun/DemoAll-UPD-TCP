package synchronizedDemo1;

public class Test {
	
    public static final Object obj = new Object();

    public static void main(String[] args) {
           new Thread( new Produce()).start();       
    }
}