package com.ctbiyi.jvm.ch4;

import java.lang.ref.WeakReference;

/**
 * ���ܵ�ǰ�ڴ�ռ��㹻��񣬶�����������ڴ�
 * @author geym
 *
 */
public class WeakRef {
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
    public static void main(String[] args) {
        User u=new User(1,"geym");
        WeakReference<User> userWeakRef = new WeakReference<User>(u);
        u=null;
        System.out.println(userWeakRef.get());
        System.gc();
        //���ܵ�ǰ�ڴ�ռ��㹻��񣬶�����������ڴ�
        System.out.println("After GC:");
        System.out.println(userWeakRef.get());
    }
}
