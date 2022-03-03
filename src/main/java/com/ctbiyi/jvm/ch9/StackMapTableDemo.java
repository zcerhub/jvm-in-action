package com.ctbiyi.jvm.ch9;

public class StackMapTableDemo {

	public static void append() {
		int i=0;
		int j=0;
		if (i > 0) {
			i++;
		}
	}


	public static void chop() {
		int i=0;
		int j=0;
		if (i > 0) {
			long k=0;
			if(j==0){
				k++;
			}
			int t=0;
		}
	}

}
