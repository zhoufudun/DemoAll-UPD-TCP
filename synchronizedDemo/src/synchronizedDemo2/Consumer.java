package synchronizedDemo2;

public class Consumer implements Runnable {
	
	
	@Override
	public void run() {
		int count = 10;
		//System.out.println(count);
		while(count>0) {						
				long start = System.currentTimeMillis();//开始时间 
				System.out.println("Consumer");
                count--;
                Test.obj.notify();//唤醒该线程，获得锁                          
                long  end= System.currentTimeMillis();//结束时间
                System.out.println(end - start);
                if (end - start < 1000) //设置刷新时间10MS
                {   
                	synchronized (Test.obj) { 
	                	try {
	                		//直到其他线程调用此对象的 notify() 方法或 notifyAll() 方法”，当前线程被唤醒(进入“就绪状态”)
	                		Test.obj.wait(1000-(end - start)); //等待时间到达后若没有执行线程的notify或者notifyAll，则继续执行本线程
						} catch (InterruptedException e) {
							
							e.printStackTrace();
						}
	                }
                }
               
			
		}

	}
}

