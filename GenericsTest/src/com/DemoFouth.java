package com;

import java.util.ArrayList;
import java.util.List;

public class DemoFouth{
	public static void main(String[] args) {
		DemoFouth demo = new DemoFouth();      
        //获取nums类型
        List<String> num = demo.getList("dd");
        for(String str:num) {
        	 System.out.println(str);
        }      
	}
	/**
	 * 这个<T> List<T> 可以传入任何类型的List
     * 参数T:
     *     第一个 表示是泛型
     *     第二个 表示返回的是T类型的数据
     *     第三个 限制参数类型为T
	 * @param i
	 * @return
	 */
	private <T> List<T> getList(T i){
		List<T> nums = new ArrayList<T>();
		nums.add(i);nums.add(i);nums.add(i);		
		return nums;
	}
}
