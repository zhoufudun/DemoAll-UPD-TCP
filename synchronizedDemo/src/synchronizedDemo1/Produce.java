package synchronizedDemo1;

public class Produce implements Runnable {

	@Override
	public void run() {
		int count = 10;
		while(count>0) {
			synchronized (Test.obj) { 
				
				long start = System.currentTimeMillis();//��ʼʱ�� 
				System.out.println("Produce");
                count--;
                Test.obj.notify();//���Ѹ��̣߳������                        
                long  end= System.currentTimeMillis();//����ʱ��
                System.out.println(end - start);
                if (end - start < 1000) //����ˢ��ʱ��10MS
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
