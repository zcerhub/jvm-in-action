package com.ctbiyi.jvm.ch3.heap;

/**
 * -Xmx20m -Xms5m -XX:+PrintCommandLineFlags -XX:+PrintGCDetails
 * -XX:+UseSerialGC
 * 
 * @author Geym
 *
 */
public class HeapAlloc {
    public static void main(String[] args) {
        System.out.print("maxMemory=");
        System.out.println(Runtime.getRuntime().maxMemory() + " bytes");
        System.out.print("free mem=");
        System.out.println(Runtime.getRuntime().freeMemory() + " bytes");
        System.out.print("total mem=");
        System.out.println(Runtime.getRuntime().totalMemory() + " bytes");

        byte[] b = new byte[1 * 1024 * 1024];
        System.out.println("������1M�ռ������");

        System.out.print("maxMemory=");
        System.out.println(Runtime.getRuntime().maxMemory() + " bytes");
        System.out.print("free mem=");
        System.out.println(Runtime.getRuntime().freeMemory() + " bytes");
        System.out.print("total mem=");
        System.out.println(Runtime.getRuntime().totalMemory() + " bytes");

        b = new byte[4 * 1024 * 1024];
        System.out.println("������4M�ռ������");

        System.out.print("maxMemory=");
        System.out.println(Runtime.getRuntime().maxMemory() + " bytes");
        System.out.print("free mem=");
        System.out.println(Runtime.getRuntime().freeMemory() + " bytes");
        System.out.print("total mem=");
        System.out.println(Runtime.getRuntime().totalMemory() + " bytes");
    }
}
