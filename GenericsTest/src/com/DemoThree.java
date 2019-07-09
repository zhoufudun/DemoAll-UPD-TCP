package com;

import java.util.ArrayList;
import java.util.List;

public class DemoThree<T>{
	public static void main(String[] args) {
		DemoThree<String> demo = new DemoThree<String>();      
        //获取nums类型
        List<String> num = demo.getList("dd");
        for(String str:num) {
        	 System.out.println(str);
        }      
	}
	/**
	 * 返回的List中的类型是DemoThree实例化传入的类型
	 * @param i
	 * @return
	 */
	private List<T> getList(T i){
		List<T> nums = new ArrayList<T>();
		nums.add(i);nums.add(i);nums.add(i);		
		return nums;
	}
}
