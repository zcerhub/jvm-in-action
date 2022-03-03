package com.ctbiyi.jvm.ch10.findorder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

/**
 * -Xbootclasspath/a:D:/tmp/clz
 * -XBootClasspath 指定 说明判断类是否存在，从app classloader开始
 * @author Administrator
 *
 */
public class FindClassOrder2 {
	public static void main(String args[]) throws Exception {		
		ClassLoader cl=FindClassOrder2.class.getClassLoader();
		byte[] bHelloLoader=loadClassBytes("HelloLoader.class");
		Method md_defineClass=ClassLoader.class.getDeclaredMethod("defineClass", byte[].class,int.class,int.class);
		md_defineClass.setAccessible(true);
		md_defineClass.invoke(cl, bHelloLoader,0,bHelloLoader.length);
		md_defineClass.setAccessible(false);
		
		HelloLoader loader = new HelloLoader();
		loader.print();
		Class<? extends HelloLoader> cls = loader.getClass();
		System.out.println(cls.getClassLoader());
		System.out.println(cls.getTypeName());

	}



	private static byte[] loadClassBytes(String className) throws ClassNotFoundException {
		try {
			URL aa = FindClassOrder2.class.getResource(className);
			FileInputStream fis = new FileInputStream(aa.getFile());
			FileChannel fileC = fis.getChannel();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			WritableByteChannel outC = Channels.newChannel(baos);
			ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
			while (true) {
				int i = fileC.read(buffer);
				if (i == 0 || i == -1) {
					break;
				}
				buffer.flip();
				outC.write(buffer);
				buffer.clear();
			}
			fis.close();
			return baos.toByteArray();
		} catch (IOException fnfe) {
			throw new ClassNotFoundException(className);
		}
	}
}
