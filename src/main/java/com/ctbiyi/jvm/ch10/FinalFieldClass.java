package com.ctbiyi.jvm.ch10;

public class FinalFieldClass {

    public static  String constString = "CONST";
    public static  final String aconstString = "CONST";

    static{
        System.out.println("FinalFieldClass init");
    }

}
