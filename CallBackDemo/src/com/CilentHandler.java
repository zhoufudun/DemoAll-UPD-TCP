package com;

public class CilentHandler {
	private Callback callback;
    public CilentHandler(Callback callback) {
		super();
		this.callback = callback;
	}
    //��Ϊ�ӿ��ǲ���ʵ�����ģ��ڲ��ӿ�ֻ�е����Ǿ�̬�Ĳ������塣
    //��ˣ�Ĭ������£��ڲ��ӿ��Ǿ�̬�ģ��������Ƿ��ֶ�����static�ؼ��֡�
	static interface Callback{
    	public Student getS(CilentHandler cilentHandler);   	
    }
	
	public Student getStudent() {
		return new Student("С��") {
			public String toString(){
                return "С��+С��";
            }
		};
	}
	class Student{
		private String name;

		
		public Student(String name) {
			super();
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}	
	}
				
}