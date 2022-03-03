package com.ctbiyi.jvm.ch5;

/**
 * 
 * �����ٶ�
 * -XX:-UseTLAB  -Xcomp -XX:-BackgroundCompilation -XX:-DoEscapeAnalysis -server
 * 
 * -XX:+UseTLAB  -Xcomp -XX:-BackgroundCompilation -XX:-DoEscapeAnalysis -server
 * 
 * ������־
 * -XX:+UseTLAB  -XX:+PrintTLAB -XX:+PrintGC -XX:TLABSize=307200 -XX:-ResizeTLAB
 * 
 * ʹ�� XX:TLABRefillWasteFraction �ı�refill waste
 * -XX:+UseTLAB  -XX:+PrintTLAB -XX:+PrintGC -XX:TLABSize=102400 -XX:-ResizeTLAB -XX:TLABRefillWasteFraction=100 -XX:-DoEscapeAnalysis -server
 * @author geym
 *
 */
public class UseTLAB {
    public static void alloc(){
        byte[] b=new byte[2];
        b[0]=1;
        
    }
    public static void main(String args[]){
        long b=System.currentTimeMillis();
        for(int i=0;i<10000000;i++){
            alloc();
        }
        long e=System.currentTimeMillis();
        System.out.println(e-b);
    }
}
