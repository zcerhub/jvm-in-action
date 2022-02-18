package com.ctbiyi.jvm;

import java.util.Vector;

public class DumpOOM {
    public static void main(String[] args) {
        Vector v=new Vector();
        for(int i=0;i<25;i++) {
            v.add(new byte[1*1024*1024]);
        }
    }
}
