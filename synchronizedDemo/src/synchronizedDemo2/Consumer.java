package synchronizedDemo2;

public class Consumer implements Runnable {
	
	
	@Override
	public void run() {
		int count = 10;
		//System.out.println(count);
		while(count>0) {						
				long start = System.currentTimeMillis();//��ʼʱ�� 
				System.out.println("Consumer");
                count--;
                Test.obj.notify();//���Ѹ��̣߳������                          
                long  end= System.currentTimeMillis();//����ʱ��
                System.out.println(end - start);
                if (end - start < 1000) //����ˢ��ʱ��10MS
                {   
                	synchronized (Test.obj) { 
	                	try {
	                		//ֱ�������̵߳��ô˶���� notify() ������ notifyAll() ����������ǰ�̱߳�����(���롰����״̬��)
	                		Test.obj.wait(1000-(end - start)); //�ȴ�ʱ�䵽�����û��ִ���̵߳�notify����notifyAll�������ִ�б��߳�
						} catch (InterruptedException e) {
							
							e.printStackTrace();
						}
	                }
                }
               
			
		}

	}
}

