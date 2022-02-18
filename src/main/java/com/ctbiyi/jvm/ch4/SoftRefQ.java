package com.ctbiyi.jvm.ch4;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

/**
 * -Xmx10m
 * @author geym
 *
 */
public class SoftRefQ {
    public static class User{
        public User(int id,String name){
            this.id=id;
            this.name=name;
        }
        public int id;
        public String name;
        
        @Override
        public String toString(){
            return "[id="+String.valueOf(id)+",name="+name+"]";
        }
    }
    
    static ReferenceQueue<byte[]> softQueue=null;
    public static class CheckRefQueue extends Thread{
        @Override
        public void run(){
            while(true){
                if(softQueue!=null){
                    UserSoftReference obj=null;
                            try {
                                obj = (UserSoftReference) softQueue.remove();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(obj!=null) {
                                System.err.println(Thread.currentThread()+":"+obj );
                            }
                }
            }
        }
    }
    
    public static class UserSoftReference extends SoftReference<byte[]>{
        public UserSoftReference(byte[] referent, ReferenceQueue<? super byte[]> q) {
            super(referent, q);
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        Thread t=new CheckRefQueue();
        t.setDaemon(true);
        t.start();
        byte[] a=new byte[1024*925*7];
        softQueue = new ReferenceQueue();
        UserSoftReference userSoftRef = new UserSoftReference(a,softQueue);
        a=null;
        System.out.println(Thread.currentThread()+":"+userSoftRef.get());
        System.gc();
        //内存足够，不会被回收
        System.out.println("After GC:");
        System.out.println(Thread.currentThread()+":"+userSoftRef.get());
        
        System.out.println("try to create byte array and GC");
        byte[] b=new byte[1024*925*7];
        System.gc();
        System.out.println(Thread.currentThread()+":"+userSoftRef.get());

        Thread.sleep(20000);
    }
}
