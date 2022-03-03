package com.ctbiyi.jvm.ch10.intern;

public class StringIntern {

	public static void main(String[] args) {
		String a=Integer.toString(1)+Integer.toString(2)+Integer.toString(3);
		String e=a.intern();
		String b="123";
		String c="123";
		System.out.println(a.equals(b));
		System.out.println(a==b);
		System.out.println(c==b);
		System.out.println(a.intern()==b);
		System.out.println(a==e);
	}

}
