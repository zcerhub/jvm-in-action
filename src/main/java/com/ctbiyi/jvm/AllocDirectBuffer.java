package com.ctbiyi.jvm;

import java.nio.ByteBuffer;

public class AllocDirectBuffer {
    public void directAllocate(){
        long starttime=System.currentTimeMillis();
        for(int i=0;i<120000022;i++){
            ByteBuffer b=ByteBuffer.allocateDirect(1024*1024);
//            System.gc();
        }
        long endtime=System.currentTimeMillis();
        System.out.println("directAllocate:"+(endtime-starttime));
    }

    public void bufferAllocate() {
        long starttime=System.currentTimeMillis();
        for(int i=0;i<200000000;i++){
            ByteBuffer b=ByteBuffer.allocate(100000000);
        }
        long endtime=System.currentTimeMillis();
        System.out.println("bufferAllocate:"+(endtime-starttime));
    }

    public static void main(String[] args) {
        AllocDirectBuffer alloc=new AllocDirectBuffer();
//        alloc.bufferAllocate();
//        alloc.directAllocate();

        for(int i=0;i<120000022;i++){
            ByteBuffer b=ByteBuffer.allocateDirect(1024*1024);
//            System.gc();
        }
//        alloc.bufferAllocate();
//        alloc.directAllocate();
    }
}