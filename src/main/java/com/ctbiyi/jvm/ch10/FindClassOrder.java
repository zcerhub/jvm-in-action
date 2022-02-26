package com.ctbiyi.jvm.ch10;

/**
 * -Xbootclasspath/a:D:/tmp/clz
 * -XBootClasspath 指定 说明加载先从启动classloader开始
 * @author Administrator
 *
 */
public class FindClassOrder {
	public static void main(String args[]){
		HelloLoader loader=new HelloLoader();
		loader.print();
		System.out.println(loader.getClass().getClassLoader());
//		System.out.println(System.getProperty("sun.boot.class.path"));
		System.out.println(System.getProperty("java.ext.dirs"));
//		System.out.println(System.getProperty("java.class.path"));
	}
}
