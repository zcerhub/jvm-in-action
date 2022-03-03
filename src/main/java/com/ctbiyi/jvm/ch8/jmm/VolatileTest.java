package com.ctbiyi.jvm.ch8.jmm;
/**
 * -server
 * @author Administrator
 *
 */
public class VolatileTest {

	private static volatile boolean stop = false;  	//ȷ��stop�����ڶ��߳��пɼ�

	public static class MyThread extends Thread{

//		public  void stopMe(){				//�������߳��е��ã�ֹͣ���߳�
//			stop=true;
//		}
//		public  boolean stopped(){
//			return stop;
//		}
		public void run() {  
			int i = 0;  
			while (!stop) { 					//�������߳��иı�stop��ֵ
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

