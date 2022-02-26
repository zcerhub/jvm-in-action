package com.ctbiyi.jvm.ch2.localvar;

import java.io.File;
import java.io.IOException;

public class LocalVarGC {
	public void localvarGc1(){
		byte[] a=new byte[6*1024*1024];
		System.gc();
	}
	public void localvarGc2(){
		byte[] a=new byte[6*1024*1024];
		a=null;
		System.gc();
	}
	public void localvarGc3(){
		{
		byte[] a=new byte[6*1024*1024];
		}
		System.gc();
	}
	public void localvarGc4(){
		{
		byte[] a=new byte[6*1024*1024];
		}
		int c=10;
		System.gc();
	}
	public void localvarGc5(){
		localvarGc1();
		System.gc();
	}
	public static void main(String[] args) {
		LocalVarGC ins=new LocalVarGC();
//		ins.localvarGc1();
//		ins.localvarGc2();
//		ins.localvarGc3();
//		ins.localvarGc4();
		ins.localvarGc5();
	}

	public void exception(){
		try {
			throw new IOException();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
