package com.ctbiyi.jvm.ch4;

import java.lang.ref.SoftReference;

/**
 * -Xmx10m
 * After gc, soft ref is exists
 * after create byte array ,soft ref is gc
 * 
 * @author geym
 *
 */
public class SoftRef {
    public static void main(String[] args) {
        byte[] barr=new byte[1024*925*7];
        SoftReference<byte[]> userSoftRef = new SoftReference(barr);
        barr=null;
        
        System.out.println(userSoftRef.get());
        System.gc();
        System.out.println("After GC:");
        System.out.println(userSoftRef.get());
        
        byte[] b=new byte[1024*925*7];
        System.gc();
        System.out.println(userSoftRef.get());
    }
}
