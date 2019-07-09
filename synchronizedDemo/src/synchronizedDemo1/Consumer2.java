package synchronizedDemo1;

public class Consumer2 implements Runnable {
	
	
	@Override
	public void run() {
		int count = 10;
		//System.out.println(count);
		while(count>0) {
			synchronized (Test.obj) { 								
				System.out.println("Consumer");
                count--;
                Test.obj.notify();//���Ѹ��̣߳������                                        
                try {
                		Test.obj.wait();
					} catch (InterruptedException e) {						
						e.printStackTrace();
					}
                }
               
			}
		}
}

