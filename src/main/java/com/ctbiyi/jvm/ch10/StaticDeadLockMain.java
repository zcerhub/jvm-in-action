package com.ctbiyi.jvm.ch10;

public class StaticDeadLockMain extends Thread{
	private char flag;
	public StaticDeadLockMain(char flag){
		this.flag=flag;
		this.setName("Thread"+flag);
	}
	@Override
	public void run(){
		try {
			Class.forName("com.ctbiyi.jvm.ch10.Static"+flag);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(getName()+" over");
	}
	public static void main(String[] args) throws InterruptedException {
		StaticDeadLockMain loadA=new StaticDeadLockMain('A');
		loadA.start();
		StaticDeadLockMain loadB=new StaticDeadLockMain('A');
		loadB.start();
	}
}
