package com.ctbiyi.jvm.ch10;

/**
 * ���̳߳�ʼ��һ���� ֻ��һ���̻߳����<clinit>
 * �����ȴ����̣߳��ڵ�һ���߳���ɺ󣬲��������<clinit>
 * @author Administrator
 *
 */
public class MultiEntryClinitMain extends Thread{
	private char flag;
	public MultiEntryClinitMain(char flag){
		this.flag=flag;
		this.setName("Thread"+flag);
	}
	@Override
	public void run(){
		try {
			Class.forName("geym.zbase.ch10.staticdead.Static"+flag);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(getName()+" over");
	}
	public static void main(String[] args) throws InterruptedException {
		MultiEntryClinitMain loadA=new MultiEntryClinitMain('A');
		loadA.start();
		MultiEntryClinitMain loadB=new MultiEntryClinitMain('A');
		loadB.start();
		
	}

}
