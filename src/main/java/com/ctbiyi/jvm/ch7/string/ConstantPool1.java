package com.ctbiyi.jvm.ch7.string;

public class ConstantPool1 {
    public static void main(String[] args) {
        String s1 = new String("hello"),s2= new String("hello");
        System.out.println(s1 == s2);
        System.out.println(s1 == s2.intern());
        System.out.println("hello"== s2.intern());
        System.out.println(s1.intern()== s2.intern());
     }
}
