package com.ctbiyi.jvm.ch5;

import java.util.HashMap;
import java.util.Map;

/**
 * ����������С�Ķ���ֱ�ӽ���
 * -Xmx32m -Xms32m -XX:+UseSerialGC -XX:+PrintGCDetails -XX:PretenureSizeThreshold=1000
 * �����ֹTLAB������С������Ȼ����eden��
 * -Xmx32m -Xms32m -XX:+UseSerialGC -XX:+PrintGCDetails -XX:PretenureSizeThreshold=1000 -XX:-UseTLAB
 * @author geym
 *
 */
public class PretenureSizeThreshold {
    public static final int _1K=1024;
    public static void main(String args[]){
        Map<Integer,byte[]> map=new HashMap<Integer,byte[]>();
        for(int i=0;i<5*_1K;i++){
            byte[] b=new byte[_1K];
            map.put(i, b);
        }
    }
}
