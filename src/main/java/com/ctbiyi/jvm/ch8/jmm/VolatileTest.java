package com.ctbiyi.jvm.ch8.jmm;
/**
 * -server
 * @author Administrator
 *
 */
public class VolatileTest {

	private static volatile boolean stop = false;  	//确保stop变量在多线程中可见

	public static class MyThread extends Thread{

//		public  void stopMe(){				//在其他线程中调用，停止本线程
//			stop=true;
//		}
//		public  boolean stopped(){
//			return stop;
//		}
		public void run() {  
			int i = 0;  
			while (!stop) { 					//在其他线程中改变stop的值
				i++;  
			}
			System.out.println("Stop Thread");  
		}  
	}

	public static void main(String[] args) throws InterruptedException {
		MyThread t = new MyThread();  
		t.start(); 
		Thread.sleep(1000);
//		t.stopMe();
		stop=true;
		Thread.sleep(1000);

	}
}

