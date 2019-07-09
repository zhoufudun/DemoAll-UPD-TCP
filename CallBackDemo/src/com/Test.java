package com;

import com.CilentHandler.Student;

public class Test {
	public static void main(String[] args) {	
		System.out.println(Test.Handler.pri());
	}
	static class Handler{
		
		static CilentHandler cilentHandler=new CilentHandler(new CilentHandler.Callback() {
			
			@Override
			public Student getS(CilentHandler cilentHandler) {
				 return cilentHandler.getStudent();
				
			}
			
		});	
		public static Student pri() {
			return cilentHandler.getStudent();
		}
		
	}
	
}
