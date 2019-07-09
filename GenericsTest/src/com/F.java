package com;

public class F<T> {
	private T obj;
	public F(){
		
	}
	public F(T o){
		this.obj=o;
	}
	public T getObj() {
		return obj;
	}
	public void setObj(T obj) {
		this.obj = obj;
	}
	public void showTyep() {  
        System.out.println("T的实际类型是: " + obj.getClass().getName());  
    }  
}
