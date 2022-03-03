package com.ctbiyi.jvm.ch7.oom;




import java.util.HashMap;

/**
 * @author Geym
 *
 */
public class MetaspaceOOM {
    public static void main(String[] args) {
    	try{
        for(int i=0;i<100000;i++){
            DirectBufferOOM.CglibBean bean = new DirectBufferOOM.CglibBean("geym.jvm.ch3.perm.bean"+i,new HashMap());
        }
    	}catch(Error e){
    		e.printStackTrace();
    	}
    } 
}
