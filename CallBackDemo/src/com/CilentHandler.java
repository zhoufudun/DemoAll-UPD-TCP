package com;

public class CilentHandler {
	private Callback callback;
    public CilentHandler(Callback callback) {
		super();
		this.callback = callback;
	}
    //因为接口是不能实例化的，内部接口只有当它是静态的才有意义。
    //因此，默认情况下，内部接口是静态的，不管你是否手动加了static关键字。
	static interface Callback{
    	public Student getS(CilentHandler cilentHandler);   	
    }
	
	public Student getStudent() {
		return new Student("小明") {
			public String toString(){
                return "小明+小明";
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
