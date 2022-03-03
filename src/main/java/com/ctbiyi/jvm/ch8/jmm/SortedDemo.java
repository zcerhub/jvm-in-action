package com.ctbiyi.jvm.ch8.jmm;

public class SortedDemo {

    private int a=0;
    private boolean flag=false;

    public synchronized void write() {
        a=1;
        flag=true;
    }

    public synchronized void read() {
        if (flag) {
            int i=a+1;
        }
    }


}
