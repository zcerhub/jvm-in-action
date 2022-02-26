package com.ctbiyi.jvm.ch10;

public class StaticA {
	static{
		try {
			Thread.sleep(1000);			
		} catch (InterruptedException e) {
		}
		try {
			Class.forName("com.ctbiyi.jvm.ch10.StaticB");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("StaticA init OK");
	}
}
