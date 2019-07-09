package synchronizedDemo1;

public class Produce2 implements Runnable {

	@Override
	public void run() {
		int count = 10;
		while(count>0) {
			synchronized (Test.obj) { 				
				System.out.println("Produce");
                count--;
                Test.obj.notify();//唤醒该线程，获得锁                                      
                try {
                	Test.obj.wait();
				} catch (InterruptedException e) {						
		        	e.printStackTrace();
				}
            }             
			}
		}
}
