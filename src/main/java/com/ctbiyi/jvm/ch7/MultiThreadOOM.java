package com.ctbiyi.jvm.ch7;

public class MultiThreadOOM {
    public static class SleepThread implements Runnable{
        public void run(){
            try {
                Thread.sleep(1000000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String args[]) throws InterruptedException {
        for(int i=0;i<1500000000;i++){
            new Thread(new SleepThread(),"Thread"+i).start();
            System.out.println("Thread"+i+" created");
            Thread.sleep(1000000000);
        }
    }
}
