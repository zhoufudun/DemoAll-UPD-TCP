package com;

import java.util.ArrayList;
import java.util.List;

public class DemoTwo<T>{
	public static void main(String[] args) {
		DemoTwo<String> demo = new DemoTwo<String>();

        //获取string类型
        List<String> array = new ArrayList<String>();
        array.add("test");
        array.add("doub");
        String str = demo.getListFirst(array);
        System.out.println(str);
        //
        
//        //获取nums类型
//        List<Integer> nums = new ArrayList<Integer>();
//        nums.add(12);
//        nums.add(13);
//        Integer num = demo.getListFirst(nums);
//        System.out.println(num);

	}
	/**
	 * 这个只能传递T类型的数据
     * 返回值 就是Demo<T> 实例化传递的对象类型
	 * @param data
	 * @return
	 */
	private T getListFirst(List<T> data) {
		if (data == null || data.size() == 0) {
			return null;
	    }
		return data.get(0);
	}
	
}
