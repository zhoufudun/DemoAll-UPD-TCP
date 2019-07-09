package synchronizedDemo1;

public class Test2 {
	
    public static final Object obj = new Object();

    public static void main(String[] args) {

           new Thread( new Produce2()).start();
           new Thread( new Consumer2()).start();

    }
}