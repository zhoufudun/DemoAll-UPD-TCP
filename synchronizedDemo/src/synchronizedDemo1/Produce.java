package synchronizedDemo1;

public class Produce implements Runnable {

	@Override
	public void run() {
		int count = 10;
		while(count>0) {
			synchronized (Test.obj) { 
				
				long start = System.currentTimeMillis();//开始时间 
				System.out.println("Produce");
                count--;
                Test.obj.notify();//唤醒该线程，获得锁                        
                long  end= System.currentTimeMillis();//结束时间
                System.out.println(end - start);
                if (end - start < 1000) //设置刷新时间10MS
                {
                	try {
                		Test.obj.wait(1000-(end - start));
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					}
                }
                
			}
		}

	}

}
