package com.ctbiyi.jvm.ch10.findorder;

/**
 * -Xbootclasspath/a:D:/tmp/clz
 * -XBootClasspath ָ�� ˵�������ȴ�����classloader��ʼ
 * @author Administrator
 *
 */
public class FindClassOrder {
	public static void main(String args[]){
		HelloLoader loader=new HelloLoader();
		loader.print();
	}
}
