package com.ctbiyi.jvm.ch8;
/**
 * -server
 * @author Administrator
 *
 */
public class VolatileTest {
	public static class MyThread extends Thread{
//		private volatile boolean stop = false;  	//ȷ��stop�����ڶ��߳��пɼ�
		private boolean stop = false;  	//ȷ��stop�����ڶ��߳��пɼ�
		public  void stopMe(){				//�������߳��е��ã�ֹͣ���߳�
			stop=true;
		}
		public  boolean stopped(){
			return stop;
		}
		public void run() {  
			int i = 0;  
			while (!stopped()) { 					//�������߳��иı�stop��ֵ
				i++;  
			}
			System.out.println("Stop Thread");  
		}  
	}

	public static void main(String[] args) throws InterruptedException {
		MyThread t = new MyThread();  
		t.start(); 
		Thread.sleep(1000);
		t.stopMe();
//		Thread.sleep(1000);

	}
}

