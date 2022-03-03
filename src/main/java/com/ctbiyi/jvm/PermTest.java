package com.ctbiyi.jvm;

import com.ctbiyi.jvm.ch7.oom.DirectBufferOOM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * JDK1.6 1.7 -XX:+PrintGCDetails -XX:PermSize=5M -XX:MaxPermSize=5m
 * 
 * JDK1.8 -XX:+PrintGCDetails -XX:MaxMetaspaceSize=5M
 * 
 * @author Geym
 *
 */
public class PermTest {

	static List<Object> list = new ArrayList<Object>();

	public static void main(String[] args) throws Exception {
		int i = 0;
		try {
			for (i = 0; i < 100000000000000L; i++) {
				DirectBufferOOM.CglibBean bean = new DirectBufferOOM.CglibBean("com.ctbiyi.jvm" + i, new HashMap());
//				list.add(bean);
			}
		} catch (Exception e) {
			System.out.println("total create count:" + i);
			throw e;
		}
	}
}
