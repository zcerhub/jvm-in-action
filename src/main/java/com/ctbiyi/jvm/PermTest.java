package com.ctbiyi.jvm;

import java.util.HashMap;

/**
 * JDK1.6 1.7 -XX:+PrintGCDetails -XX:PermSize=5M -XX:MaxPermSize=5m
 * 
 * JDK1.8 -XX:+PrintGCDetails -XX:MaxMetaspaceSize=5M
 * 
 * @author Geym
 *
 */
public class PermTest {
	public static void main(String[] args) throws Exception {
		int i = 0;
		try {
			for (i = 0; i < 100000000; i++) {
				CglibBean bean = new  CglibBean("com.ctbiyi.jvm" + i, new HashMap());
			}
		} catch (Exception e) {
			System.out.println("total create count:" + i);
			throw e;
		}
	}
}
