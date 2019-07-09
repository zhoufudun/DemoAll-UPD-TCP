package com;

public class TestMain {
	public static void main(String[] args) {
		F<Integer> f1=new F<Integer>(100);
		f1.showTyep();
		System.out.println(f1.getObj());
		
		F<Integer> f2=new F<Integer>();
		f2.setObj(200);
		f2.showTyep();
		System.out.println(f2.getObj());
		
	}
}
