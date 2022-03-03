package com.ctbiyi.jvm.ch10.brkparent;

/**
 * ��ʹapp classloader���Լ��أ�����Ҳ������أ���ΪOrderClassLoader�ƻ���˫��ģʽ
 * @author Administrator
 *
 */
public class ClassLoaderTest {
    public static void main(String[] args) throws ClassNotFoundException {
        OrderClassLoader myLoader=new OrderClassLoader("D:/tmp/clz/");
        Class clz=myLoader.loadClass("geym.zbase.ch10.brkparent.DemoA");
        System.out.println(clz.getClassLoader());
        
        System.out.println("==== Class Loader Tree ====");
        ClassLoader cl=clz.getClassLoader();
        while(cl!=null){
            System.out.println(cl);
            cl=cl.getParent();
        }
    }
}
