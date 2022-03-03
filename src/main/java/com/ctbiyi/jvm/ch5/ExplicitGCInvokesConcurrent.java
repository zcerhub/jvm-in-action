package com.ctbiyi.jvm.ch5;

/**
 * -XX:+PrintGCDetails -XX:+ExplicitGCInvokesConcurrent -XX:+UseConcMarkSweepGC
 * -XX:+PrintGCDetails -XX:+ExplicitGCInvokesConcurrent -XX:+UseG1GC
 * 
 * ExplicitGCInvokesConcurrent ��System.gc()ʱ��ʹ�ò�����ʽ ����
 * @author geym
 *
 */
public class ExplicitGCInvokesConcurrent {
    public static void main(String args[]){
        while(true){
            System.gc();
            Thread.yield();
        }
    }
}
