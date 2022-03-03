# Class装载系统

## 来取都有序：看懂Class文件的装载流程

class文件只要被class装载系统装载到jvm内部，对应的Class类型才能被jvm识别。class装载分为三个阶段：加载、连接、初始化。连接又可分为：校验、准备、解析

![1646227790534](D:\code\jvm-in-action\assets\1646227790534.png)

## 类装载的条件

类只有在初始使用时才会被初始化。类的使用方式分为两种：主动使用、被动使用。只有主动使用才会触发类的初始化。

#### 主动使用

- 通过new、反射、克隆和反序列化一个类对象时
- 调用类的静态属性时（Final修饰除外）
- 调用类的静态方法时
- 使用java.lang.reflect包中的方法反射类的方法时
- 初始化子类时，需要先初始化父类
- 包含main方法的启动类

#### demo展示

场景一：

前置说明：

- 静态代码块和静态常量会在类对象创建时初始化，所以可以通过静态代码块中的调用情况说明类对象是否初始化

```
public class Parent {
    static {
        System.out.println("Parent init");
    }

}
```

```
public class Child  extends Parent{
    static {
        System.out.println("Child init");
    }
}
```

```
public class InitMain {

    public static void main(String[] args) {
        Child c=new Child();
        Child d=new Child();
    }

}
```

结果：

```
Parent init
Child init
```

说明：

- 类对象只有在首次使用时才会被创建，虽然调用了两次new，但只有第一次调用new时才会触发类的初始化
- new对象时会触发类对象的初始化
- 创建子类对象时会触发父类对象的创建

场景二：调用父类的静态字段不会触发子类的初始化

jvm参数：-XX:+TraceClassLoading

```
public class Parent {
    static {
        System.out.println("Parent init");
    }

    public static int v=100;
}

```

```
public class Child  extends Parent{
    static {
        System.out.println("Child init");
    }
}
```

```
public class InitMain {


    public static void main(String[] args) {
        System.out.println(Child.v);
    }


}
```

结果：

```
[Loaded com.ctbiyi.jvm.ch10.Parent from file:/D:/code/jvm-in-action/target/classes/]
[Loaded com.ctbiyi.jvm.ch10.Child from file:/D:/code/jvm-in-action/target/classes/]
Parent init
100
```

结论：

- 通过子类调用父类的静态属性不会触发子类的初始化。但是子类仍旧会加载，从类的加载日志可以证明

场景三：常量不会触发类对象初始化

```
public class FinalFieldClass {

    public static final String constString = "CONST";
    static{
        System.out.println("FinalFieldClass init");
    }

}
```

```
public class UseFinalField {


    public static void main(String[] args) {
        System.out.println(FinalFieldClass.constString);
    }
}
```

结果：

```
[Loaded java.lang.Class$MethodArray from C:\Program Files\Java\jdk1.8.0_241\jre\lib\rt.jar]
[Loaded java.net.SocksSocketImpl$3 from C:\Program Files\Java\jdk1.8.0_241\jre\lib\rt.jar]
[Loaded java.lang.Void from C:\Program Files\Java\jdk1.8.0_241\jre\lib\rt.jar]
[Loaded java.net.ProxySelector from C:\Program Files\Java\jdk1.8.0_241\jre\lib\rt.jar]
[Loaded sun.net.spi.DefaultProxySelector from C:\Program Files\Java\jdk1.8.0_241\jre\lib\rt.jar]
CONST
```

类对象没有被加载和初始化

从字节码中可以看到常量在编译时已经被写入，没有任何调用FinalFieldClass的信息

![1646266421538](D:\code\jvm-in-action\assets\1646266421538.png)

![1646270563892](D:\code\jvm-in-action\assets\1646270563892.png)

ldc从常量池中加载常量到操作数栈中

### 加载类

装载类的第一个阶段为加载类。类的加载分为两个过程：

- 根据类全名获得类的二进制字节流

  可以通过类全名从文件系统、jar/war包、网络、甚至数据库中获得类的二进制字节流

- 通过二进制字节流创建类对应的java.lang.Class对象

  在java.lang.Class中保存类的数据信息

demo展示

```
public class ReflectionMain {
	public static void main(String[] args) throws Exception {
		Class clzStr=Class.forName("java.lang.String");
		Method[] ms=clzStr.getDeclaredMethods();
		for(Method m:ms){
			String mod=Modifier.toString(m.getModifiers());
			System.out.print(mod+" "+ m.getName()+" (");
			Class<?>[] ps=m.getParameterTypes();
			if(ps.length==0)System.out.print(')');
			for(int i=0;i<ps.length;i++){
				char end=i==ps.length-1?')':',';
				System.out.print(ps[i].getSimpleName()+end);
			}
			System.out.println();
		}
	}
}

```

结果：

```
public equals (Object)
public toString ()
public hashCode ()
public compareTo (String)
public volatile compareTo (Object)
public indexOf (String,int)
public indexOf (String)
public indexOf (int,int)
public indexOf (int)
static indexOf (char[],int,int,char[],int,int,int)
static indexOf (char[],int,int,String,int)
public static valueOf (int)
public static valueOf (long)
public static valueOf (float)
...
```

可以从类的Class对象中获得类的所有方法

### 验证类

连接阶段的第一个阶段为验证阶段。主要的校验步骤为：格式检查、语义检查、字节码验证、符号引用验证。

![1646270115904](D:\code\jvm-in-action\assets\1646270115904.png)

字节码校验阶段会使用到StackMapTable。在符号引用中没有找到引用的类或者方法时会抛出NoClassDefFoundError、NoSuchMethodError。

#### demo展示

场景一：加载类是没有找到对应的class文件，抛出NoClassDefFoundError

```
public class Parent {

    static{
        System.out.println("Parent init");
    }

    public static int v=100;

}
```



```
public class Child extends Parent {

    static{
        System.out.println("child init");
    }
}

```

```
public class InitMain {


    public static void main(String[] args) {
       Child c=new Child();

    }


}
```

- 同一文件夹下包含Parent、Child、InitMain

  ![1646269133586](D:\code\jvm-in-action\assets\1646269133586.png)

- 编译

  ![1646269159823](D:\code\jvm-in-action\assets\1646269159823.png)

- 删掉Child.class

  ![1646269191102](D:\code\jvm-in-action\assets\1646269191102.png)

- 运行程序

  ```
  java InitMain
  ```

结果：

```
[root@k8s-node-biyi-test-04 validate]# java InitMain
Exception in thread "main" java.lang.NoClassDefFoundError: Child
        at InitMain.main(InitMain.java:5)
Caused by: java.lang.ClassNotFoundException: Child
        at java.net.URLClassLoader$1.run(URLClassLoader.java:360)
        at java.net.URLClassLoader$1.run(URLClassLoader.java:349)
        at java.security.AccessController.doPrivileged(Native Method)
        at java.net.URLClassLoader.findClass(URLClassLoader.java:348)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:430)
        at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:326)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:363)
        ... 1 more
```

场景二：找不到类对应的方法抛出NoSuchMethodError

```
public class Child  {

    public void hello(){
        System.out.println("hello child");
    }

}
```

```
public class InitMain {

    public static void main(String[] args) {
       Child c=new Child();
        c.hello();
    }

}
```

- 编译

  ```
  javac *.java
  ```

- 运行

  ```
   java InitMain
  ```

结果：

```
hello child
```

修改Child类：

```
public class Child  {
}
```

去掉hello()方法

- 编译Child类

  ```
  javac Child.java 
  ```

- 运行

  ```
   java InitMain
  ```

结果：

```
Exception in thread "main" java.lang.NoSuchMethodError: Child.hello()V
        at InitMain.main(InitMain.java:6)
```

### 准备

准备阶段为Class对象分配空间，并初始化变量为默认值，常量值为常量

#### java类型的默认值

| 类型      | 默认值   |
| --------- | -------- |
| int       | 0        |
| long      | 0L       |
| short     | (short)0 |
| char      | \u0000   |
| boolean   | false    |
| reference | null     |
| float     | 0f       |
| double    | 0f       |

#### demo展示

```
public class FinalFieldClass {

    public static final String constString = "CONST";
    
}
```

![1646271040798](D:\code\jvm-in-action\assets\1646271040798.png)

constString为常量

去掉final，修改变量为变量

![1646271207709](D:\code\jvm-in-action\assets\1646271207709.png)

编译器生成了<clinit>方法，在该方法中对类属性constString赋值。

### 解析类

解析过程会将符号引用转换为直接引用

符号引用：字节码指令中涉及的类、方法、属性均为符号引用

#### demo展示

```
public class FinalFieldClass {

    public static String constString = "CONST";


}
```

![1646273558998](D:\code\jvm-in-action\assets\1646273558998.png)

符号引用在编译时就确定，可以通过符号引用明确java代码的语义。但是方法调用时需要直接引用。通过直接引用可以定位到对象在内存上的地址，方法和属性在对象上的的偏移量。

#### 字符串常量的优化

字符串常量对应常量池中的CONSTANT_String类型。类型为CONSTANT_String会被添加到运行时常量池，相同内容的字符串只会保存一份。通过String.intern()方法会查找常量池是否有相同内容的字符串常量，如果有则返回该引用，没有的话将该字符串添加到常量池中后返回常量池中该字符串的引用

#### demo展示

```
public class StringIntern {

	public static void main(String[] args) {
		String a=Integer.toString(1)+Integer.toString(2)+Integer.toString(3);
		String b="123";
		String c="123";
		System.out.println(a.equals(b));
		System.out.println(a==b);
		System.out.println(c==b);
		System.out.println(a.intern()==b);
	}

}
```

结果：

```
true
false
true
true
```

结论：

- c==b返回true，表示相同内容的字符串在常量池中只会保存一份
- a.intern()==b返回true，表示如果常量池中已经存在相同内容的值intern()会返回该引用

场景二：

```
public class StringIntern {

	public static void main(String[] args) {
		String a=Integer.toString(1)+Integer.toString(2)+Integer.toString(3);
		String e=a.intern();
		String b="123";
		System.out.println(a==b);
	}

}
```

结果：

```
true
```

结论：

- 解析CONSTAN_String时如果常量池中已经有相同内容的字符串会将该引用为字符串的直接引用

场景三：CONSTRANT_String

当给变量赋值为字符串常量值时，该字符串常量为CONSTRANT_String

```
	public static void main(String[] args) {
		String b="123";
	}
```

![1646280861969](D:\code\jvm-in-action\assets\1646280861969.png)

![1646280889567](D:\code\jvm-in-action\assets\1646280889567.png)

当通过赋值final修饰的变量时

```
	private static final String f="123";;
	public static void main(String[] args) {
		String c=f;
	}
```

![1646281122039](D:\code\jvm-in-action\assets\1646281122039.png)

![1646281135635](D:\code\jvm-in-action\assets\1646281135635.png)

可见，两种写法在字节码层面是相同的

### 初始化

初始化会调用编译器自动生成的<clinit>方法为类对象的属性赋值。<clinit>方法的内容为静态变量和静态代码块中的内容。如果没有静态代码块或者静态变量则不会生成该方法。

#### demo展示

场景一：

```
public class StaticFinalClass {
	public static final int i=1;
	public static final int j=2;
}
```

结果：

![1646282316109](D:\code\jvm-in-action\assets\1646282316109.png)

结论：没有静态变量和静态代码块不会生成<clinit>方法

场景二：

```
public class SimpleStatic {
	public static int id=1;
	public static int number;
	static{
		number=4;
	}
}
```

![1646282453186](D:\code\jvm-in-action\assets\1646282453186.png)



父子类对象的初始化顺序为先初始化父类对象，再初始化子类对象，则子类中的属性值会覆盖父类中的属性值

#### demo展示

```
public class SimpleStatic {
	public static int id=1;
	public static int number;
	static{
		number=4;
	}
}
```

```

public class ChildStatic extends SimpleStatic{
	static{
		number=2;
	}
	public static void main(String[] args) {
		System.out.println(number);
	}
}

```

结果：

```
2

```



存在的问题：jvm会通过锁确保类对象只会初始化一次，这意味着类的初始化可能会造成死锁

#### demo展示

```
public class StaticA {
	static{
		try {
			Thread.sleep(1000);			
		} catch (InterruptedException e) {
		}
		try {
			Class.forName("com.ctbiyi.jvm.ch10.staticdead.StaticB");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("StaticA init OK");
	}
}

```

```
public class StaticB {
	static{
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		try {
			Class.forName("com.ctbiyi.jvm.ch10.staticdead.StaticA");
		} catch (ClassNotFoundException e) {
		}
		System.out.println("StaticB init OK");
	}
}
```

```
public class StaticDeadLockMain extends Thread{
	private char flag;
	public StaticDeadLockMain(char flag){
		this.flag=flag;
		this.setName("Thread"+flag);
	}
	@Override
	public void run(){
		try {
			Class.forName("com.ctbiyi.jvm.ch10.staticdead.Static"+flag);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(getName()+" over");
	}
	public static void main(String[] args) throws InterruptedException {
		StaticDeadLockMain loadA=new StaticDeadLockMain('A');
		loadA.start();
		StaticDeadLockMain loadB=new StaticDeadLockMain('B');
		loadB.start();
	}
}
```

![1646284163428](D:\code\jvm-in-action\assets\1646284163428.png)

结果：程序陷入死锁状态

而且类加载过程中的死锁visual vm中是检测不出来的

![1646284354443](D:\code\jvm-in-action\assets\1646284354443.png)

## 一切Class从这里开始：掌握ClassLoader

类加载的过程由ClassLoader完成，获得二进制字节码流交给连接处理。

### 认识ClassLoader，看懂类加载

classloader的主要接口：

- public Class<?> loadClass(String name) throws ClassNotFoundException

  根据类签名获得类对象。

- protected final Class<?> defineClass(byte[] b,int off,int len)

  从字节数组中加载类对象

- protected Class<?> findClass(String name) throws ClassNotFoundException

  根据指定类签名获得二进制字节流，利用defineClass获得类对象。

- protected final Class<?> findLoadedClass(String name)

  查找已经被加载的类

loadClass为public，提供对外服务。在其内部先通过findLoadedClass查看该类是否已经加载，没有加载的话根据定义好的双亲委派模型加载。双亲没有加载到该类会将实际的加载工作交给findClass，由findclass完成类对象加载

### ClassLoader的分类

classloader主要分为三类，不同的类负载不同的加载模块。

- 启动类加载器：cpp语言编写的类加载器，负责核心类库的加载，如rt.jar。对应的java类型为null
- 扩展类加载器：其双亲为启动类加载器，负载ext包下的类加载。对应的java类为ExtClassLoader
- 应用类加载器：其双亲为扩展类加载器，负载应用程序的加载。对应的java类为AppClassLoader

![1646304960509](D:\code\jvm-in-action\assets\1646304960509.png)

#### demo展示

```
public class PrintClassLoaderTree {
	public static void main(String[] args) {
		ClassLoader cl=PrintClassLoaderTree.class.getClassLoader();
        while(cl!=null){
            System.out.println(cl);
            cl=cl.getParent();
        }
        System.out.println("String classloader:"+String.class.getClassLoader());
	}
}
```

结果：

```
sun.misc.Launcher$AppClassLoader@18b4aac2
sun.misc.Launcher$ExtClassLoader@7f31245a
String classloader:null
```

结论：

- String是核心类由启动类加载器加载

### ClassLoader的双亲委托模式



