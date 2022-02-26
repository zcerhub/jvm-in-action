package com.ctbiyi.jvm.ch10;

public class StaticB {
	static{
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		try {
			Class.forName("com.ctbiyi.jvm.ch10.StaticA");
		} catch (ClassNotFoundException e) {
		}
		System.out.println("StaticB init OK");
	}
}
