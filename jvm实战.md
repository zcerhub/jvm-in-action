# 第二章 

## java虚拟机结构

![1645659400647](D:\code\jvm-in-action\assets\1645664157174.png)

- 类加载子系统：负责从文件系统或者网络中加载Class信息，加载的类型存放在方法区中

- 方法区：存放加载的类信息以及运行时常量池信息
  - 运行时常量池：类或者接口常量池的运行时的形式，包括常量池中的符号引用及其解析后的引用

- java堆在虚拟机启动的时候建立，对所有线程共享
- 直接内存：位于堆外，直接向系统申请的内存，大小不受Xmx的限制。其不像堆那样需要从内存拷贝数据，访问速度较快，在读写频繁的场合会使用
- 垃圾回收系统可以对方法区、java堆、直接内存区域进行回收。java堆是其工作重点
- java栈为线程私有，在线程创建时创建，销毁而销毁。保存的基本单元为栈帧，用于方法的调用，栈帧中保存着方法的基本信息
  - 栈帧组成：局部变量表、操作数栈

- 本地方法栈：用于本地方法的调用
- pc寄存器：线程私有。pc寄存器指向正在执行的指令
- 执行引擎：负责执行虚拟机的字节码

### 对象去哪儿里

字节码经过加载子系统加载后保存到方法区，java堆保存程序执行过程中产生的新的对象，可分为新生代、老年代。新生代又可划分为eden、s0、s1，s0和s1同时作为from区和to区。刚创建出来的对象保存在eden中，每次执行一次gc，对象的年龄加1，年龄低于晋升到老年代的阈值h会一直在from区。年龄达到阈值会保存到老年代

#### 堆结构图

![1645666172230](D:\code\jvm-in-action\assets\1645666172230.png)

demo展示：

```
public class SimpleHeap {
    private int id;
    public SimpleHeap(int id){
        this.id=id;
    }
    public void show(){
        System.out.println("My ID is "+id);
    }
    public static void main(String[] args) {
        SimpleHeap s1=new SimpleHeap(1);
        SimpleHeap s2=new SimpleHeap(2);
        s1.show();
        s2.show();
    }
}
```

不同对象在jvm内存中的分布图：

![1645666421480](D:\code\jvm-in-action\assets\1645666421480.png)

#### 函数如何调用：出入Java栈

每次函数调用对应java栈上栈帧的入栈，函数调用完毕对应着栈帧的出栈。函数的调用和栈帧的结构如图：

![1645700257212](D:\code\jvm-in-action\assets\1645700951668.png)

存在的问题：

- 如果方法调用一直调用下去则栈帧需要一直创建（无结束的递归函数），直到出现StackOverflowError
- 不同的方法局部变量的类型、个数不同导致其占用的栈帧大小也不相同。但是每个线程java栈空间是固定的，所以调用的方法不同对应出现StackOverflowError时栈的深度不同

#### demo展示

jvm参数：

- -Xss：每个线程栈的大小

设置jvm参数：  -Xss1m

```
/**
 *      -Xss1m
 * @author Administrator
 *
 */
public class TestStackDeep {
	private static int count=0;
	public static void recursion(long a,long b,long c){
		long e=1,f=2,g=3,h=4,i=5,k=6,q=7,x=8,y=9,z=10;
		count++;
		recursion(a,b,c);
	}
	public static void recursion(){
		count++;
		recursion();
	}
	public static void main(String args[]){
		try{
//			recursion(0L,0L,0L);
			recursion();
		}catch(Throwable e){
			System.out.println("deep of calling = "+count);
			e.printStackTrace();
		}
	}
}
```

场景1：启动recursion(0L,0L,0L)方法

```
deep of calling = 11496
java.lang.StackOverflowError
	at com.ctbiyi.jvm.ch2.xss.TestStackDeep.recursion(TestStackDeep.java:13)
	at com.ctbiyi.jvm.ch2.xss.TestStackDeep.recursion(TestStackDeep.java:13)
	at com.ctbiyi.jvm.ch2.xss.TestStackDeep.recursion(TestStackDeep.java:13)
	at com.ctbiyi.jvm.ch2.xss.TestStackDeep.recursion(TestStackDeep.java:13)
```

场景2：启动recursion()方法

```
deep of calling = 36507
java.lang.StackOverflowError
	at com.ctbiyi.jvm.ch2.xss.TestStackDeep.recursion(TestStackDeep.java:17)
	at com.ctbiyi.jvm.ch2.xss.TestStackDeep.recursion(TestStackDeep.java:17)
	at com.ctbiyi.jvm.ch2.xss.TestStackDeep.recursion(TestStackDeep.java:17)
	at com.ctbiyi.jvm.ch2.xss.TestStackDeep.recursion(TestStackDeep.java:17)
	at com.ctbiyi.jvm.ch2.xss.TestStackDeep.recursion(TestStackDeep.java:17)
	at com.ctbiyi.jvm.ch2.xss.TestStackDeep.recursion(TestStackDeep.java:17)
	at com.ctbiyi.jvm.ch2.xss.TestStackDeep.recursion(TestStackDeep.java:17)
```

调用recursion()出现栈溢出时栈的深度为36507，调用recursion(0L,0L,0L)时出现栈溢出时栈的深度为11496。

#### 局部变量表

局部变量表中保存方法参数、局部变量。



#### demo展示

设置jvm参数：  -Xss1m

```
public class TestStackDeep {
	private static int count=0;
	public static void recursion(long a,long b,long c){
		long e=1,f=2,g=3,h=4,i=5,k=6,q=7,x=8,y=9,z=10;
		count++;
		System.out.println("helllo");
		recursion(a,b,c);
	}

	public void recursion1(){
		count++;
		recursion1();
	}
 

	public static void recursion(){
		count++;
		recursion();
	}
	public static void main(String args[]){
		try{
			recursion(0L,0L,0L);
//			recursion();
		}catch(Throwable e){
			System.out.println("deep of calling = "+count);
			e.printStackTrace();
		}
	}
}
```

说明：

- recursion1()和recursion()的主要区别是recursion1为非static
- 分别调用recursion(0L,0L,0L)和recursion()方法，出现栈溢出时栈的深度不同，主要原因在于两者的局部变量不同（包含方法参数），由此造成局部变量表的空间不同。recursion(0L,0L,0L)方法中的赋值语句也增加了栈帧数据区的空间。最终导致recursion(0L,0L,0L)栈溢出时的深度小于recursion()方法。

#### 字节码层面分析

##### recursion(long a,long b,long c)

![1645702566745](D:\code\jvm-in-action\assets\1645702566745.png)

##### recursion()

![1645702655122](D:\code\jvm-in-action\assets\1645702655122.png)

从jclasslib的显示结果来看，recursion(long a,long b,long c)的操作数栈的最大深度、局部变量最大操作数、字节码长度都大于recursion()。可以验证代码的执行结果。

问题1：为什么recursion(long a,long b,long c)局部最大槽位数为26？

该方法中总共13个long变量，每个变量占用2个槽位，每个槽位代表一个字。所以局部变量表最大槽位数为26

问题2：为什么recursion1()局部最大操作数为1？

![1645703303061](D:\code\jvm-in-action\assets\1645703501926.png)

实例方法有一个默认的局部变量this，所以recursion1的局部变量最大槽位数为1

#### LocalVariableTable

起始PC、长度、序号、名字、描述符分别表示：局部变量的作用范围为[start_pc,start_pc+length)，名字为局部变量名，描述符为局部变量数据类型（J表示long类型），序号为变量在局部变量表中的索引。

##### recursion(long a,long b,long c)

![1645703885129](D:\code\jvm-in-action\assets\1645703885129.png)

recursion(long a,long b,long c)方法的参数a、b、c和局部变量e、f、q、h、i、k、q、x、y、z都保存在局部变量表中，并且方法参数排在局部变量前面。

##### recursion()

![1645704086722](D:\code\jvm-in-action\assets\1645704086722.png)

可以看出局部变量表中的0位置上保存着this

#### 局部变量表槽位复用

局部变量保存在局部变量表中，具有一定的作用域，超过作用域后期空间可以被其余的变量复用

```
public class LocalVar {
	public void localvar1(){
		int a=0;
		System.out.println(a);
		int b=0;
	}
	public void localvar2(){
		{
		int a=0;
		System.out.println(a);
		}
		int b=0;
	}
	public static void main(String[] args) {
		
	}

}
```

说明：

- localvar1中a和b的生命周期在整个方法中都有效，所以局部变量表的槽位无法被复用
- localvar2中a超出方法块后失效，b可以复用a的槽位

##### localvar1的LocalVariableTable

![1645705654388](D:\code\jvm-in-action\assets\1645705654388.png)

![1645705544472](D:\code\jvm-in-action\assets\1645705544472.png)

##### localvar2的LocalVariableTable

![1645705670819](D:\code\jvm-in-action\assets\1645705670819.png)

![1645705599316](D:\code\jvm-in-action\assets\1645705599316.png)



总结：

- localvar1中局部变量最大槽位数为3。this、a、b在索引0、1、2处，方法结束时a、b都有效。
- localvar2中局部变量最大槽位数为2，a和b共用索引1处的槽位

##### 局部变量槽位复用对GC的影响

GC时常以局部变量作为GC root进行引用分析。局部变量失效后其槽位被其它局部变量复用，导致以其为GC root的对象可能可能会被回收。

#### demo展示

添加的jvm参数：-XX:+PrintGC

```

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
		ins.localvarGc1();
	}

}
```

localvarGc1执行结果：

```
[GC (System.gc())  14008K->6976K(500736K), 0.0043318 secs]
[Full GC (System.gc())  6976K->6784K(500736K), 0.0056133 secs]
```

localvarGc2执行结果：

```
[GC (System.gc())  14008K->784K(500736K), 0.0009701 secs]
[Full GC (System.gc())  784K->621K(500736K), 0.0041817 secs]
```

localvarGc3执行结果：

```
[GC (System.gc())  14008K->6944K(500736K), 0.0042054 secs]
[Full GC (System.gc())  6944K->6756K(500736K), 0.0061365 secs]
```

localvarGc4执行结果：

```
[GC (System.gc())  14008K->752K(500736K), 0.0010923 secs]
[Full GC (System.gc())  752K->619K(500736K), 0.0057659 secs]
```

localvarGc5执行结果：

```
[GC (System.gc())  14008K->6896K(500736K), 0.0044487 secs]
[Full GC (System.gc())  6896K->6759K(500736K), 0.0052584 secs]
[GC (System.gc())  9381K->6823K(500736K), 0.0003421 secs]
[Full GC (System.gc())  6823K->615K(500736K), 0.0048500 secs]
```

说明：GC表明为young gc，14008K->6896K(500736K)表示年轻代回收前对象占用空间14008K，回收后为6896K，整个堆空间为500736K（包括年轻代和老年代），0.0044487 secs为这次gc的总耗时，Full GC表明该次为full gc。

- localvarGc1中变量a在方法在调用System.gc()时有效，所以该次gc不能回收a对象占用的6m空间
- localvarGc2中变量a在gc执行时为null，其申请的6m空间变为垃圾被gc回收调，young gc结束时只剩784K空间
- localvarGc3中变量a在gc执行时虽然已经失效，但是堆中的数组对象仍旧指向a，gc不能回收掉
- localvarGc4中变量a在局部变量表中的槽位为b复用，在垃圾回收时堆中的6m数组对象没有被局部变量引用，经过gc后空间被回收调
- localvarGc5中调用System.gc()时方法localvarGc1已经执行结束，其对应的栈帧包括变量a已经被销毁，gc可以回收堆中的数组对象。整个堆大小经回收后变成615K，说明数组对象已经被回收

#### 操作数栈

操作数栈用于保存指令执行过程中的中间变量及其结果。大多数的jvm指令都是对栈的操作。

### 帧数据区

帧数据区用于保存常量池指针和异常处理表。jvm的指令中的符号引用需要常量池的配合才能获得实际对象的引用，在帧数据区保存常量池的引用方便访问常量池。

方法调用过程中出现异常查找异常表完成异常处理，所以帧数据区也需要保存异常处理表。如下为一个异常处理表：

![1645709997604](D:\code\jvm-in-action\assets\1645710626517.png)

```
	public void exception(){
		try {
			throw new IOException();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
```

表示为偏移量为0-8之间出现IOException异常，则跳转到8处执行。否则出现异常处理表中没有的异常则会结束函数调用，返回调用函数，并在调用函数抛出相同的异常，查找调用函数的异常处理表处理。

#### 栈上分配

当jvm通过逃逸分析到创建的对象为线程私有对象（只能被当前线程访问到），会将对象打碎后在栈上分配。分配的对象随着栈的销毁而自动销毁，避免了在堆上分配后需要gc参与。由于栈的空间有限，所以在栈上分配的对象不能过大。

##### 逃逸分析说明

通过代码展示说明逃逸分析。

##### 出现逃逸的

u对象在方法外可以访问，出现逃逸

```
    private User u;
    public static void alloc(){
        u=new User();
        u.id=5;
        u.name="geym";
    }
```

##### 没有出现逃逸

u对象既不是通过参数传递进来，也没有赋值给成员变量，也没有返回。并且该对象比较小，jvm会将user中的id和name打散作为局部变量分配空间

```
    public static void alloc(){
        User u=new User();
        u.id=5;
        u.name="geym";
    }
```

##### demo展示

jvm参数设置：-server -Xmx10m -Xms10m -XX:+DoEscapeAnalysis -XX:+PrintGC -XX:-UseTLAB  -XX:+EliminateAllocations

- -server：表示开启服务端模式，只有在服务端模式才开启逃逸分析
- -XX:+DoEscapeAnalysis：开启逃逸分析
- -XX:+PrintGC：打印gc日志
- -XX:-UserTLAB：关闭TLAB
- -XX:+EliminateAllocations：开启标量替换（默认打开），将对象打散分配在栈上
- -Xmx10m -Xms10m：堆内存起始空间和最大空间为10m

```
public class OnStackTest {
    public static class User{
        public int id=0;
        public String name="";
    }

    public static void alloc(){
        User u=new User();
        u.id=5;
        u.name="geym";
    }
    public static void main(String[] args) throws InterruptedException {
        long b=System.currentTimeMillis();
        for(int i=0;i<100000000;i++){
            alloc();
        }
        long e=System.currentTimeMillis();
        System.out.println(e-b);
    }
}

```

结果：

```
10
```

每个User对象大约占用16byte，100000000个user对象大约需要1.6g。如果在堆上分配的话应该需要大量的gc。但是结果中没有出现gc，说明该对象在栈上分配。



场景1：关闭逃逸分析

jvm参数： -server -Xmx10m -Xms10m -XX:-DoEscapeAnalysis -XX:+PrintGC -XX:-UseTLAB  -XX:+EliminateAllocations

```
[GC 2844K->284K(10240K), 0.0010620 secs]
[GC 2844K->284K(10240K), 0.0013140 secs]
[GC 2844K->284K(10240K), 0.0009020 secs]
[GC 2844K->284K(10240K), 0.0010930 secs]
[GC 2844K->284K(10240K), 0.0009580 secs]
[GC 2844K->284K(10240K), 0.0019420 secs]
4155
```

场景2：关闭标量替换

jvm参数： -server -Xmx10m -Xms10m -XX:+DoEscapeAnalysis -XX:+PrintGC -XX:-UseTLAB  -XX:-EliminateAllocations

```
[GC 2864K->304K(10240K), 0.0011930 secs]
[GC 2864K->304K(10240K), 0.0019100 secs]
[GC 2864K->304K(10240K), 0.0016010 secs]
[GC 2864K->304K(10240K), 0.0011110 secs]
[GC 2864K->304K(10240K), 0.0016070 secs]
[GC 2864K->304K(10240K), 0.0012540 secs]
3645
```

关闭标量替换和逃逸分析中的任何一个出现大量gc，说明对象在堆上分配

### 类去哪儿了：识别方法区

方法区中保存类信息，jdk8使用metaspace作为方法区。metaspace使用本地内存，不受xmx的限制。

#### demo展示

jvm参数：-XX:+PrintGCDetails -XX:MaxMetaspaceSize=15M

- -XX:MaxMetaspaceSize=15m设置metaspace最大空间为15m
- -XX:MetaspaceSize=10m设置metaspace初始空间为10m

```
public class MethodAreaTest {
	public static void main(String[] args) throws Exception {
		int i = 0;
		try {
			for (i = 0; i < 100000000; i++) {
				CglibBean bean = new  CglibBean("com.ctbiyi.jvm" + i, new HashMap());
			}
		} catch (Exception e) {
			System.out.println("total create count:" + i);
			throw e;
		}
	}
}
```

结果：

```
[GC (Allocation Failure) [PSYoungGen: 131072K->4051K(152576K)] 131072K->4067K(500736K), 0.0039483 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Metadata GC Threshold) [PSYoungGen: 133736K->5996K(152576K)] 133752K->6020K(500736K), 0.0045701 secs] [Times: user=0.13 sys=0.00, real=0.01 secs] 
[Full GC (Metadata GC Threshold) [PSYoungGen: 5996K->0K(152576K)] [ParOldGen: 24K->5853K(226816K)] 6020K->5853K(379392K), [Metaspace: 15030K->15030K(1060864K)], 0.0194249 secs] [Times: user=0.13 sys=0.00, real=0.02 secs] 
[GC (Last ditch collection) [PSYoungGen: 0K->0K(152576K)] 5853K->5853K(379392K), 0.0010080 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[Full GC (Last ditch collection) [PSYoungGen: 0K->0K(152576K)] [ParOldGen: 5853K->4041K(438784K)] 5853K->4041K(591360K), [Metaspace: 15030K->15030K(1060864K)], 0.0336778 secs] [Times: user=0.05 sys=0.00, real=0.03 secs] 
total create count:2266
Heap
 PSYoungGen      total 152576K, used 3863K [0x0000000755900000, 0x0000000765500000, 0x00000007ff800000)
  eden space 131072K, 2% used [0x0000000755900000,0x0000000755cc5d90,0x000000075d900000)
  from space 21504K, 0% used [0x000000075d900000,0x000000075d900000,0x000000075ee00000)
  to   space 21504K, 0% used [0x0000000764000000,0x0000000764000000,0x0000000765500000)
 ParOldGen       total 438784K, used 4041K [0x0000000601a00000, 0x000000061c680000, 0x0000000755900000)
  object space 438784K, 0% used [0x0000000601a00000,0x0000000601df2458,0x000000061c680000)
 Metaspace       used 15062K, capacity 15274K, committed 15360K, reserved 1060864K
  class space    used 3771K, capacity 3793K, committed 3840K, reserved 1048576K
Exception in thread "main" java.lang.IllegalStateException: Unable to load cache item
	at net.sf.cglib.core.internal.LoadingCache.createEntry(LoadingCache.java:79)
	at net.sf.cglib.core.internal.LoadingCache.get(LoadingCache.java:34)
	at net.sf.cglib.core.AbstractClassGenerator$ClassLoaderData.get(AbstractClassGenerator.java:119)
	at net.sf.cglib.core.AbstractClassGenerator.create(AbstractClassGenerator.java:294)
	at net.sf.cglib.beans.BeanMap$Generator.create(BeanMap.java:127)
	at net.sf.cglib.beans.BeanMap.create(BeanMap.java:59)
	at com.ctbiyi.jvm.ch7.oom.DirectBufferOOM.CglibBean.<init>(CglibBean.java:35)
	at com.ctbiyi.jvm.PermTest.main(PermTest.java:23)
Caused by: java.lang.OutOfMemoryError: Metaspace
	at java.lang.Class.forName0(Native Method)
	at java.lang.Class.forName(Class.java:348)
	at net.sf.cglib.core.ReflectUtils.defineClass(ReflectUtils.java:467)
	at net.sf.cglib.core.AbstractClassGenerator.generate(AbstractClassGenerator.java:339)
	at net.sf.cglib.core.AbstractClassGenerator$ClassLoaderData$3.apply(AbstractClassGenerator.java:96)
	at net.sf.cglib.core.AbstractClassGenerator$ClassLoaderData$3.apply(AbstractClassGenerator.java:94)
	at net.sf.cglib.core.internal.LoadingCache$2.call(LoadingCache.java:54)
	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
	at net.sf.cglib.core.internal.LoadingCache.createEntry(LoadingCache.java:61)
	... 7 more

```

使用cglib创建了大量的类对象，造成metaspace不足而触发Full GC，等到Full GC处理不过来出现OutOfMmemoryError：Metaspace

# 第三章



跟踪垃圾回收-读懂虚拟机日志

#### -XX:UserSerialGC

串行垃圾回收，工作在年轻代

```
[GC (Allocation Failure) [DefNew: 139264K->4019K(156672K), 0.0164134 secs] 139264K->4019K(504832K), 0.0164614 secs] [Times: user=0.02 sys=0.00, real=0.02 secs] 
[Full GC (Metadata GC Threshold) [Tenured: 0K->5853K(348160K), 0.0487124 secs] 125598K->5853K(504832K), [Metaspace: 15027K->15027K(1060864K)], 0.0487951 secs] [Times: user=0.05 sys=0.02, real=0.05 secs] 
```

- DefNew：垃圾收集器名称，单线程，采用标记复制算法，工作在年轻代
- GC：用来区分是minior gc还是full gc，代表为minior gc
- Allocation Failure：引起垃圾回收的原因，本次gc由于年轻代没有空间创建新的对象触发的
- 139264K->4019K(156672K)：gc工作前的年轻代已经139264K的空间，gc后的年轻代占用的空间为4019K
- (156672K)：年轻代的总大小
-  139264K->4019K：gc工作前整个堆被使用的空间为139264K，gc后整个堆被使用的空间4019K
- (504832K)：整个堆的总大小
- 0.0164134 secs]：gc的总时间
- [Times: user=0.02 sys=0.00, real=0.02 secs] ： user=0.02为gc线程消耗的total cpu time， sys=0.00为系统调用和等待时间的时间， real=0.02 secs为程序暂停时间，由于串行垃圾回收只会使用单个线程，所以real time=user time+sys time

#### -XX:+PrintGC

gc时打印日志

```
[GC (Allocation Failure)  139264K->4018K(504832K), 0.0100267 secs]
[GC (Allocation Failure)  143282K->6206K(504832K), 0.0129695 secs]
```

#### -XX:+PrintGCDetails

jvm退出前会打印堆的详细使用情况

```
[GC[DefNew: 280163K->0K(309248K), 0.0098220 secs] 280163K->5711K(996480K), 0.0098830 secs] [Times: user=0.00 sys=0.01, real=0.01 secs] 
[GC[DefNew: 274446K->0K(309248K), 0.0017720 secs] 280157K->5711K(996480K), 0.0018750 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
Heap
 def new generation   total 309312K, used 59775K [0x0000000407000000, 0x000000041bfa0000, 0x00000005568a0000)
  eden space 274944K,  21% used [0x0000000407000000, 0x000000040aa5fd48, 0x0000000417c80000)
  from space 34368K,   0% used [0x0000000417c80000, 0x0000000417c80000, 0x0000000419e10000)
  to   space 34368K,   0% used [0x0000000419e10000, 0x0000000419e10000, 0x000000041bfa0000)
 tenured generation   total 687232K, used 5711K [0x00000005568a0000, 0x00000005807c0000, 0x00000007f5a00000)
   the space 687232K,   0% used [0x00000005568a0000, 0x0000000556e33cb0, 0x0000000556e33e00, 0x00000005807c0000)
 compacting perm gen  total 21248K, used 3139K [0x00000007f5a00000, 0x00000007f6ec0000, 0x0000000800000000)
   the space 21248K,  14% used [0x00000007f5a00000, 0x00000007f5d10f28, 0x00000007f5d11000, 0x00000007f6ec0000)
No shared spaces configured.
```

- [0x0000000407000000, 0x000000040aa5fd48, 0x0000000417c80000):0x0000000407000000为eden起始地址，0x000000040aa5fd48为eden未使用的最小地址，0x0000000417c80000为eden的边界地址。上界减去下界为eden的总大小，未使用的最小地址减去下界为eden已经使用的空间大小

#### -XX:+PrintHeapAtGC

打印gc前后heap的使用情况

```
{Heap before GC invocations=16 (full 0):
 def new generation   total 309248K, used 274446K [0x0000000407000000, 0x000000041bf80000, 0x00000005568a0000)
  eden space 274944K,  99% used [0x0000000407000000, 0x0000000417c03b20, 0x0000000417c80000)
  from space 34304K,   0% used [0x0000000417c80000, 0x0000000417c80088, 0x0000000419e00000)
  to   space 34304K,   0% used [0x0000000419e00000, 0x0000000419e00000, 0x000000041bf80000)
 tenured generation   total 687232K, used 5711K [0x00000005568a0000, 0x00000005807c0000, 0x00000007f5a00000)
   the space 687232K,   0% used [0x00000005568a0000, 0x0000000556e33c28, 0x0000000556e33e00, 0x00000005807c0000)
 compacting perm gen  total 21248K, used 3132K [0x00000007f5a00000, 0x00000007f6ec0000, 0x0000000800000000)
   the space 21248K,  14% used [0x00000007f5a00000, 0x00000007f5d0f1a8, 0x00000007f5d0f200, 0x00000007f6ec0000)
No shared spaces configured.
Heap after GC invocations=17 (full 0):
 def new generation   total 309312K, used 0K [0x0000000407000000, 0x000000041bfa0000, 0x00000005568a0000)
  eden space 274944K,   0% used [0x0000000407000000, 0x0000000407000000, 0x0000000417c80000)
  from space 34368K,   0% used [0x0000000417c80000, 0x0000000417c80000, 0x0000000419e10000)
  to   space 34368K,   0% used [0x0000000419e10000, 0x0000000419e10000, 0x000000041bfa0000)
 tenured generation   total 687232K, used 5711K [0x00000005568a0000, 0x00000005807c0000, 0x00000007f5a00000)
   the space 687232K,   0% used [0x00000005568a0000, 0x0000000556e33cb0, 0x0000000556e33e00, 0x00000005807c0000)
 compacting perm gen  total 21248K, used 3132K [0x00000007f5a00000, 0x00000007f6ec0000, 0x0000000800000000)
   the space 21248K,  14% used [0x00000007f5a00000, 0x00000007f5d0f1a8, 0x00000007f5d0f200, 0x00000007f6ec0000)
No shared spaces configured.
}
```

#### -XX:+PrintGCTimeStamps

打印gc执行时间。该时间为距离系统启动的时间间距

```
0.369: [GC0.369: [DefNew: 274549K->5713K(309248K), 0.0152060 secs] 274549K->5713K(996480K), 0.0154200 secs] [Times: user=0.01 sys=0.01, real=0.01 secs] 
0.431: [GC0.431: [DefNew: 279669K->5711K(309248K), 0.0091020 secs] 279669K->5711K(996480K), 0.0091760 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
0.487: [GC0.487: [DefNew: 279978K->5711K(309248K), 0.0051660 secs] 279978K->5711K(996480K), 0.0052530 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
```

表示在jvm启动后的0.369s、0.431s、0.487s发生minior gc

#### -XX:+PrintGCApplicationConcurrentTime

打印应用程序的执行时间

```
0.489: Application time: 0.4221190 seconds
0.553: Application time: 0.0488380 seconds
0.615: Application time: 0.0477270 seconds
0.666: Application time: 0.0466810 seconds
0.716: Application time: 0.0447880 seconds
0.772: Application time: 0.0504090 seconds
0.825: Application time: 0.0479900 seconds
0.874: Application time: 0.0443830 seconds
0.924: Application time: 0.0450710 seconds
0.975: Application time: 0.0453640 seconds
```



#### -XX:+PrintGCApplicationStoppedTime

打印应用程序暂停的时间

```
0.592: Total time for which application threads were stopped: 0.0157940 seconds, Stopping threads took: 0.0000420 seconds
0.658: Total time for which application threads were stopped: 0.0119090 seconds, Stopping threads took: 0.0000430 seconds
0.708: Total time for which application threads were stopped: 0.0045830 seconds, Stopping threads took: 0.0000860 seconds
0.756: Total time for which application threads were stopped: 0.0041520 seconds, Stopping threads took: 0.0000480 seconds
0.804: Total time for which application threads were stopped: 0.0042800 seconds, Stopping threads took: 0.0000270 seconds
```

#### -XX:+PrintReferenceGC

打印不同类型引用的使用情况

```
[GC[DefNew[SoftReference, 0 refs, 0.0000480 secs][WeakReference, 0 refs, 0.0000070 secs][FinalReference, 4 refs, 0.0000180 secs][PhantomReference, 0 refs, 0 refs, 0.0000160 secs][JNI Weak Reference, 0.0000080 secs]: 280497K->5711K(309248K), 0.0046380 secs] 280497K->5711K(996480K), 0.0046940 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
[GC[DefNew[SoftReference, 0 refs, 0.0000460 secs][WeakReference, 0 refs, 0.0000180 secs][FinalReference, 4 refs, 0.0000120 secs][PhantomReference, 0 refs, 0 refs, 0.0000150 secs][JNI Weak Reference, 0.0000080 secs]: 280377K->5711K(309248K), 0.0048110 secs] 280377K->5711K(996480K), 0.0048680 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC[DefNew[SoftReference, 0 refs, 0.0000420 secs][WeakReference, 0 refs, 0.0000170 secs][FinalReference, 4 refs, 0.0000140 secs][PhantomReference, 0 refs, 0 refs, 0.0000210 secs][JNI Weak Reference, 0.0000080 secs]: 280298K->5711K(309248K), 0.0045820 secs] 280298K->5711K(996480K), 0.0046410 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
```



#### -Xloggc:/tmp/log/gc.log

默认的gc日志输出到控制台上，指定输出gc信息到指定文件

```
[root@k8s jvm-demo]# ls /tmp/log
gc.log
```



## 类加载/卸载跟踪

#### -verbose:class

查看类的加载和卸载信息

```
[Opened /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
[Loaded java.lang.Object from /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
[Loaded java.io.Serializable from /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
[Loaded java.lang.Comparable from /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
[Loaded java.lang.CharSequence from /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
[Loaded java.lang.String from /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
[Loaded java.lang.reflect.GenericDeclaration from /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
[Loaded java.lang.reflect.Type from /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
[Loaded java.lang.reflect.AnnotatedElement from /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
...
```



#### -XX:+TraceClassLoading

跟踪类的加载信息

```
[Opened /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
[Loaded java.lang.Object from /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
[Loaded java.io.Serializable from /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
[Loaded java.lang.Comparable from /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
[Loaded java.lang.CharSequence from /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
[Loaded java.lang.String from /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
[Loaded java.lang.reflect.GenericDeclaration from /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
[Loaded java.lang.reflect.Type from /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
[Loaded java.lang.reflect.AnnotatedElement from /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar]
...
```



#### -XX:+TraceClassUnloading

跟踪类的卸载信息



#### -XX:+PrintClassHistogram

打印当前类信息的柱状图



## 系统参数查看

#### -XX:+PrintVMOptions

打印启动jvm时显示指定的参数

```
VM option '-TraceClassLoading'
VM option '-TraceClassUnloading'
VM option '-PrintClassHistogram'
VM option '+PrintVMOptions'
```



#### -XX:+PrintCommandLineFlags

打印启动jvm时显示或隐式指定的参数

```
-XX:InitialHeapSize=1055487168 -XX:MaxHeapSize=16887794688 -XX:-PrintClassHistogram -XX:+PrintCommandLineFlags -XX:-PrintVMOptions -XX:-TraceClassLoading -XX:-TraceClassUnloading -XX:+UseCompressedOops -XX:+UseParallelGC 
```



#### -XX:+PrintFlagsFinal

打印虚拟机所以的默认参数设置

```
[Global flags]
    uintx AdaptivePermSizeWeight                    = 20              {product}           
    uintx AdaptiveSizeDecrementScaleFactor          = 4               {product}           
    uintx AdaptiveSizeMajorGCDecayTimeScale         = 10              {product}           
    uintx AdaptiveSizePausePolicy                   = 0               {product}           
    uintx AdaptiveSizePolicyCollectionCostMargin    = 50              {product}           
    uintx AdaptiveSizePolicyInitializingSteps       = 20              {product}           
    uintx AdaptiveSizePolicyOutputInterval          = 0               {product}           
    uintx AdaptiveSizePolicyWeight                  = 10              {product}           
    uintx AdaptiveSizeThroughPutPolicy              = 0               {product}           
    uintx AdaptiveTimeWeight                        = 25              {product}     
   ...
    uintx YoungGenerationSizeIncrement              = 20              {product}           
    uintx YoungGenerationSizeSupplement             = 80              {product}           
    uintx YoungGenerationSizeSupplementDecay        = 8               {product}           
    uintx YoungPLABSize                             = 4096            {product}           
     bool ZeroTLAB                                  = false           {product}           
     intx hashCode                                  = 0               {product}       
```



## 让性能飞起来：学习堆的配置参数

#### 最大堆和初始堆设置

参数-Xms指定jvm启动时堆的初始大小，随着程序的运行，当内存不够时，jvm的内存会逐步增大至-Xmx的值。

#### demo展示

jvm参数：-Xmx20m -Xms5m -XX:+PrintCommandLineFlags -XX:+PrintGCDetails  -XX:+UseSerialGC

```
public class HeapAlloc {
    public static void main(String[] args) {
        System.out.print("maxMemory=");
        System.out.println(Runtime.getRuntime().maxMemory() + " bytes");
        System.out.print("free mem=");
        System.out.println(Runtime.getRuntime().freeMemory() + " bytes");
        System.out.print("total mem=");
        System.out.println(Runtime.getRuntime().totalMemory() + " bytes");

        byte[] b = new byte[1 * 1024 * 1024];
        System.out.println("分配了1M空间给数组");

        System.out.print("maxMemory=");
        System.out.println(Runtime.getRuntime().maxMemory() + " bytes");
        System.out.print("free mem=");
        System.out.println(Runtime.getRuntime().freeMemory() + " bytes");
        System.out.print("total mem=");
        System.out.println(Runtime.getRuntime().totalMemory() + " bytes");

        b = new byte[4 * 1024 * 1024];
        System.out.println("分配了4M空间给数组");

        System.out.print("maxMemory=");
        System.out.println(Runtime.getRuntime().maxMemory() + " bytes");
        System.out.print("free mem=");
        System.out.println(Runtime.getRuntime().freeMemory() + " bytes");
        System.out.print("total mem=");
        System.out.println(Runtime.getRuntime().totalMemory() + " bytes");
    }
}
```

结果：

```
-XX:InitialHeapSize=5242880 -XX:MaxHeapSize=20971520 -XX:+PrintCommandLineFlags -XX:+PrintGCDetails -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:-UseLargePagesIndividualAllocation -XX:+UseSerialGC 
maxMemory=20316160 bytes
free mem=4428192 bytes
total mem=6094848 bytes
[GC (Allocation Failure) [DefNew: 1627K->191K(1856K), 0.0012867 secs] 1627K->611K(5952K), 0.0013202 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
分配了1M空间给数组
maxMemory=20316160 bytes
free mem=4369888 bytes
total mem=6094848 bytes
[GC (Allocation Failure) [DefNew: 1265K->0K(1856K), 0.0010964 secs][Tenured: 1635K->1635K(4096K), 0.0011596 secs] 1684K->1635K(5952K), [Metaspace: 3141K->3141K(1056768K)], 0.0022920 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
分配了4M空间给数组
maxMemory=20316160 bytes
free mem=4380504 bytes
total mem=10358784 bytes
Heap
 def new generation   total 1920K, used 123K [0x00000000fec00000, 0x00000000fee10000, 0x00000000ff2a0000)
  eden space 1728K,   7% used [0x00000000fec00000, 0x00000000fec1ef40, 0x00000000fedb0000)
  from space 192K,   0% used [0x00000000fedb0000, 0x00000000fedb0000, 0x00000000fede0000)
  to   space 192K,   0% used [0x00000000fede0000, 0x00000000fede0000, 0x00000000fee10000)
 tenured generation   total 8196K, used 5731K [0x00000000ff2a0000, 0x00000000ffaa1000, 0x0000000100000000)
   the space 8196K,  69% used [0x00000000ff2a0000, 0x00000000ff838ff8, 0x00000000ff839000, 0x00000000ffaa1000)
 Metaspace       used 3223K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 348K, capacity 388K, committed 512K, reserved 1048576K
```

- maxMemory：表示堆内存的最大值，通过-Xmx设置
- free mem：剩余堆内存
- total mem：当前堆总大小

maxMemory保持不变、total mem随着程序的运行逐步增大

最佳实践：建议将Xms和Xmx的值设置相等。减少程序运行过程中gc的次数，也减少了由于内存扩展造成性能下降的问题

### 新生代配置

参数-Xmn设置新生代的大小。新生代的增大会导致老年代的减小，所以该参数对系统性能及GC影响较大。推荐新生代占总堆大小的1/4-1/3。

参数-XX:SurivorRatio设置eden和from、to之间的比例，其值=eden/from=eden/to。

#### demo展示

```
public class NewSizeDemo {
    public static void main(String[] args) {
       byte[] b=null;
       for(int i=0;i<10;i++)
           b=new byte[1*1024*1024];
    }
}

```

场景一：

jvm参数：-Xmx20m -Xms20m -Xmn1m   -XX:SurvivorRatio=2 -XX:+PrintGCDetails -XX:+UseSerialGC

```
[GC (Allocation Failure) [DefNew: 512K->256K(768K), 0.0009190 secs] 512K->434K(20224K), 0.0009646 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [DefNew: 768K->106K(768K), 0.0012163 secs] 946K->540K(20224K), 0.0012483 secs] [Times: user=0.00 sys=0.02, real=0.00 secs] 
[GC (Allocation Failure) [DefNew: 617K->166K(768K), 0.0004632 secs] 1050K->600K(20224K), 0.0004824 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
Heap
 def new generation   total 768K, used 384K [0x00000000fec00000, 0x00000000fed00000, 0x00000000fed00000)
  eden space 512K,  42% used [0x00000000fec00000, 0x00000000fec366a0, 0x00000000fec80000)
  from space 256K,  65% used [0x00000000fecc0000, 0x00000000fece9a38, 0x00000000fed00000)
  to   space 256K,   0% used [0x00000000fec80000, 0x00000000fec80000, 0x00000000fecc0000)
 tenured generation   total 19456K, used 10673K [0x00000000fed00000, 0x0000000100000000, 0x0000000100000000)
   the space 19456K,  54% used [0x00000000fed00000, 0x00000000ff76c6d0, 0x00000000ff76c800, 0x0000000100000000)
 Metaspace       used 3234K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 350K, capacity 388K, committed 512K, reserved 1048576K
```

eden空间站512k，from/to占用空间256k。符合SurvivorRatio=2=eden/from=eden/to。total 768K为eden+from空间的大小。新生代的总大小=eden+from+to=1m，也就是-Xmn1m的参数设定。由于eden不能存放1m的数据，该对象直接进入老年代，导致老年代的大小最终为10673K。

场景二：

jvm参数设置：-Xmx20m -Xms20m -Xmn7m   -XX:SurvivorRatio=2 -XX:+PrintGCDetails -XX:+UseSerialGC

```
[GC (Allocation Failure) [DefNew: 2743K->1643K(5376K), 0.0021084 secs] 2743K->1643K(18688K), 0.0021456 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [DefNew: 4823K->1036K(5376K), 0.0018077 secs] 4823K->1649K(18688K), 0.0018288 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [DefNew: 4193K->1024K(5376K), 0.0005067 secs] 4806K->1649K(18688K), 0.0005304 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
Heap
 def new generation   total 5376K, used 4235K [0x00000000fec00000, 0x00000000ff300000, 0x00000000ff300000)
  eden space 3584K,  89% used [0x00000000fec00000, 0x00000000fef22de8, 0x00000000fef80000)
  from space 1792K,  57% used [0x00000000ff140000, 0x00000000ff2400d8, 0x00000000ff300000)
  to   space 1792K,   0% used [0x00000000fef80000, 0x00000000fef80000, 0x00000000ff140000)
 tenured generation   total 13312K, used 625K [0x00000000ff300000, 0x0000000100000000, 0x0000000100000000)
   the space 13312K,   4% used [0x00000000ff300000, 0x00000000ff39c6e8, 0x00000000ff39c800, 0x0000000100000000)
 Metaspace       used 3156K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 343K, capacity 388K, committed 512K, reserved 1048576K
```

新生代可用内存为5376K，无法容纳10m的数据，所以发生了3次minio gc。老年代最终占用625K，可见所有的数据都分配在新生代中。

场景三：

jvm参数设置：-Xmx20m -Xms20m -Xmn15m  -XX:SurvivorRatio=8 -XX:+PrintGCDetails -XX:+UseSerialGC

```
Heap
 PSYoungGen      total 13824K, used 11223K [0x00000000ff100000, 0x0000000100000000, 0x0000000100000000)
  eden space 12288K, 91% used [0x00000000ff100000,0x00000000ffbf5fd8,0x00000000ffd00000)
  from space 1536K, 0% used [0x00000000ffe80000,0x00000000ffe80000,0x0000000100000000)
  to   space 1536K, 0% used [0x00000000ffd00000,0x00000000ffd00000,0x00000000ffe80000)
 ParOldGen       total 5120K, used 0K [0x00000000fec00000, 0x00000000ff100000, 0x00000000ff100000)
  object space 5120K, 0% used [0x00000000fec00000,0x00000000fec00000,0x00000000ff100000)
 PSPermGen       total 21504K, used 3135K [0x00000000f4600000, 0x00000000f5b00000, 0x00000000fec00000)
  object space 21504K, 14% used [0x00000000f4600000,0x00000000f490fd78,0x00000000f5b00000)
```

新生代空间为12288K，可用容纳10m的数组对象，所以没有发生gc，所有的新生对象都分配在新生代中。

最佳实践：对象应该尽可能的分配在新生代，减少老年代gc的次数

场景四：

- -XX:NewRatio=2：设置老年代/新生代=2，通过和老年代的比例设置新生代的大小

jvm参数设置：-Xmx20M -Xms20M -XX:NewRatio=2  -XX:+PrintGCDetails -XX:+UseSerialGC

```
[GC (Allocation Failure) [DefNew: 4771K->613K(6144K), 0.0016240 secs] 4771K->1637K(19840K), 0.0016606 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [DefNew: 5896K->0K(6144K), 0.0021129 secs] 6920K->2655K(19840K), 0.0021343 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
Heap
 def new generation   total 6144K, used 2241K [0x00000000fec00000, 0x00000000ff2a0000, 0x00000000ff2a0000)
  eden space 5504K,  40% used [0x00000000fec00000, 0x00000000fee300f8, 0x00000000ff160000)
  from space 640K,   0% used [0x00000000ff160000, 0x00000000ff160390, 0x00000000ff200000)
  to   space 640K,   0% used [0x00000000ff200000, 0x00000000ff200000, 0x00000000ff2a0000)
 tenured generation   total 13696K, used 2654K [0x00000000ff2a0000, 0x0000000100000000, 0x0000000100000000)
   the space 13696K,  19% used [0x00000000ff2a0000, 0x00000000ff537b18, 0x00000000ff537c00, 0x0000000100000000)
 Metaspace       used 3215K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 347K, capacity 388K, committed 512K, reserved 1048576K
```

新生代的大小=20*1/3=6，eden空间占用5504K，不足以容纳10m的数据所以会多次触发minior gc。from空间占用640K，不足以容纳1m的数组对象，gc时需要老年代的空间担保，最终会有2m的数据保存至老年代

#### Xmx、Xms、SurvivorRatio和NewRatio的关系

![1645815003095](D:\code\jvm-in-action\assets\1645815003095.png)



#### 堆溢出处理

堆溢出时会报出如下的错误：

```
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
	at com.ctbiyi.jvm.ch3.heap.DumpOOM.main(DumpOOM.java:19)
```

堆溢出时需要在jvm崩溃前将堆信息导出，可以使用的参数为：HeapDumpOnOutMemoryError、HeapDumPath。

- -XX:+HeapOnOutOfMemoryError：发送堆溢出时将dump堆信息
- -XX:HeapDumpPath：设置dump文件的保存路径

#### demo展示：

jvm参数设置：-Xmx20m -Xms5m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=d:/a.dump

```
public class DumpOOM {
    public static void main(String[] args) {
        Vector v=new Vector();
        for(int i=0;i<25;i++)
            v.add(new byte[1*1024*1024]);
    }
}
```

结果：

```
java.lang.OutOfMemoryError: Java heap space
Dumping heap to d:/a.dump ...
Unable to create d:/a.dump: File exists
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
	at com.ctbiyi.jvm.ch3.heap.DumpOOM.main(DumpOOM.java:19)
```



### 被让性能有缺口：了解非堆内存的参数设置

非堆内存参数包括方法区、线程栈、直接内存。

#### 方法区配置

方法区主要用于保存类的元数据。jdk8使用metaspace作为方法区实现。可以通过参数-XX:MaxMetaspaceSize、-XX:MetaspaceSize参数设置

#### 栈配置

栈为线程私有，可以通过-xss参数设置

#### 直接内存配置

java支持向系统直接申请内存用于保存对象。通过参数-XX:MaxDirectMemorySize设置，默认大小和-Xmx保持一致。当直接内存不够用时也会触发GC，发生GC内存仍旧不够用会抛出OOM。

直接内存相比堆内存的优势在于访问数据比较快，劣势是创建速度较慢。直接内存用在不需要频繁创建，读写频繁的场景

#### demo展示

场景一：

```
public class AccessDirectBuffer {
    public void directAccess(){
        long starttime=System.currentTimeMillis();
        ByteBuffer b=ByteBuffer.allocateDirect(500);
        for(int i=0;i<100000;i++){
            for(int j=0;j<99;j++)
                b.putInt(j);
            b.flip();
            for(int j=0;j<99;j++)
                b.getInt();
            b.clear();
        }
        long endtime=System.currentTimeMillis();
        System.out.println("testDirectWrite:"+(endtime-starttime));
    }

    public void bufferAccess() {
        long starttime=System.currentTimeMillis();
        ByteBuffer b=ByteBuffer.allocate(500);
        for(int i=0;i<100000;i++){
            for(int j=0;j<99;j++)
                b.putInt(j);
            b.flip();
            for(int j=0;j<99;j++)
                b.getInt();
            b.clear();
        }
        long endtime=System.currentTimeMillis();
        System.out.println("testBufferWrite:"+(endtime-starttime));
    }
    
    public static void main(String[] args) {
        AccessDirectBuffer alloc=new AccessDirectBuffer();
        alloc.bufferAccess();
        alloc.directAccess();
        
        alloc.bufferAccess();
        alloc.directAccess();
    }
}
```

结果：

```
testBufferWrite:27
testDirectWrite:18
testBufferWrite:17
testDirectWrite:36
```

场景二：

```
public class AllocDirectBuffer {
    public void directAllocate(){
        long starttime=System.currentTimeMillis();
        for(int i=0;i<200000;i++){
            ByteBuffer b=ByteBuffer.allocateDirect(1000);
        }
        long endtime=System.currentTimeMillis();
        System.out.println("directAllocate:"+(endtime-starttime));
    }

    public void bufferAllocate() {
        long starttime=System.currentTimeMillis();
        for(int i=0;i<200000;i++){
            ByteBuffer b=ByteBuffer.allocate(1000);
        }
        long endtime=System.currentTimeMillis();
        System.out.println("bufferAllocate:"+(endtime-starttime));
    }
    
    public static void main(String[] args) {
        AllocDirectBuffer alloc=new AllocDirectBuffer();
        alloc.bufferAllocate();
        alloc.directAllocate();
        
        alloc.bufferAllocate();
        alloc.directAllocate();
    }
}

```

结果：

```
bufferAllocate:80
directAllocate:134
bufferAllocate:95
directAllocate:109
```

可以看出：直接内存读写较快，分配较慢



#### 虚拟机的工作模式

虚拟机可以工作在client和server模式。server模式需要收集比较多的程序运行时参数，会对程序进行优化，所以server模式的性能较client的高，但是启动较慢。

```
java  -XX:+PrintFlagsFinal -version  NewSizeDemo  | grep  CompileThreshold
     intx CompileThreshold                          = 10000           {pd product}        
     intx Tier2CompileThreshold                     = 0               {product}           
     intx Tier3CompileThreshold                     = 2000            {product}           
     intx Tier4CompileThreshold                     = 15000           {product}           
java version "1.7.0_261"
OpenJDK Runtime Environment (rhel-2.6.22.2.el7_8-x86_64 u261-b02)
OpenJDK 64-Bit Server VM (build 24.261-b02, mixed mode)
```

Server模式的CompileThreshold为10000，表示方法被调用10000次后才会被JIT编译为机器码，方法的执行由解释执行变为编译执行。



# 垃圾回收概念和算法

### 认识垃圾回收

程序运行过程中创建大量的对象需要保存在内存中，但是内存的大小有一定的上限，为了合理利用内存资源需要将不再使用的对象回收掉，这个过程成为垃圾回收

## 讨论常见的垃圾回收算法

### 引用计数法

引用计数法算法：每个对象有一个保存该对象被其它对象使用的引用计数器，当其它对象指向该对象时引用计数加1，当其它对象取消掉对该对象的引用时该引用计数器减一，直到引用计数器为0表示该对象没有被其它对象使用成为垃圾时被系统回收。

存在的问题：

- 循环引用问题

  对象A和对象B都保存对方的引用，但是它们两个整体不再被其它对象引用而成为垃圾，但是引用计数器无法识别出这种垃圾而失效

  ![1645819694228](D:\code\jvm-in-action\assets\1645819694228.png)

  

- 引用变化时引用计数器需要加一或者减一操作，对系统性能带来不良影响

#### 标记清除（Mark-Sweep）

标记清除算法分为两个节点：标记阶段、清除阶段。

- 标记阶段

  标记从根节点所有可达的对象，不可达的对象为垃圾对象

- 清除阶段

  清除上一阶段未被标记的垃圾对象

存在的问题：产生内存碎片

##### 标记阶段：

![1645820402336](D:\code\jvm-in-action\assets\1645820868733.png)

通过枚举根节点将垃圾对象和正常对象区分开来

##### 清除阶段：

![1645820656047](D:\code\jvm-in-action\assets\1645820888846.png)

清除掉垃圾对象，未使用内存不再连续，内存碎片产生

#### 复制算法（Coping）

复制算法：将内存切分为两份，每次只使用其中的一块。在垃圾回收时将正在使用内存中的存活对象复制到另一块未使用的部分，然后清除包含垃圾那块内存中的所有数据后将其标记为未使用。

优势：

- 如果对象的存活率较低，则需要复制的对象较少，此时复制算法的效率较高（新生代对象的特点“朝生夕死”，死亡率非常高，比较合适新生代使用）
- 没有内存碎片产生

劣势：

- 需要一块内存留作未使用

![1645841740495](D:\code\jvm-in-action\assets\1645841740495.png)

#### 在java中的应用

java中的新生代垃圾回收的串行回收算法采用复制算法。新生代由eden、from和to区组成。eden保存新建的对象，from和to区的大小相等，保存年龄不大的对象，但是角色可以互换。from和to又被称为survivor区。

新创建的对象首先放在eden区（大对象会直接存放值老年代），当eden区的内存区域不够时会触发垃圾回收。垃圾回收会将eden和from区中仍旧存活的对象复制到to区（年龄查过一定阈值的存活对象会进入老年代，to区的内存不足时存活对象会保存至老年代），然后清除eden和from区域，转换from和to区域的角色。



![1645842769805](D:\code\jvm-in-action\assets\1645842806723.png)

- 新生代：保存年轻代对象的堆区域。年轻对象表示新创建或者经历垃圾回收次数不多的对象
- 老年代：存放老年对象的堆区域。老年对象表示经过长时间的垃圾回收仍旧存活的对象

### 标记压缩算法（Mark-Compact）

老年代的对象存活率比较高，这意味着采用复制算法的话不仅需要更大的未使用内存，复制对象的时间也会更长。其不使用复制算法。 

标记压缩算法：也分为两个阶段标记和压缩

- 标记阶段：标记根对象可达的对象为存活对象，未被标记的对象为垃圾对象
- 压缩阶段：将所有的存活对象复制到内存空间的一端，其余空间作为未使用空间保存对象

优势：

- 不需要保留一块未使用的存在保存存活对象
- 垃圾回收后没有内存碎片



![1645843633367](D:\code\jvm-in-action\assets\1645843633367.png)



### 分代算法（Generational Collecting）

标记清除、复制算法和标记整理都有各自的优势和劣势，而对象在不同时期的特点也不相同：新生成的对象存活率很高，超过一定时间后仍旧存活的对象很有可能仍旧存活下去。分代算法将内存划分为不同的区域，保存不同生命周期的对象，采用不同的垃圾回收算法。

新生代的对象采用复制算法，老年代的对象采用标记清除或标记整理算法

![1645844385455](D:\code\jvm-in-action\assets\1645844385455.png)

#### 卡表（Card Table）

新生代的对象在标记阶段使用老年代的对象作为根节点。逐个枚举老年代的对象比较耗时，为了加速这种枚举需要使用卡表。卡表是一个比特集合，每一个bit保存老年代指定大小区域中的对象是否引用新生代的对象，如果引用则标记为1，没有引用标记为0。新生代进行垃圾回收时只枚举卡表为1的那些区域的老年代中的对象作为根节点。

![1645844977194](D:\code\jvm-in-action\assets\1645844977194.png)



### 分区算法（Region）

分区算法将内存区域划分为多个独立的小区域，每个区域可以独立使用、独立回收。从而可以控制每次回收块的个数达到控制gc停顿时间。

相同条件下，堆的空间越多，垃圾回收工作的时间越长，应用暂停的时间越长。将堆划分为不同的块，从而可以根据应用的对暂停时间的要求回收不同个数的块，达到了控制应用的暂停时间。

![1645846144584](D:\code\jvm-in-action\assets\1645846201015.png)



## 谁才是真正的垃圾：判断可触及性

对象的可触及性包含三种状态：

- 可触及的：从根节点开始可以达到这个对象
- 可复活：没有指向该对象的引用，但是对象可以在finalize()中复活自己
- 不可触及的：对象的finalize()已经被调用了，并且没有指向该对象的引用，则对象进入了不可触及状态（finalize函数只能被调用一次）

只有不可触及的对象才能被回收。

### 对象的复活

demo展示

```
public class TraceCanReliveObj {
	public static TraceCanReliveObj obj;

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		System.out.println("CanReliveObj finalize called");
		obj=this;
	}
	@Override
	public String toString(){
		return "I am CanReliveObj";
	}
	public static void main(String[] args) throws InterruptedException{
		obj=new TraceCanReliveObj();

		obj=null;
		System.gc();
		Thread.sleep(1000);
		if(obj==null){
			System.out.println("obj 是 null");
		}else{
			System.out.println("obj 可用,"+obj);
		}
		System.out.println("第二次gc");
		obj=null;
		System.gc();
		Thread.sleep(1000);
		if(obj==null){
			System.out.println("obj 是 null");
		}else{
			System.out.println("obj 可用");
		}
	}
}
```

结果：

```
CanReliveObj finalize called
obj 可用,I am CanReliveObj
第二次gc
obj 是 null
```

垃圾对象可以在finalize函数中添加指向该对象的引用复活对象。但是finalize函数只能被调用一次。



### 引用和可触及性的强度

java提供了4个级别的引用：强引用、软引用、弱引用和虚引用 。

对应的类结构如下所示：

![1645856127036](D:\code\jvm-in-action\assets\1645856127036.png)

强引用是可触及的，不会被回收的，软引用、弱引用和虚引用是软可触及、弱可触及和虚可触及的。在一定条件下都是可回收的。这里的是否可被回收表示在相应引用存在的前提下，对象是否可被回收

### 强引用

一般的引用为强引用的，如下：

```
		StringBuffer sb = new StringBuffer("strong reference");
```

特点：

- 通过强引用可以直接访问对象
- 强引用指向的对象不会被回收，哪怕jvm出现oom

### 软引用-可被回收的引用

软引用在内存不够时候会被gc回收

#### demo展示

jvm参数设置：  -Xmx10m -XX:+PrintGC

```
public class SoftRef {
    public static void main(String[] args) {
        byte[] barr=new byte[1024*925*7];
        SoftReference<byte[]> userSoftRef = new SoftReference(barr);
        barr=null;
        
        System.out.println(userSoftRef.get());
        System.gc();
        System.out.println("After GC:");
        System.out.println(userSoftRef.get());
        
        byte[] b=new byte[1024*925*7];
        System.gc();
        System.out.println(userSoftRef.get());
    }
}
```

结果：

```
[B@7f31245a
[GC (System.gc())  8222K->7143K(9728K), 0.0012752 secs]
[Full GC (System.gc())  7143K->7091K(9728K), 0.0037128 secs]
After GC:
[B@7f31245a
[GC (Allocation Failure)  7155K->7187K(9728K), 0.0004198 secs]
[Full GC (Ergonomics)  7187K->7085K(9728K), 0.0051396 secs]
[GC (Allocation Failure)  7085K->7085K(9728K), 0.0005708 secs]
[Full GC (Allocation Failure)  7085K->592K(8192K), 0.0051759 secs]
[GC (System.gc())  7147K->7179K(9728K), 0.0006203 secs]
[Full GC (System.gc())  7179K->7090K(9728K), 0.0059224 secs]
null
```

数组只被软引用指向，当内存即使发生gc也不会被清理掉。当创建7m强引用指向的对象时会清理掉软引用指向的对象

### 弱引用-发现即回收

gc工作时只要发现弱引用就会进行回收

```
public class WeakRef {
    public static class User{
        public User(int id,String name){
            this.id=id;
            this.name=name;
        }
        public int id;
        public String name;
        
        @Override
        public String toString(){
            return "[id="+String.valueOf(id)+",name="+name+"]";
        }
    }
    public static void main(String[] args) {
        User u=new User(1,"geym");
        WeakReference<User> userWeakRef = new WeakReference<User>(u);
        u=null;
        System.out.println(userWeakRef.get());
        System.gc();
        //不管当前内存空间足够与否，都会回收它的内存
        System.out.println("After GC:");
        System.out.println(userWeakRef.get());
    }
}
```

结果：

```
[id=1,name=geym]
After GC:
null
```

最佳实践：软引用和弱引用可以用来保存缓存数据，即增加了系统的运行速度，又避免了oom的产生。

### 虚引用-对象回收跟踪

垃圾回收是会回收调虚引用的对象，通过虚引用不能获得的引用对象。虚引用需要和引用队列一块使用，主要用于通知引用对象的回收情况

```
public class TraceCanReliveObj {
	public static TraceCanReliveObj obj;
	 static ReferenceQueue<TraceCanReliveObj> phantomQueue=null;
	    public static class CheckRefQueue extends Thread{
	        @Override
	        public void run(){
	            while(true){
	                if(phantomQueue!=null){
	                    PhantomReference<TraceCanReliveObj> objt=null;
	                            try {
	                                objt = (PhantomReference<TraceCanReliveObj>)phantomQueue.remove();
	                            } catch (InterruptedException e) {
	                                e.printStackTrace();
	                            }
	                            if(objt!=null){
	                                System.out.println("TraceCanReliveObj is delete by GC");
	                            }
	                }
	            }
	        }
	    }
	    
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		System.out.println("CanReliveObj finalize called");
		obj=this;
	}
	@Override
	public String toString(){
		return "I am CanReliveObj";
	}
	public static void main(String[] args) throws InterruptedException{
        Thread t=new CheckRefQueue();
        t.setDaemon(true);
        t.start();
        
	    phantomQueue = new ReferenceQueue<TraceCanReliveObj>();  
		obj=new TraceCanReliveObj();
		PhantomReference<TraceCanReliveObj> phantomRef = new PhantomReference<TraceCanReliveObj>(obj,phantomQueue);
		
		obj=null;
		System.out.println(phantomRef.get());
		System.gc();
		Thread.sleep(1000);
		if(obj==null){
			System.out.println("obj 是 null");
		}else{
			System.out.println("obj 可用");
		}
		System.out.println("第二次gc");
		obj=null;
		System.gc();
		Thread.sleep(1000);
		if(obj==null){
			System.out.println("obj 是 null");
		}else{
			System.out.println("obj 可用");
		}
	}
}

```

结果：

```
null
CanReliveObj finalize called
obj 可用
第二次gc
TraceCanReliveObj is delete by GC
obj 是 null
```



# 垃圾收集器和内存分配

### 串行回收器

特点：

- 使用单线程进行垃圾回收
- 工作方式是独占式的：垃圾回收时所有的应用线程需要暂停

优点：

- 简单、高效，没有线程切换带来的开销，适用于单核cpu的场景

可以工作在新生代和老年代。

![1645860528716](D:\code\jvm-in-action\assets\1645860807216.png)

### 新生代串行回收器

jvm参数：+UseSerialGC表示老年代和新生代启用串行回收器

日志格式：

```
0.229: [GC (Allocation Failure) 0.229: [DefNew: 959K->63K(960K), 0.0028728 secs] 20962K->20958K(1048512K), 0.0029288 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
```

上面为新生代为串行回收器gc时的日志。

### 老年代串行回收器

老年代串行回收器采用标记-压缩算法，工作方式也是独占式，老年代的空间比较大意味着应用线程停顿的时间比较长。

可以通过下面的jvm参数启动老年代串行回收器：

- -XX:+UseSerialGC 新生代和老年代都使用串行回收器
- -XX:+UseParNewGC 新生代使用ParNew回收器，老年代使用串行回收器
- -XX:+UseParallelGC 新生代使用Parallel回收器，老年代使用串行回收器

日志展示：

```
[Full GC (Allocation Failure) [Tenured: 1047551K->1047551K(1047552K), 0.8118628 secs] 1048511K->1048506K(1048512K), [Metaspace: 3785K->3785K(1056768K)], 0.8119044 secs] [Times: user=0.81 sys=0.00, real=0.81 secs] 
```

## 人多力量大：并行回收器

### 新生代ParNew回收器

串行回收器的多线程版本，工作方式和串行回收器保持一致，也是独占式。

优点：在多核cpu环境比串行回收器引起的应用线程暂停时间更短。单核环境由于线程切换表现比串行回收器更差

![1645862285049](D:\code\jvm-in-action\assets\1645862285049.png)



开启的jvm参数：

- -XX:+UseParNewGC 新生代使用ParNew并行回收器，老年代使用串行回收器
- -XX:+UseConcMarkSweepGC 新生代使用ParNew回收器，老年代使用cms回收器

ParNew回收器的工作线程数量可以通过-XX:ParallelGCThreads参数指定。

日志输出格式：

```
[GC (Allocation Failure) [ParNew: 960K->62K(960K), 0.0020496 secs] 1005775K->1005813K(1048512K), 0.0020782 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
```

### 新生代ParallelGC回收器

采用复制算法，和ParNew基本相同：都是多线程，独占式的收集器。

开启的jvm参数：

- -XX:+UseParallelGC 新生代使用ParallelGC，老年代使用串行回收器
- -XX:+UseParallelOldGC 新生代使用ParallelGC，老年代使用ParallelOldGC回收器

日志格式：

```
[GC (Allocation Failure) [PSYoungGen: 1024K->512K(1024K)] 457957K->457981K(1048064K), 0.0020126 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
```

![1645864889240](D:\code\jvm-in-action\assets\1645864889240.png)

#### 和ParNew的不同点

- 比较关注系统的吞吐量

  提供两个参数用于控制系统的吞吐量。

  - -XX:MaxGCPauseMills 设置最大垃圾收集暂停时间。ParallelGC在工作时会调整堆的大小，让gc的暂停时间控制在MaxGCPauseMills 时间以内。如果用户设置MaxGCPauseMills 的值较小，这意味着实际使用堆的内存更小（小堆回收的快），造成更频繁的垃圾回收，降低了系统的吞吐量
  - -XX:GCTimeRatio 设置吞吐量的大小。假设GCTimeRatio=n，那么系统将花费不大于1/(1+n)的时间在垃圾回收上

- 支持自使用的GC调节策略

  使用-XX:UseAdaptiveSizePolicy可以打开自使用GC策略。

用户线程的停顿时间和吞吐量呈反比，更小的停顿时间往往意味着更低的吞吐量。

## 老年代ParallelOldGC回收器

使用标记压缩算法，工作在老年代。和ParallelGC基本一致：多线程、独占式的、比较关注吞吐量。

![1645864893486](D:\code\jvm-in-action\assets\1645864893486.png)

开启的jvm参数：

- -XX:+UseParallelOldGC 新生代使用ParallelGC，老年代使用ParallelOldGC

  对于关注吞吐量的应用推荐使用该配置



日志格式：

```
[Full GC (Ergonomics) [PSYoungGen: 512K->0K(1024K)] [ParOldGen: 1045236K->1045173K(1047040K)] 1045748K->1045173K(1048064K), [Metaspace: 3787K->3787K(1056768K)], 0.5728228 secs] [Times: user=4.08 sys=0.03, real=0.57 secs] 
9.933
```



### CMS回收器（Concurrent Mark Sweep）

cms回收器主要关注停顿时间，采用并发标记清除算法，工作在老年代。

#### 工作步骤

![1645923871883](D:\code\jvm-in-action\assets\1645923871883.png)

cms的主要工作步骤：初始标记、并发标记、预清理、重新标记、并发清除、并发重置。初始标记和重新标记是独占式的，需要stw。并发标记、预清理、并发清理、并发重置都可以和用户线程同时进行。初始标记、并发标记、重新标记都是标记出可用对象，并发清理是清理垃圾对象，并发重置是初始化cms的数据，为下次的cms做准备。预清理不仅为正式清理做准备和检查。

#### CMS主要的设置参数

启动cms的jvm参数：

- -XX:+UseConcMarkSweepGC 新生代使用ParNew，老年代使用CMS





### -XX:CMSInitiatingOccupancyFraction

该参数指定老年代的内存使用率达到该值后开始执行cms。cms和用户线程并发执行，这要求执行cms时需要由足够的空间让应用程序正常执行。如果该值较大，或者用户的内存使用增长率较高，即使执行cms仍旧无法有足够的空间让应用程序执行，会将cms切换到老年代串行回收器，会造成用户线程更大的停顿。如果该值较小会导致cms频繁执行降低了系统的吞吐量





执行cms后会引起内存碎片，离散的内存无法分配较大的对象，会频繁触发gc。所以cms时需要内存整理

#### cms内存整理参数

- -XX:+UseCMSCompactAtFullCollection cms  cms执行完进行内存整理
- -XX:CMSFullGCsBeforeCompaction 执行多少次cms后进行内存整理



日志格式：

```
[GC (CMS Initial Mark) [1 CMS-initial-mark: 423058K(1047552K)] 423157K(1048512K), 0.0007136 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[GC (Allocation Failure) [ParNew: 958K->64K(960K), 0.0014426 secs] 424016K->423991K(1048512K), 0.0014697 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 960K->64K(960K), 0.0023518 secs] 424887K->424861K(1048512K), 0.0023844 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 959K->64K(960K), 0.0023126 secs] 425757K->425741K(1048512K), 0.0023449 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
12.642
[GC (Allocation Failure) [ParNew: 960K->62K(960K), 0.0026442 secs] 426637K->426622K(1048512K), 0.0026782 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 958K->64K(960K), 0.0024230 secs] 427518K->427503K(1048512K), 0.0024624 secs] [Times: user=0.11 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [ParNew: 960K->64K(960K), 0.0023359 secs] 428399K->428385K(1048512K), 0.0023655 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark: 0.117/0.172 secs] [Times: user=0.42 sys=0.00, real=0.17 secs] 
[CMS-concurrent-preclean-start]
[GC (Allocation Failure) [ParNew: 960K->64K(960K), 0.0019529 secs] 429281K->429267K(1048512K), 0.0019838 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
12.743
[CMS-concurrent-preclean: 0.008/0.010 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
[GC (CMS Final Remark) [YG occupancy: 373 K (960 K)][Rescan (parallel) , 0.0009811 secs][weak refs processing, 0.0000074 secs][class unloading, 0.0002504 secs][scrub symbol table, 0.0003175 secs][scrub string table, 0.0001214 secs][1 CMS-remark: 429203K(1047552K)] 429576K(1048512K), 0.0017286 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-sweep-start]
[CMS-concurrent-reset-start]
```



启动cms回收器失败：

```
[GC (Allocation Failure) [ParNew (promotion failed): 959K->959K(960K), 0.0027069 secs][CMS[CMS-concurrent-mark: 0.372/0.505 secs] [Times: user=0.89 sys=0.02, real=0.50 secs] 
 (concurrent mode failure): 1047118K->1047230K(1047552K), 0.9474883 secs] 1047347K->1047230K(1048512K), [Metaspace: 3792K->3792K(1056768K)], 0.9502490 secs] [Times: user=1.03 sys=0.00, real=0.95 secs] 

```

由于老年代的空间不够导致cms执行失败，需要通过-XX:CMSInitiatingOccupancyFraction设置合理的值



class的回收：

```
[GC (CMS Final Remark) [YG occupancy: 209 K (960 K)][Rescan (parallel) , 0.0011536 secs][weak refs processing, 0.0000106 secs][class unloading, 0.0002824 secs][scrub symbol table, 0.0003627 secs][scrub string table, 0.0001317 secs][1 CMS-remark: 426655K(1047552K)] 426865K(1048512K), 0.0019933 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
```

使用cms可以回收metaspace。涉及参数-XX:+CMSClassUnloadingEnabled

```
[Full GC (Allocation Failure) [CMS: 1047551K->1047551K(1047552K), 0.6135675 secs] 1048511K->1048511K(1048512K), [Metaspace: 3816K->3816K(1056768K)], 0.6136004 secs] [Times: user=0.58 sys=0.03, real=0.61 secs] 
```



### G1回收器









## 有关对象内存分配和回收的一些细节问题

### 禁用System.gc()

System.gc()会触发full gc，同时对新生代和老年代进行回收。垃圾回收需要自动进行，频繁的触发会降低系统的性能。

可以通过 -XX:+DisableExplicitGC禁用System.gc()

#### demo展示

```
		System.gc();
```

开启DisableExplicitGC前：

```
[GC (System.gc()) [PSYoungGen: 11386K->6952K(152576K)] 11386K->6960K(500736K), 0.0042395 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[Full GC (System.gc()) [PSYoungGen: 6952K->0K(152576K)] [ParOldGen: 8K->6776K(348160K)] 6960K->6776K(500736K), [Metaspace: 3145K->3145K(1056768K)], 0.0040788 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (System.gc()) [PSYoungGen: 0K->0K(152576K)] 6776K->6776K(500736K), 0.0005269 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[Full GC (System.gc()) [PSYoungGen: 0K->0K(152576K)] [ParOldGen: 6776K->632K(348160K)] 6776K->632K(500736K), [Metaspace: 3145K->3145K(1056768K)], 0.0043282 secs] [Times: user=0.08 sys=0.02, real=0.00 secs] 
Heap
 PSYoungGen      total 152576K, used 3932K [0x0000000716100000, 0x0000000720b00000, 0x00000007c0000000)
  eden space 131072K, 3% used [0x0000000716100000,0x00000007164d7278,0x000000071e100000)
  from space 21504K, 0% used [0x000000071f600000,0x000000071f600000,0x0000000720b00000)
  to   space 21504K, 0% used [0x000000071e100000,0x000000071e100000,0x000000071f600000)
 ParOldGen       total 348160K, used 632K [0x00000005c2200000, 0x00000005d7600000, 0x0000000716100000)
  object space 348160K, 0% used [0x00000005c2200000,0x00000005c229e098,0x00000005d7600000)
 Metaspace       used 3152K, capacity 4500K, committed 4864K, reserved 1056768K
  class space    used 344K, capacity 388K, committed 512K, reserved 1048576K
```

开启DisableExplicitGC后：

```
Heap
 PSYoungGen      total 152576K, used 14008K [0x0000000716100000, 0x0000000720b00000, 0x00000007c0000000)
  eden space 131072K, 10% used [0x0000000716100000,0x0000000716eae2f0,0x000000071e100000)
  from space 21504K, 0% used [0x000000071f600000,0x000000071f600000,0x0000000720b00000)
  to   space 21504K, 0% used [0x000000071e100000,0x000000071e100000,0x000000071f600000)
 ParOldGen       total 348160K, used 0K [0x00000005c2200000, 0x00000005d7600000, 0x0000000716100000)
  object space 348160K, 0% used [0x00000005c2200000,0x00000005c2200000,0x00000005d7600000)
 Metaspace       used 3223K, capacity 4500K, committed 4864K, reserved 1056768K
  class space    used 349K, capacity 388K, committed 512K, reserved 1048576K
```



### System.gc()使用并发回收

使用System.gc()时会使用传统的Full GC回收整个堆，不会进行并发回收。使用参数-XX:+ExplicitGCInvokesConcurrent开启并发回收

#### demo展示

jvm参数：-XX:+UseConcMarkSweepGC   -XX:+PrintGCDetails 

```
[Full GC (System.gc()) [CMS: 0K->6764K(348160K), 0.0491372 secs] 11714K->6764K(504832K), [Metaspace: 3199K->3199K(1056768K)], 0.0492428 secs] [Times: user=0.03 sys=0.02, real=0.05 secs] 
[Full GC (System.gc()) [CMS: 6764K->620K(348160K), 0.0064134 secs] 9550K->620K(504896K), [Metaspace: 3199K->3199K(1056768K)], 0.0065096 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
Heap
 par new generation   total 156736K, used 5573K [0x00000005c2200000, 0x00000005ccc10000, 0x00000005ebb90000)
  eden space 139328K,   4% used [0x00000005c2200000, 0x00000005c27715f8, 0x00000005caa10000)
  from space 17408K,   0% used [0x00000005caa10000, 0x00000005caa10000, 0x00000005cbb10000)
  to   space 17408K,   0% used [0x00000005cbb10000, 0x00000005cbb10000, 0x00000005ccc10000)
 concurrent mark-sweep generation total 348160K, used 620K [0x00000005ebb90000, 0x0000000600f90000, 0x00000007c0000000)
 Metaspace       used 3219K, capacity 4500K, committed 4864K, reserved 1056768K
  class space    used 348K, capacity 388K, committed 512K, reserved 1048576K

```



jvm参数：-XX:+UseG1GC-XX:+PrintGCDetails 

```
[Full GC (System.gc())  8128K->6785K(24M), 0.0098485 secs]
   [Eden: 2048.0K(24.0M)->0.0B(6144.0K) Survivors: 0.0B->0.0B Heap: 8128.3K(510.0M)->6785.8K(24.0M)], [Metaspace: 3231K->3231K(1056768K)]
 [Times: user=0.00 sys=0.01, real=0.01 secs] 
[Full GC (System.gc())  6785K->641K(8192K), 0.0031502 secs]
   [Eden: 0.0B(6144.0K)->0.0B(2048.0K) Survivors: 0.0B->0.0B Heap: 6785.8K(24.0M)->641.7K(8192.0K)], [Metaspace: 3231K->3231K(1056768K)]
 [Times: user=0.00 sys=0.00, real=0.00 secs] 
Heap
 garbage-first heap   total 8192K, used 641K [0x00000005c2200000, 0x00000005c2400020, 0x00000007c0000000)
  region size 2048K, 1 young (2048K), 0 survivors (0K)
 Metaspace       used 3238K, capacity 4500K, committed 4864K, reserved 1056768K
  class space    used 351K, capacity 388K, committed 512K, reserved 1048576K
```



##### 开启并发回收

jvm参数：-XX:+UseConcMarkSweepGC     -XX:+PrintGCDetails  -XX:+ExplicitGCInvokesConcurrent

```
[GC (System.gc()) [ParNew: 11714K->6860K(156672K), 0.0038108 secs] 11714K->6860K(504832K), 0.0038856 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 0K(348160K)] 9646K(504832K), 0.0004144 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.002/0.002 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
 CMS: abort preclean due to time [CMS-concurrent-abortable-preclean: 0.154/5.031 secs] [Times: user=0.19 sys=0.00, real=5.03 secs] 
[GC (CMS Final Remark) [YG occupancy: 15216 K (156672 K)][Rescan (parallel) , 0.0011609 secs][weak refs processing, 0.0000124 secs][class unloading, 0.0004067 secs][scrub symbol table, 0.0006432 secs][scrub string table, 0.0003079 secs][1 CMS-remark: 0K(348160K)] 15216K(504832K), 0.0026840 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-reset-start]
[GC (System.gc()) [ParNew: 15216K->1248K(156672K), 0.0026601 secs] 15216K->1248K(504832K), 0.0027083 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-reset: 0.069/0.072 secs] [Times: user=0.01 sys=0.08, real=0.07 secs] 
[GC (CMS Initial Mark) [1 CMS-initial-mark: 0K(348160K)] 4034K(504832K), 0.0002575 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-abortable-preclean-start]
 CMS: abort preclean due to time [CMS-concurrent-abortable-preclean: 0.207/5.073 secs] [Times: user=0.30 sys=0.00, real=5.07 secs] 
[GC (CMS Final Remark) [YG occupancy: 4034 K (156672 K)][Rescan (parallel) , 0.0005665 secs][weak refs processing, 0.0000053 secs][class unloading, 0.0002254 secs][scrub symbol table, 0.0003515 secs][scrub string table, 0.0001250 secs][1 CMS-remark: 0K(348160K)] 4034K(504832K), 0.0013327 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[CMS-concurrent-reset-start]
Heap
 par new generation   total 156672K, used 5427K [0x00000005c2200000, 0x00000005ccc00000, 0x00000005ebb90000)
  eden space 139264K,   3% used [0x00000005c2200000, 0x00000005c2614930, 0x00000005caa00000)
  from space 17408K,   7% used [0x00000005caa00000, 0x00000005cab38398, 0x00000005cbb00000)
  to   space 17408K,   0% used [0x00000005cbb00000, 0x00000005cbb00000, 0x00000005ccc00000)
 concurrent mark-sweep generation total 348160K, used 0K [0x00000005ebb90000, 0x0000000600f90000, 0x00000007c0000000)
 Metaspace       used 3742K, capacity 4540K, committed 4864K, reserved 1056768K
  class space    used 410K, capacity 428K, committed 512K, reserved 1048576K

```



jvm参数：-XX:+UseG1GC  -XX:+PrintGCDetails  -XX:+ExplicitGCInvokesConcurrent

```
[GC pause (System.gc()) (young) (initial-mark), 0.0019377 secs]
   [Parallel Time: 1.1 ms, GC Workers: 8]
      [GC Worker Start (ms): Min: 114.2, Avg: 114.3, Max: 114.5, Diff: 0.3]
      [Ext Root Scanning (ms): Min: 0.2, Avg: 0.4, Max: 0.8, Diff: 0.6, Sum: 3.0]
      [Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
         [Processed Buffers: Min: 0, Avg: 0.0, Max: 0, Diff: 0, Sum: 0]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Object Copy (ms): Min: 0.0, Avg: 0.3, Max: 0.4, Diff: 0.4, Sum: 2.3]
      [Termination (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.3]
         [Termination Attempts: Min: 1, Avg: 3.3, Max: 4, Diff: 3, Sum: 26]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.2]
      [GC Worker Total (ms): Min: 0.6, Avg: 0.7, Max: 0.8, Diff: 0.3, Sum: 5.8]
      [GC Worker End (ms): Min: 115.1, Avg: 115.1, Max: 115.1, Diff: 0.0]
   [Code Root Fixup: 0.0 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.2 ms]
   [Other: 0.6 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 0.1 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.3 ms]
      [Humongous Register: 0.0 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.0 ms]
   [Eden: 2048.0K(24.0M)->0.0B(24.0M) Survivors: 0.0B->2048.0K Heap: 8128.3K(510.0M)->6880.0K(510.0M)]
 [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC concurrent-root-region-scan-start]
[GC concurrent-root-region-scan-end, 0.0007052 secs]
[GC concurrent-mark-start]
[GC concurrent-mark-end, 0.0003189 secs]
[GC remark [Finalize Marking, 0.0001460 secs] [GC ref-proc, 0.0002706 secs] [Unloading, 0.0006035 secs], 0.0012386 secs]
 [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC cleanup 7371K->7371K(510M), 0.0010200 secs]
 [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC pause (System.gc()) (young) (initial-mark), 0.0017915 secs]
   [Parallel Time: 0.9 ms, GC Workers: 8]
      [GC Worker Start (ms): Min: 123.6, Avg: 123.7, Max: 123.9, Diff: 0.2]
      [Ext Root Scanning (ms): Min: 0.3, Avg: 0.5, Max: 0.9, Diff: 0.6, Sum: 4.2]
      [Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
         [Processed Buffers: Min: 0, Avg: 0.0, Max: 0, Diff: 0, Sum: 0]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Object Copy (ms): Min: 0.0, Avg: 0.3, Max: 0.5, Diff: 0.5, Sum: 2.0]
      [Termination (ms): Min: 0.0, Avg: 0.0, Max: 0.1, Diff: 0.1, Sum: 0.3]
         [Termination Attempts: Min: 1, Avg: 2.6, Max: 6, Diff: 5, Sum: 21]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [GC Worker Total (ms): Min: 0.7, Avg: 0.8, Max: 0.9, Diff: 0.2, Sum: 6.6]
      [GC Worker End (ms): Min: 124.6, Avg: 124.6, Max: 124.6, Diff: 0.0]
   [Code Root Fixup: 0.0 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.3 ms]
   [Other: 0.5 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 0.3 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.1 ms]
      [Humongous Register: 0.0 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.0 ms]
   [Eden: 2048.0K(24.0M)->0.0B(66.0M) Survivors: 2048.0K->2048.0K Heap: 7371.6K(510.0M)->895.6K(510.0M)]
 [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC concurrent-root-region-scan-start]
[GC concurrent-root-region-scan-end, 0.0008667 secs]
[GC concurrent-mark-start]
[GC concurrent-mark-end, 0.0002904 secs]
[GC remark [Finalize Marking, 0.0001805 secs] [GC ref-proc, 0.0002963 secs] [Unloading, 0.0005534 secs], 0.0014815 secs]
 [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC cleanup 1919K->1919K(510M), 0.0007220 secs]
 [Times: user=0.05 sys=0.00, real=0.00 secs] 
Heap
 garbage-first heap   total 522240K, used 895K [0x00000005c2200000, 0x00000005c24007f8, 0x00000007c0000000)
  region size 2048K, 2 young (4096K), 1 survivors (2048K)
 Metaspace       used 3238K, capacity 4500K, committed 4864K, reserved 1056768K
  class space    used 351K, capacity 388K, committed 512K, reserved 1048576K

```



### 并发GC前额外触发的新生代GC

使用并发回收器（-XX:+UseParallelOldGC、-XX:+UseParallelGC）调用full gc前，会额外调用一次新生代gc。可以通过参数ScavengeBeforeFullGC取消掉新生代gc

jvm参数：-XX:+PrintGCDetails  -XX:+UseParallelOldGC

```
[GC (System.gc()) [PSYoungGen: 5242K->808K(152576K)] 5242K->816K(500736K), 0.0008278 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[Full GC (System.gc()) [PSYoungGen: 808K->0K(152576K)] [ParOldGen: 8K->605K(348160K)] 816K->605K(500736K), [Metaspace: 3111K->3111K(1056768K)], 0.0048237 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
Heap
 PSYoungGen      total 152576K, used 3932K [0x0000000716100000, 0x0000000720b00000, 0x00000007c0000000)
  eden space 131072K, 3% used [0x0000000716100000,0x00000007164d7230,0x000000071e100000)
  from space 21504K, 0% used [0x000000071e100000,0x000000071e100000,0x000000071f600000)
  to   space 21504K, 0% used [0x000000071f600000,0x000000071f600000,0x0000000720b00000)
 ParOldGen       total 348160K, used 605K [0x00000005c2200000, 0x00000005d7600000, 0x0000000716100000)
  object space 348160K, 0% used [0x00000005c2200000,0x00000005c22974f8,0x00000005d7600000)
 Metaspace       used 3124K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 337K, capacity 388K, committed 512K, reserved 1048576K
```

jvm参数：-XX:+PrintGCDetails  -XX:-ScavengeBeforeFullGC -XX:+UseParallelOldGC

```
[Full GC (System.gc()) [PSYoungGen: 5242K->0K(152576K)] [ParOldGen: 0K->607K(348160K)] 5242K->607K(500736K), [Metaspace: 3117K->3117K(1056768K)], 0.0046204 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
Heap
 PSYoungGen      total 152576K, used 3932K [0x0000000716100000, 0x0000000720b00000, 0x00000007c0000000)
  eden space 131072K, 3% used [0x0000000716100000,0x00000007164d7240,0x000000071e100000)
  from space 21504K, 0% used [0x000000071f600000,0x000000071f600000,0x0000000720b00000)
  to   space 21504K, 0% used [0x000000071e100000,0x000000071e100000,0x000000071f600000)
 ParOldGen       total 348160K, used 607K [0x00000005c2200000, 0x00000005d7600000, 0x0000000716100000)
  object space 348160K, 0% used [0x00000005c2200000,0x00000005c2297c10,0x00000005d7600000)
 Metaspace       used 3129K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 338K, capacity 388K, committed 512K, reserved 1048576K
```



### 对象何时进入老年代

正常情况下新创建的对象保存在eden区。



#### 新生对象在eden区

demo展示：

jvm参数：-Xmx64M -Xms64M -XX:+PrintGCDetails

```
public class AllocEden {
    public static final int _1K=1024;
    public static void main(String args[]){
        for(int i=0;i<5*_1K;i++){
            byte[] b=new byte[_1K];
        }
    }
```

结果：

```
Heap
 PSYoungGen      total 18944K, used 7575K [0x00000000feb00000, 0x0000000100000000, 0x0000000100000000)
  eden space 16384K, 46% used [0x00000000feb00000,0x00000000ff265c50,0x00000000ffb00000)
  from space 2560K, 0% used [0x00000000ffd80000,0x00000000ffd80000,0x0000000100000000)
  to   space 2560K, 0% used [0x00000000ffb00000,0x00000000ffb00000,0x00000000ffd80000)
 ParOldGen       total 44032K, used 0K [0x00000000fc000000, 0x00000000feb00000, 0x00000000feb00000)
  object space 44032K, 0% used [0x00000000fc000000,0x00000000fc000000,0x00000000feb00000)
 Metaspace       used 3233K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 350K, capacity 388K, committed 512K, reserved 1048576K
```

可以看出eden已经使用7m多，创建的5m数组对象都在eden。from、老年代已占用0k

#### 老年对象进入老年代

from区中的对象每经过一次gc年龄加1，一般而言，当年龄等于MaxTenuringThreshold时该对象已经成为老对象可以保存至老年代。实际上，即使from区的对象的年龄没有超过MaxTenuringThreshold，但是超过特定年龄的对应对象的总大小超过了size(from区)*TargetSurvivorRatio/100时，会将该年龄及以上的年龄的对象保存至老年代。TargetSurvivorRatio默认为50.

源码：

```cpp
  size_t desired_survivor_size = (size_t)((((double) survivor_capacity)*TargetSurvivorRatio)/100);
  size_t total = 0;
  uint age = 1;
  assert(sizes[0] == 0, "no objects with age zero should be recorded");
  while (age < table_size) {
    total += sizes[age];
    // check if including objects of age 'age' made us pass the desired
    // size, if so 'age' is the new threshold
    if (total > desired_survivor_size) break;
    age++;
  }
  uint result = age < MaxTenuringThreshold ? age : MaxTenuringThreshold;
```

可以看出决定晋升到老年代的年龄result由MaxTenuringThreshold和age中的最小值，



#### 日志说明

demo展示：

```
public class MaxTenuringThreshold {
    public static final int _1M=1024*1024;
    public static final int _1K=1024;
    public static void main(String args[]){
        Map<Integer,byte[]> map=new HashMap<Integer,byte[]>();
        for(int i=0;i<5*_1K;i++){
            byte[] b=new byte[_1K];
            map.put(i, b);
        }
        
        for(int k=0;k<17;k++){
            for(int i=0;i<270;i++){
                byte[] g=new byte[_1M];
            }
        }
    }
}
```

map中保存5m的对象，通过创建g可以让eden满触发minior gc

场景一：年龄查过MaxTenuringThreshold后对象晋升至老年代

jvm参数：-Xmx1024M -Xms1024M -XX:+PrintGCDetails -XX:MaxTenuringThreshold=15 -XX:+PrintHeapAtGC -XX:+UseSerialGC

```
{Heap before GC invocations=0 (full 0):
 def new generation   total 314560K, used 278925K [0x00000000c0000000, 0x00000000d5550000, 0x00000000d5550000)
  eden space 279616K,  99% used [0x00000000c0000000, 0x00000000d10637f8, 0x00000000d1110000)
  from space 34944K,   0% used [0x00000000d1110000, 0x00000000d1110000, 0x00000000d3330000)
  to   space 34944K,   0% used [0x00000000d3330000, 0x00000000d3330000, 0x00000000d5550000)
 tenured generation   total 699072K, used 0K [0x00000000d5550000, 0x0000000100000000, 0x0000000100000000)
   the space 699072K,   0% used [0x00000000d5550000, 0x00000000d5550000, 0x00000000d5550200, 0x0000000100000000)
 Metaspace       used 3234K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 349K, capacity 388K, committed 512K, reserved 1048576K
[GC (Allocation Failure) [DefNew: 278925K->6111K(314560K), 0.0060001 secs] 278925K->6111K(1013632K), 0.0060241 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
Heap after GC invocations=1 (full 0):
 def new generation   total 314560K, used 6111K [0x00000000c0000000, 0x00000000d5550000, 0x00000000d5550000)
  eden space 279616K,   0% used [0x00000000c0000000, 0x00000000c0000000, 0x00000000d1110000)
  from space 34944K,  17% used [0x00000000d3330000, 0x00000000d3927d40, 0x00000000d5550000)
  to   space 34944K,   0% used [0x00000000d1110000, 0x00000000d1110000, 0x00000000d3330000)
 tenured generation   total 699072K, used 0K [0x00000000d5550000, 0x0000000100000000, 0x0000000100000000)
   the space 699072K,   0% used [0x00000000d5550000, 0x00000000d5550000, 0x00000000d5550200, 0x0000000100000000)
 Metaspace       used 3234K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 349K, capacity 388K, committed 512K, reserved 1048576K
}
{Heap before GC invocations=1 (full 0):
 def new generation   total 314560K, used 285003K [0x00000000c0000000, 0x00000000d5550000, 0x00000000d5550000)
  eden space 279616K,  99% used [0x00000000c0000000, 0x00000000d105b160, 0x00000000d1110000)
  from space 34944K,  17% used [0x00000000d3330000, 0x00000000d3927d40, 0x00000000d5550000)
  to   space 34944K,   0% used [0x00000000d1110000, 0x00000000d1110000, 0x00000000d3330000)
 tenured generation   total 699072K, used 0K [0x00000000d5550000, 0x0000000100000000, 0x0000000100000000)
   the space 699072K,   0% used [0x00000000d5550000, 0x00000000d5550000, 0x00000000d5550200, 0x0000000100000000)
 Metaspace       used 3235K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 349K, capacity 388K, committed 512K, reserved 1048576K
[GC (Allocation Failure) [DefNew: 285003K->6104K(314560K), 0.0049598 secs] 285003K->6104K(1013632K), 0.0049805 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
Heap after GC invocations=2 (full 0):
 def new generation   total 314560K, used 6104K [0x00000000c0000000, 0x00000000d5550000, 0x00000000d5550000)
  eden space 279616K,   0% used [0x00000000c0000000, 0x00000000c0000000, 0x00000000d1110000)
  from space 34944K,  17% used [0x00000000d1110000, 0x00000000d17062a8, 0x00000000d3330000)
  to   space 34944K,   0% used [0x00000000d3330000, 0x00000000d3330000, 0x00000000d5550000)
 tenured generation   total 699072K, used 0K [0x00000000d5550000, 0x0000000100000000, 0x0000000100000000)
   the space 699072K,   0% used [0x00000000d5550000, 0x00000000d5550000, 0x00000000d5550200, 0x0000000100000000)
 Metaspace       used 3235K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 349K, capacity 388K, committed 512K, reserved 1048576K
}
{Heap before GC invocations=2 (full 0):
 def new generation   total 314560K, used 285500K [0x00000000c0000000, 0x00000000d5550000, 0x00000000d5550000)
  eden space 279616K,  99% used [0x00000000c0000000, 0x00000000d10d9110, 0x00000000d1110000)
  from space 34944K,  17% used [0x00000000d1110000, 0x00000000d17062a8, 0x00000000d3330000)
  to   space 34944K,   0% used [0x00000000d3330000, 0x00000000d3330000, 0x00000000d5550000)
 tenured generation   total 699072K, used 0K [0x00000000d5550000, 0x0000000100000000, 0x0000000100000000)
   the space 699072K,   0% used [0x00000000d5550000, 0x00000000d5550000, 0x00000000d5550200, 0x0000000100000000)
 Metaspace       used 3235K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 349K, capacity 388K, committed 512K, reserved 1048576K
[GC (Allocation Failure) [DefNew: 285500K->6104K(314560K), 0.0025483 secs] 285500K->6104K(1013632K), 0.0025715 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
Heap after GC invocations=3 (full 0):
 def new generation   total 314560K, used 6104K [0x00000000c0000000, 0x00000000d5550000, 0x00000000d5550000)
  eden space 279616K,   0% used [0x00000000c0000000, 0x00000000c0000000, 0x00000000d1110000)
  from space 34944K,  17% used [0x00000000d3330000, 0x00000000d39261d0, 0x00000000d5550000)
  to   space 34944K,   0% used [0x00000000d1110000, 0x00000000d1110000, 0x00000000d3330000)
 tenured generation   total 699072K, used 0K [0x00000000d5550000, 0x0000000100000000, 0x0000000100000000)
   the space 699072K,   0% used [0x00000000d5550000, 0x00000000d5550000, 0x00000000d5550200, 0x0000000100000000)
 Metaspace       used 3235K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 349K, capacity 388K, committed 512K, reserved 1048576K
}
{Heap before GC invocations=3 (full 0):
 def new generation   total 314560K, used 285014K [0x00000000c0000000, 0x00000000d5550000, 0x00000000d5550000)
  eden space 279616K,  99% used [0x00000000c0000000, 0x00000000d105f908, 0x00000000d1110000)
  from space 34944K,  17% used [0x00000000d3330000, 0x00000000d39261d0, 0x00000000d5550000)
  to   space 34944K,   0% used [0x00000000d1110000, 0x00000000d1110000, 0x00000000d3330000)
 tenured generation   total 699072K, used 0K [0x00000000d5550000, 0x0000000100000000, 0x0000000100000000)
   the space 699072K,   0% used [0x00000000d5550000, 0x00000000d5550000, 0x00000000d5550200, 0x0000000100000000)
 Metaspace       used 3235K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 349K, capacity 388K, committed 512K, reserved 1048576K
[GC (Allocation Failure) [DefNew: 285014K->6104K(314560K), 0.0019652 secs] 285014K->6104K(1013632K), 0.0019889 secs] [Times: user=0.02 sys=0.00, real=0.00 secs] 
Heap after GC invocations=4 (full 0):
 def new generation   total 314560K, used 6104K [0x00000000c0000000, 0x00000000d5550000, 0x00000000d5550000)
  eden space 279616K,   0% used [0x00000000c0000000, 0x00000000c0000000, 0x00000000d1110000)
  from space 34944K,  17% used [0x00000000d1110000, 0x00000000d17061d0, 0x00000000d3330000)
  to   space 34944K,   0% used [0x00000000d3330000, 0x00000000d3330000, 0x00000000d5550000)
 tenured generation   total 699072K, used 0K [0x00000000d5550000, 0x0000000100000000, 0x0000000100000000)
   the space 699072K,   0% used [0x00000000d5550000, 0x00000000d5550000, 0x00000000d5550200, 0x0000000100000000)
 Metaspace       used 3235K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 349K, capacity 388K, committed 512K, reserved 1048576K
}
...
{Heap before GC invocations=15 (full 0):
 def new generation   total 314560K, used 285099K [0x00000000c0000000, 0x00000000d5550000, 0x00000000d5550000)
  eden space 279616K,  99% used [0x00000000c0000000, 0x00000000d1074b20, 0x00000000d1110000)
  from space 34944K,  17% used [0x00000000d3330000, 0x00000000d39261d0, 0x00000000d5550000)
  to   space 34944K,   0% used [0x00000000d1110000, 0x00000000d1110000, 0x00000000d3330000)
 tenured generation   total 699072K, used 0K [0x00000000d5550000, 0x0000000100000000, 0x0000000100000000)
   the space 699072K,   0% used [0x00000000d5550000, 0x00000000d5550000, 0x00000000d5550200, 0x0000000100000000)
 Metaspace       used 3235K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 349K, capacity 388K, committed 512K, reserved 1048576K
[GC (Allocation Failure) [DefNew: 285099K->0K(314560K), 0.0053448 secs] 285099K->6104K(1013632K), 0.0053643 secs] [Times: user=0.00 sys=0.01, real=0.01 secs] 
Heap after GC invocations=16 (full 0):
 def new generation   total 314560K, used 0K [0x00000000c0000000, 0x00000000d5550000, 0x00000000d5550000)
  eden space 279616K,   0% used [0x00000000c0000000, 0x00000000c0000000, 0x00000000d1110000)
  from space 34944K,   0% used [0x00000000d1110000, 0x00000000d1110000, 0x00000000d3330000)
  to   space 34944K,   0% used [0x00000000d3330000, 0x00000000d3330000, 0x00000000d5550000)
 tenured generation   total 699072K, used 6104K [0x00000000d5550000, 0x0000000100000000, 0x0000000100000000)
   the space 699072K,   0% used [0x00000000d5550000, 0x00000000d5b461d0, 0x00000000d5b46200, 0x0000000100000000)
 Metaspace       used 3235K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 349K, capacity 388K, committed 512K, reserved 1048576K
}
Heap
 def new generation   total 314560K, used 267179K [0x00000000c0000000, 0x00000000d5550000, 0x00000000d5550000)
  eden space 279616K,  95% used [0x00000000c0000000, 0x00000000d04ead68, 0x00000000d1110000)
  from space 34944K,   0% used [0x00000000d1110000, 0x00000000d1110000, 0x00000000d3330000)
  to   space 34944K,   0% used [0x00000000d3330000, 0x00000000d3330000, 0x00000000d5550000)
 tenured generation   total 699072K, used 6104K [0x00000000d5550000, 0x0000000100000000, 0x0000000100000000)
   the space 699072K,   0% used [0x00000000d5550000, 0x00000000d5b461d0, 0x00000000d5b46200, 0x0000000100000000)
 Metaspace       used 3241K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 350K, capacity 388K, committed 512K, reserved 1048576K
```

结果：当第16次时from中的对象晋升至老年代



场景二：年龄低于MaxTenuringThreshold的对象晋升至老年代

修改TargetSurvivorRatio为15，默认为50.

jvm参数：-Xmx1024M -Xms1024M -XX:+PrintGCDetails -XX:MaxTenuringThreshold=15 -XX:+PrintHeapAtGC -XX:+UseSerialGC -XX:TargetSurvivorRatio=15

日志：

```
{Heap before GC invocations=0 (full 0):
 def new generation   total 314560K, used 278925K [0x00000000c0000000, 0x00000000d5550000, 0x00000000d5550000)
  eden space 279616K,  99% used [0x00000000c0000000, 0x00000000d10637f8, 0x00000000d1110000)
  from space 34944K,   0% used [0x00000000d1110000, 0x00000000d1110000, 0x00000000d3330000)
  to   space 34944K,   0% used [0x00000000d3330000, 0x00000000d3330000, 0x00000000d5550000)
 tenured generation   total 699072K, used 0K [0x00000000d5550000, 0x0000000100000000, 0x0000000100000000)
   the space 699072K,   0% used [0x00000000d5550000, 0x00000000d5550000, 0x00000000d5550200, 0x0000000100000000)
 Metaspace       used 3234K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 349K, capacity 388K, committed 512K, reserved 1048576K
[GC (Allocation Failure) [DefNew: 278925K->6111K(314560K), 0.0056974 secs] 278925K->6111K(1013632K), 0.0057291 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
Heap after GC invocations=1 (full 0):
 def new generation   total 314560K, used 6111K [0x00000000c0000000, 0x00000000d5550000, 0x00000000d5550000)
  eden space 279616K,   0% used [0x00000000c0000000, 0x00000000c0000000, 0x00000000d1110000)
  from space 34944K,  17% used [0x00000000d3330000, 0x00000000d3927d40, 0x00000000d5550000)
  to   space 34944K,   0% used [0x00000000d1110000, 0x00000000d1110000, 0x00000000d3330000)
 tenured generation   total 699072K, used 0K [0x00000000d5550000, 0x0000000100000000, 0x0000000100000000)
   the space 699072K,   0% used [0x00000000d5550000, 0x00000000d5550000, 0x00000000d5550200, 0x0000000100000000)
 Metaspace       used 3234K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 349K, capacity 388K, committed 512K, reserved 1048576K
}
{Heap before GC invocations=1 (full 0):
 def new generation   total 314560K, used 285003K [0x00000000c0000000, 0x00000000d5550000, 0x00000000d5550000)
  eden space 279616K,  99% used [0x00000000c0000000, 0x00000000d105b160, 0x00000000d1110000)
  from space 34944K,  17% used [0x00000000d3330000, 0x00000000d3927d40, 0x00000000d5550000)
  to   space 34944K,   0% used [0x00000000d1110000, 0x00000000d1110000, 0x00000000d3330000)
 tenured generation   total 699072K, used 0K [0x00000000d5550000, 0x0000000100000000, 0x0000000100000000)
   the space 699072K,   0% used [0x00000000d5550000, 0x00000000d5550000, 0x00000000d5550200, 0x0000000100000000)
 Metaspace       used 3235K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 349K, capacity 388K, committed 512K, reserved 1048576K
[GC (Allocation Failure) [DefNew: 285003K->0K(314560K), 0.0049129 secs] 285003K->6104K(1013632K), 0.0049331 secs] [Times: user=0.02 sys=0.00, real=0.00 secs] 
Heap after GC invocations=2 (full 0):
 def new generation   total 314560K, used 0K [0x00000000c0000000, 0x00000000d5550000, 0x00000000d5550000)
  eden space 279616K,   0% used [0x00000000c0000000, 0x00000000c0000000, 0x00000000d1110000)
  from space 34944K,   0% used [0x00000000d1110000, 0x00000000d1110000, 0x00000000d3330000)
  to   space 34944K,   0% used [0x00000000d3330000, 0x00000000d3330000, 0x00000000d5550000)
 tenured generation   total 699072K, used 6104K [0x00000000d5550000, 0x0000000100000000, 0x0000000100000000)
   the space 699072K,   0% used [0x00000000d5550000, 0x00000000d5b462a8, 0x00000000d5b46400, 0x0000000100000000)
 Metaspace       used 3235K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 349K, capacity 388K, committed 512K, reserved 1048576K
}
......
```

当invocations=2就将from中的对象保存至老年代。

#### 大对象进入老年代

当对象的大小超过了eden、from的大小会直接进入老年代。

![1645943119702](D:\code\jvm-in-action\assets\1645943119702.png)



jvm参数：

- -XX:PretenureSizeThreshold  大小超过该值的对象直接晋升至老年代，单位byte。该参数只对串行和ParNewGC回收器有效，对ParallelGC无效

#### demo展示

```
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
```

场景一：没有禁用TALB

jvm参数：-Xmx32m -Xms32m -XX:+UseSerialGC -XX:+PrintGCDetails -XX:PretenureSizeThreshold=1000 -XX:+UseTLAB

```
Heap
 def new generation   total 9792K, used 7523K [0x00000000fe000000, 0x00000000feaa0000, 0x00000000feaa0000)
  eden space 8704K,  86% used [0x00000000fe000000, 0x00000000fe758cc8, 0x00000000fe880000)
  from space 1088K,   0% used [0x00000000fe880000, 0x00000000fe880000, 0x00000000fe990000)
  to   space 1088K,   0% used [0x00000000fe990000, 0x00000000fe990000, 0x00000000feaa0000)
 tenured generation   total 21888K, used 63K [0x00000000feaa0000, 0x0000000100000000, 0x0000000100000000)
   the space 21888K,   0% used [0x00000000feaa0000, 0x00000000feaafe08, 0x00000000feab0000, 0x0000000100000000)
 Metaspace       used 3227K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 348K, capacity 388K, committed 512K, reserved 1048576K
```

数组对象保存至新生代

场景二：禁用TALB

jvm参数：-Xmx32m -Xms32m -XX:+UseSerialGC -XX:+PrintGCDetails -XX:PretenureSizeThreshold=1000 -XX:-UseTLAB

```
Heap
 def new generation   total 9792K, used 1161K [0x00000000fe000000, 0x00000000feaa0000, 0x00000000feaa0000)
  eden space 8704K,  13% used [0x00000000fe000000, 0x00000000fe122648, 0x00000000fe880000)
  from space 1088K,   0% used [0x00000000fe880000, 0x00000000fe880000, 0x00000000fe990000)
  to   space 1088K,   0% used [0x00000000fe990000, 0x00000000fe990000, 0x00000000feaa0000)
 tenured generation   total 21888K, used 6047K [0x00000000feaa0000, 0x0000000100000000, 0x0000000100000000)
   the space 21888K,  27% used [0x00000000feaa0000, 0x00000000ff087e38, 0x00000000ff088000, 0x0000000100000000)
 Metaspace       used 3241K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 350K, capacity 388K, committed 512K, reserved 1048576K
```

数组对象直接保存至老年代

### 在TLAB上分配对象

堆是线程共享的，不同线程创建新对象时面向着同步的问题。为了减低同步带来的开销，每个线程在eden有一块线程私有的内存区域TLAB（thread local allocate buffer），线程优先将创建出来的新对象保存在TLAB中。

#### demo展示

```
public class UseTLAB {
    public static void alloc(){
        byte[] b=new byte[2];
        b[0]=1;
        
    }
    public static void main(String args[]){
        long b=System.currentTimeMillis();
        for(int i=0;i<10000000;i++){
            alloc();
        }
        long e=System.currentTimeMillis();
        System.out.println(e-b);
    }
}
```



场景一：关闭TLAB

jvm参数：-XX:-UseTLAB  -Xcomp -XX:-BackgroundCompilation -XX:-DoEscapeAnalysis -server

禁用后台编译、禁用栈上分配

结果：

```
172

```

场景二：打开TLAB（默认打开）

jvm参数：-XX:+UseTLAB  -Xcomp -XX:-BackgroundCompilation -XX:-DoEscapeAnalysis -server

禁用后台编译、禁用栈上分配

```
93
```

通过TLAB显著提示创建对象的速度

#### refill_waste

当TLAB剩余的空间小于待创建对象的大小，通过该参数选择是在堆上创建换是创建新的TLAB，在新的TLAB创建该对象。例如：TLAB的空间为100kb，已经使用了80kb，如果假设待创建的对象为30kb，此时有两种选择：

- 将对象创建在堆上

  如果TLAB只剩下1kb，则后续大多数的对象需要在堆上创建

- 废弃调该tlab，重新开辟一块TLAB创建给对象

  开辟TLAB需要同步，频繁创建TLAB需要同步的开销也很大

jvm提供了refill_waste参数进行调节，当待创建的对象大于refill_waste则直接在堆上创建。小于refill_waste重新开辟TLAB创建该对象。在上面的例子中如果refill_waste=25，如果对象大于25kb直接在堆上创建，否则废弃当前TLAB，在新的TLAB创建新对象。



### 对象分配流程



![1645947637500](D:\code\jvm-in-action\assets\1645947637500.png)

对象优先在栈上、TLAB、老年代、Eden中创建对象。





# 性能监控工具



## JDK性能监控工具

lib中的工具之所小这么小是因为主要的程序都在tools包中

![1645948435009](D:\code\jvm-in-action\assets\1645948435009.png)

![1645948387378](D:\code\jvm-in-action\assets\1645948387378.png)

#### 查看java进行-jps

jps类似于ps，会列出java的进程。

```
[root@k8s-node-biyi-test-04 jvm-demo]# jps
26872 CTFFMonitor.jar
21650 Jps
26471 CTFFAgent.jar
```

-q：只列出进程号

```
[root@k8s-node-biyi-test-04 jvm-demo]# jps -q
26872
23291
26471
```

-m：列出java进程的参数

```
[root@k8s-node-biyi-test-04 jvm-demo]# jps -m
26872 CTFFMonitor.jar
24534 Jps -m
26471 CTFFAgent.jar
```

-l：列出主函数的路径

```
[root@k8s-node-biyi-test-04 jvm-demo]# jps -m -l
26872 /data/ctff_agent_linux_64/bin/CTFFMonitor.jar
25136 sun.tools.jps.Jps -m -l
26471 /data/ctff_agent_linux_64/bin/CTFFAgent.jar
```

-v：列出传递给jvm的参数

```
[root@k8s-node-biyi-test-04 jvm-demo]# jps -v
26872 CTFFMonitor.jar -Djava.library.path=/data/ctff_agent_linux_64/bin -Xms64m -Xmx128m -XX:PermSize=64m -XX:MaxPermSize=128m
26471 CTFFAgent.jar -Djava.library.path=/data/ctff_agent_linux_64/bin -Xms64m -Xmx128m -XX:PermSize=64m -XX:MaxPermSize=128m
26756 Jps -Dapplication.home=/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64 -Xms8m
```

#### 查看jvm运行时信息-jstat

jstat列出java应用运行时相关信息

```
 jstat -<option> [-t] [-h<lines>] <vmid> [<interval> [<count>]]
```

-class：显示classloader相关信息

```
[root@k8s-node-biyi-test-04 jvm-demo]# jstat -class -t 26872 1000 2
Timestamp       Loaded  Bytes  Unloaded  Bytes     Time   
     10039054.7    932  1969.0        0     0.0       0.26
     10039055.7    932  1969.0        0     0.0       0.26
```

说明：

- Loaded：加载的类的个数
- Bytes：载入类的大小
- Unloaded：卸载的类的个数
- Bytes：卸载类的大小
- Time：加载和卸载类的总耗时

-compiler：显示JIT编译的相关信息

```
[root@k8s-node-biyi-test-04 jvm-demo]# jstat -compiler -t 26872 1000 2
Timestamp       Compiled Failed Invalid   Time   FailedType FailedMethod
     10039173.4      221      0       0     1.94          0             
     10039174.4      221      0       0     1.94          0         
```

说明：

- Compiled：编译任务执行的次数
- Failed：编译失败的次数
- Invalid：编译不可用的次数
- Time：编译总耗时
- FailedType：最后一次编译失败的类
- FailedMethod：最后一次编译失败的方法

-gc：显示与gc相关的堆信息

```
[root@k8s-node-biyi-test-04 jvm-demo]# jstat -gc -t 26872 1000 2
Timestamp        S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT     GCT   
      4510399.6 2688.0 2688.0  0.0    0.0   16448.0   658.0    43712.0     483.6      -      -      -      -       752    1.664  752    16.397   18.061
      4510400.6 2688.0
```

说明：

- S0C：s0（from）的大小（KB）
- S1C：s1（to）的大小（KB）
- S0U：s0（from）已使用的空间（KB）
- S1U：s1（from）已使用的空间（KB）
- EC：eden区的大小（KB）
- EU：eden区已使用的空间（KB）
- OC：old区的大小（KB）
- OU：old区已使用的空间（KB）
- MC：metaspace的大小（KB）
- MU：metaspace已使用的空间（KB）

- CCSC：压缩类空间大小（KB）
- CCSU：压缩类空间已使用的空间（KB）
- YGC：新生代GC次数
- YGCT：新生代GC耗时
- FGC：Full GC次数
- FGCT：Full GC耗时
- GCT：GC总耗时

 -gccapacity：不仅包含各个代的当前大小，也包含了各个代的最大值和最小值

```
[root@k8s-node-biyi-test-04 jvm-demo]# jstat -gccapacity -t  2762 1000 2
Timestamp        NGCMN    NGCMX     NGC     S0C   S1C       EC      OGCMN      OGCMX       OGC         OC       MCMN     MCMX      MC     CCSMN    CCSMX     CCSC    YGC    FGC 
      4510721.2  21824.0  43648.0  21824.0 2688.0 2688.0  16448.0    43712.0    87424.0    43712.0    43712.0        -        -        -        -        -        -    752   752
      4510722.2  21824.0  43648.0  21824.0 2688.0 2688.0  16448.0    43712.0    87424.0    43712.0    43712.0        -        -        -        -        -        -    752   752
```

说明：

- NGCMN：新生代最小值（KB）
- NGCMX：新生代最大值（KB）
- NGC：当前新生代使用的大小（KB)
- OGCMN：老生代最小值（KB）
- OGCMX：老生代最大值（KB）
- OGC：当前老年代使用的大小（KB)

- MGCMN：metaspace最小值（KB）
- MGCMX：metaspace最大值（KB）
- MGC：当前metaspace使用的大小（KB)

-gccause：显示最近一次、当前GC的原因

```
root@k8s-master-biyi-test-01 openjdk]# jstat -gccause -t  2762 1000 2
Timestamp         S0     S1     E      O      M     CCS    YGC     YGCT    FGC    FGCT     GCT    LGCC                 GCC                 
      4510998.3   0.00   0.00   4.00   1.11      -      -    752    1.664   752   16.397   18.061 System.gc()          No GC               
      4510999.4   0.00   0.00   4.00   1.11      -      -    752    1.664   752   16.397   18.061 System.gc()          No GC        
```

说明：

- LGCC：上次GC的原因
- GCC：当前GC的原因

上次gc的原因是System.gc() ，当前没有gc

-gcnew：查看新生代的详细信息

```
root@k8s-master-biyi-test-01 openjdk]# jstat -gcnew -t  2762 1000 2
Timestamp        S0C    S1C    S0U    S1U   TT MTT  DSS      EC       EU     YGC     YGCT  
      4511168.1 2688.0 2688.0    0.0    0.0  7  15 2688.0  16448.0    658.0    752    1.664
      4511169.1 2688.0 2688.0    0.0    0.0  7  15 2688.0  16448.0    658.0    752    1.664
```

说明：

- TT：新生代晋升的老年代的年龄
- MTT：新生代晋升到老年代的最大年龄
- DSS：所需的survivor区大小

-gcnewcapacity：详细输出新生代各个区的大小信息

```
root@k8s-master-biyi-test-01 openjdk]# jstat -gcnewcapacity -t  2762 1000 2
Timestamp         NGCMN      NGCMX       NGC      S0CMX     S0C     S1CMX     S1C       ECMX        EC      YGC   FGC 
      4511340.5    21824.0    43648.0    21824.0  14528.0   2688.0  14528.0   2688.0    43520.0    16448.0   752   752
      4511341.5    21824.0    43648.0    21824.0  14528.0   2688.0  14528.0   2688.0    43520.0    16448.0   752   752
```

- S0CMX：s0区的最大值（KB）
- S1CMX：s1区的最大值（KB）
- ECMX：eden区的最大值（KB）

-gcold：显示老年代gc的信息

```
root@k8s-master-biyi-test-01 openjdk]# jstat -gcold -t  2762 1000 2
Timestamp          MC       MU      CCSC     CCSU       OC          OU       YGC    FGC    FGCT     GCT   
      4511468.7        -        -        -        -     43712.0       483.6    752   752   16.397   18.061
      4511469.8        -        -        -        -     43712.0       483.6    752   752   16.397   18.061
```

-gcoldcapacity：显示老年代的容量信息

```
root@k8s-master-biyi-test-01 openjdk]# jstat -gcoldcapacity -t  2762 1000 2
Timestamp          OGCMN       OGCMX        OGC         OC       YGC   FGC    FGCT     GCT   
      4511560.9     43712.0     87424.0     43712.0     43712.0   752   752   16.397   18.061
      4511562.0     43712.0     87424.0     43712.0     43712.0   752   752   16.397   18.061
```

-gcutil：显示gc回收相关信息

```
root@k8s-master-biyi-test-01 openjdk]# jstat -gcutil -t  2762 1000 2
Timestamp         S0     S1     E      O      M     CCS    YGC     YGCT    FGC    FGCT     GCT   
      4511633.2   0.00   0.00   4.00   1.11      -      -    752    1.664   752   16.397   18.061
      4511634.3   0.00   0.00   4.00   1.11      -      -    752    1.664   752   16.397   18.061
```

- S0：s0区使用的百分比
- S1：s1区使用的百分比
- E：eden区使用的百分比
- O：old区使用的百分比
- M：metaspace区使用的百分比

#### 查看jvm参数-jinfo

jinfo查看正在运行的java应用的扩展参数，支持少量参数的运行时修改

```
   jinfo [option] <pid>
```



查看新生代对象晋升到老年代对象的最大年龄：

```
[root@k8s-master-biyi-test-01 openjdk]# jinfo -flag MaxTenuringThreshold   2762
-XX:MaxTenuringThreshold=15
```

查看是否打印GC详细信息：

```
[root@k8s-master-biyi-test-01 openjdk]# jinfo -flag MaxTenuringThreshold   2762
-XX:-PrintGCDetails
```

##### 动态修改参数

```
[root@k8s-master-biyi-test-01 openjdk]# jinfo -flag MaxTenuringThreshold   2762
-XX:-PrintGCDetails

[root@k8s-master-biyi-test-01 openjdk]# jinfo -flag  +PrintGCDetails   2762

[root@k8s-master-biyi-test-01 openjdk]# jinfo -flag MaxTenuringThreshold   2762
-XX:+PrintGCDetails
```

#### 导出堆到文件-jmap

jmap可以生产堆的dump文件，可以看查看堆内对象实例的统计信息，查看ClassLoader的信息

查看对象统计信息

```
[root@k8s-master-biyi-test-01 openjdk]# jmap -histo 2762
 num     #instances         #bytes  class name
----------------------------------------------
   1:         12227        1845704  <constMethodKlass>
   2:         12227        1669640  <methodKlass>
   3:           935        1147608  <constantPoolKlass>
   4:         20396         946808  <symbolKlass>
   5:          1011         758320  [I
   6:           797         713864  <constantPoolCacheKlass>
   7:           935         691800  <instanceKlassKlass>
   8:          1947         257608  [B
   9:          3101         232552  [C
  10:           390         197528  <methodDataKlass>
  11:          1047         108888  java.lang.Class
  12:          3001          96032  java.lang.String
  13:          1341          95368  [S
  14:          1458          84224  [[I
  15:             2          65568  [Lcom.alibaba.fastjson.util.IdentityHashMap$Entry;
  16:            83          48472  <objArrayKlassKlass>
...
 385:             1             16  java.lang.reflect.ReflectAccess
 386:             1             16  com.alibaba.fastjson.serializer.CharacterCodec
 387:             1             16  java.lang.System$2
 388:             1             16  java.security.ProtectionDomain$2
 389:             1             16  com.alibaba.fastjson.serializer.AtomicCodec
 390:             1             16  com.alibaba.fastjson.parser.deserializer.NumberDeserializer
 391:             1             16  sun.jkernel.DownloadManager$1
 392:             1             16  com.alibaba.fastjson.parser.deserializer.TimeDeserializer
 393:             1             16  java.util.Hashtable$EmptyEnumerator
 394:             1             16  com.alibaba.fastjson.serializer.IntegerCodec
 395:             1             16  sun.nio.ch.FileDispatcher
 396:             1             16  java.util.Collections$EmptyList
 397:             1             16  java.util.Hashtable$EmptyIterator
Total         67927        9241360
```

导出当前堆快照：

```
[root@k8s-master-biyi-test-01 openjdk]# jmap  -dump:format=b,file=heap.hprof  2762
Dumping heap to /data/zc/openjdk/heap.hprof ...
Heap dump file created
```

打印类加载信息：

```
[root@k8s-master-biyi-test-01 openjdk]# jmap -clstats 2762
```



#### jdk自带的堆分析工具-jhat

jhat可以分析java应用的堆快照内容

```
[root@k8s-master-biyi-test-01 openjdk]# jhat heap.hprof 
Reading from heap.hprof...
Dump file created Sun Feb 27 16:46:30 CST 2022
Snapshot read, resolving...
Resolving 18701 objects...
Chasing references, expect 3 dots...
Eliminating duplicate references...
Snapshot resolved.
Started HTTP server on port 7000
Server is ready.
```

![1645951909580](D:\code\jvm-in-action\assets\1645951909580.png)

#### 查看线程堆栈-jstack

jstack导出java应用程序的线程堆栈。用法：

```
    jstack [-l] <pid>
```

-l：打印锁的附加信息



#### demo展示

死锁demo，两个线程分别占用south锁和north锁

```

public class HoldNetMain {
 
    public static class HoldNetTask implements Runnable {
        public void visitWeb(String strUrl){
            URL url = null;
            URLConnection urlcon = null;
            InputStream is = null;
            try {
                url = new URL(strUrl);
                urlcon = url.openConnection();
                is = urlcon.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
                StringBuffer bs = new StringBuffer();
                String l = null;
                while ((l = buffer.readLine()) != null) {
                    bs.append(l).append("\r\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        @Override
        public void run() {
            while (true) {
                visitWeb("http://www.sina.com.cn");
            }
        }
    }

    public static class LazyTask implements Runnable {
        public void run() {
            try {
                while (true) {
                    Thread.sleep(1000);
                }
            } catch (Exception e) {

            }
        }
    }

    public static void main(String[] args) {
        new Thread(new HoldNetTask()).start();
        new Thread(new LazyTask()).start();
        new Thread(new LazyTask()).start();
        new Thread(new LazyTask()).start();
    }
}


```





```
[root@k8s-node-biyi-test-04 jvm-demo]# jstack -l 1651
2022-02-27 16:59:42
Full thread dump OpenJDK 64-Bit Server VM (24.261-b02 mixed mode):

"Attach Listener" daemon prio=10 tid=0x00007f5a8c001000 nid=0x8d9 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

"DestroyJavaVM" prio=10 tid=0x00007f5b08009000 nid=0x674 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

"Thread-19" prio=10 tid=0x00007f5b08151800 nid=0x69d waiting for monitor entry [0x00007f5ad1b53000]
   java.lang.Thread.State: BLOCKED (on object monitor)
        at HoldLockMain$HoldLockTask.run(HoldLockMain.java:17)
        - waiting to lock <0x00000006b0760018> (a java.lang.Object)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - None

"Thread-18" prio=10 tid=0x00007f5b0814f000 nid=0x69c runnable [0x00007f5ad1c54000]
   java.lang.Thread.State: RUNNABLE
        at java.lang.Object.notifyAll(Native Method)
        at HoldLockMain$HoldLockTask.run(HoldLockMain.java:21)
        - locked <0x00000006b0760018> (a java.lang.Object)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - None

"Thread-17" prio=10 tid=0x00007f5b0814d000 nid=0x69b in Object.wait() [0x00007f5ad1d55000]
   java.lang.Thread.State: WAITING (on object monitor)
        at java.lang.Object.wait(Native Method)
        - waiting on <0x00000006b0760008> (a java.lang.Object)
        at HoldLockMain$HoldLockTask.run(HoldLockMain.java:19)
        - locked <0x00000006b0760008> (a java.lang.Object)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - None

"Thread-16" prio=10 tid=0x00007f5b0814b000 nid=0x69a in Object.wait() [0x00007f5ad1e56000]
   java.lang.Thread.State: WAITING (on object monitor)
        at java.lang.Object.wait(Native Method)
        - waiting on <0x00000006b0760008> (a java.lang.Object)
        at HoldLockMain$HoldLockTask.run(HoldLockMain.java:19)
        - locked <0x00000006b0760008> (a java.lang.Object)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - None

...

"VM Thread" prio=10 tid=0x00007f5b0808b000 nid=0x682 runnable 

"GC task thread#0 (ParallelGC)" prio=10 tid=0x00007f5b0801f000 nid=0x675 runnable 

"GC task thread#1 (ParallelGC)" prio=10 tid=0x00007f5b08021000 nid=0x676 runnable 

"GC task thread#2 (ParallelGC)" prio=10 tid=0x00007f5b08022800 nid=0x677 runnable 

"GC task thread#3 (ParallelGC)" prio=10 tid=0x00007f5b08024800 nid=0x678 runnable 

"GC task thread#4 (ParallelGC)" prio=10 tid=0x00007f5b08026800 nid=0x679 runnable 

"GC task thread#5 (ParallelGC)" prio=10 tid=0x00007f5b08028800 nid=0x67a runnable 

"GC task thread#6 (ParallelGC)" prio=10 tid=0x00007f5b0802a800 nid=0x67b runnable 

"GC task thread#7 (ParallelGC)" prio=10 tid=0x00007f5b0802c800 nid=0x67c runnable 

"GC task thread#8 (ParallelGC)" prio=10 tid=0x00007f5b0802e800 nid=0x67d runnable 

"GC task thread#9 (ParallelGC)" prio=10 tid=0x00007f5b08030000 nid=0x67e runnable 

"GC task thread#10 (ParallelGC)" prio=10 tid=0x00007f5b08032000 nid=0x67f runnable 

"GC task thread#11 (ParallelGC)" prio=10 tid=0x00007f5b08034000 nid=0x680 runnable 

"GC task thread#12 (ParallelGC)" prio=10 tid=0x00007f5b08036000 nid=0x681 runnable 

"VM Periodic Task Thread" prio=10 tid=0x00007f5b080d1000 nid=0x689 waiting on condition 

JNI global references: 109
```



#### 多功能命令行-jcmd

jcmd是多功能的工具，导出堆、查看java线程、导出线程信息、执行gc



-l：列出当前运行的java程序

```
[root@k8s-node-biyi-test-04 jvm-demo]# jcmd -l
26872 /data/ctff_agent_linux_64/bin/CTFFMonitor.jar
1651 HoldLockMain
26471 /data/ctff_agent_linux_64/bin/CTFFAgent.jar
9729 sun.tools.jcmd.JCmd -l
```

help列出所支持的命令

```
[root@k8s-node-biyi-test-04 jvm-demo]# jcmd 1651 help
1651:
The following commands are available:
VM.native_memory
GC.rotate_log
ManagementAgent.stop
ManagementAgent.start_local
ManagementAgent.start
Thread.print
GC.class_histogram
GC.heap_dump
GC.run_finalization
GC.run
VM.uptime
VM.flags
VM.system_properties
VM.command_line
VM.version
help
```

查看jvm启动时间VM.uptime

```
[root@k8s-node-biyi-test-04 jvm-demo]# jcmd 1651 VM.uptime
1651:
394.496 s
```



Thread.print：打印线程栈信息

```
[root@k8s-node-biyi-test-04 jvm-demo]# jcmd 1651 Thread.print
1651:
2022-02-27 17:06:24
Full thread dump OpenJDK 64-Bit Server VM (24.261-b02 mixed mode):

"Attach Listener" daemon prio=10 tid=0x00007f5a8c001000 nid=0x8d9 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"DestroyJavaVM" prio=10 tid=0x00007f5b08009000 nid=0x674 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE
...
```

GC.class_histogram：打印类的统计信息

```
[root@k8s-node-biyi-test-04 jvm-demo]# jcmd 1651 GC.class_histogram
1651:

 num     #instances         #bytes  class name
----------------------------------------------
   1:          6495         990376  <constMethodKlass>
   2:          6495         837280  <methodKlass>
   3:           418         576696  <constantPoolKlass>
   4:           383         317568  <constantPoolCacheKlass>
   5:           418         284576  <instanceKlassKlass>
   6:           585         106896  [B
   7:           955          85840  [C
   8:           481          47528  java.lang.Class
   9:           676          45888  [[I
   ,,,
```



GC.heap_dump：导出堆信息

```
[root@k8s-node-biyi-test-04 jvm-demo]# jcmd 1651 GC.heap_dump help.hprof
1651:
Heap dump file created
```

VM.system_properties：打印系统的所以Propertis

```
[root@k8s-node-biyi-test-04 jvm-demo]# jcmd 1651 VM.system_properties
1651:
#Sun Feb 27 17:09:09 CST 2022
java.runtime.name=OpenJDK Runtime Environment
sun.boot.library.path=/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/amd64
java.vm.version=24.261-b02
java.vm.vendor=Oracle Corporation
java.vendor.url=http\://java.oracle.com/
path.separator=\:
java.vm.name=OpenJDK 64-Bit Server VM
file.encoding.pkg=sun.io
user.country=US
sun.java.launcher=SUN_STANDARD
sun.os.patch.level=unknown
java.vm.specification.name=Java Virtual Machine Specification
user.dir=/data/zc/jvm-demo/ch6
java.runtime.version=1.7.0_261-mockbuild_2020_04_29_08_59-b00
java.awt.graphicsenv=sun.awt.X11GraphicsEnvironment
java.endorsed.dirs=/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/endorsed
os.arch=amd64
java.io.tmpdir=/tmp
line.separator=\n
java.vm.specification.vendor=Oracle Corporation
os.name=Linux
sun.jnu.encoding=UTF-8
java.library.path=/usr/java/packages/lib/amd64\:/usr/lib64\:/lib64\:/lib\:/usr/lib
java.specification.name=Java Platform API Specification
java.class.version=51.0
sun.management.compiler=HotSpot 64-Bit Tiered Compilers
os.version=4.19.9-1.el7.elrepo.x86_64
user.home=/root
user.timezone=
java.awt.printerjob=sun.print.PSPrinterJob
file.encoding=UTF-8
java.specification.version=1.7
user.name=root
java.class.path=.
java.vm.specification.version=1.7
sun.java.command=HoldLockMain
java.home=/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre
sun.arch.data.model=64
user.language=en
java.specification.vendor=Oracle Corporation
awt.toolkit=sun.awt.X11.XToolkit
java.vm.info=mixed mode
java.version=1.7.0_261
java.ext.dirs=/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/ext\:/usr/java/packages/lib/ext
sun.boot.class.path=/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/resources.jar\:/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rt.jar\:/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/sunrsasign.jar\:/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/jsse.jar\:/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/jce.jar\:/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/charsets.jar\:/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/rhino.jar\:/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/jfr.jar\:/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/classes
java.vendor=Oracle Corporation
file.separator=/
java.vendor.url.bug=http\://bugreport.sun.com/bugreport/
sun.io.unicode.encoding=UnicodeLittle
sun.cpu.endian=little
sun.cpu.isalist=
```



VM.flags：获取启动参数

```
[root@k8s-node-biyi-test-04 jvm-demo]# jcmd 1651 VM.flags
1651:
-XX:InitialHeapSize=1055487168 -XX:MaxHeapSize=16888365056 -XX:+UseCompressedOops -XX:+UseParallelGC 
```



PerfCounter.print：获得所有的PerfData数据

```
[root@k8s-node-biyi-test-04 jvm-demo]# jcmd 1651  PerfCounter.print
1651:
java.ci.totalTime=26939
java.cls.loadedClasses=501
java.cls.sharedLoadedClasses=0
java.cls.sharedUnloadedClasses=0
java.cls.unloadedClasses=0
java.property.java.class.path="."
java.property.java.endorsed.dirs="/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/endorsed"
java.property.java.ext.dirs="/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre/lib/ext:/usr/java/packages/lib/ext"
java.property.java.home="/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.261-2.6.22.2.el7_8.x86_64/jre"
java.property.java.library.path="/usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib"
java.property.java.version="1.7.0_261"
java.property.java.vm.info="mixed mode"
java.property.java.vm.name="OpenJDK 64-Bit Server VM"
java.property.java.vm.specification.name="Java Virtual Machine Specification"
java.property.java.vm.specification.vendor="Oracle Corporation"
java.property.java.vm.specification.version="1.7"
java.property.java.vm.vendor="Oracle Corporation"
java.property.java.vm.version="24.261-b02"
java.rt.vmArgs=""
java.rt.vmFlags=""
java.threads.daemon=4
java.threads.live=25
java.threads.livePeak=25
sun.gc.policy.avgSurvivedDev=106496
sun.gc.policy.avgSurvivedPaddedAvg=598016
sun.gc.policy.avgYoungLive=139714
sun.gc.policy.boundaryMoved=0
sun.gc.policy.changeOldGenForMajPauses=0
sun.gc.policy.changeOldGenForMinPauses=0
sun.gc.policy.changeYoungGenForMajPauses=0
sun.gc.policy.changeYoungGenForMinPauses=0
sun.gc.policy.collectors=2
sun.gc.policy.decideAtFullGc=0
sun.gc.policy.decreaseForFootprint=0
sun.gc.policy.decrementTenuringThresholdForGcCost=0
sun.gc.policy.decrementTenuringThresholdForSurvivorLimit=0
sun.gc.policy.desiredSurvivorSize=43515904
sun.gc.policy.edenSize=264765440
sun.gc.policy.freeSpace=529530880
sun.gc.policy.fullFollowsScavenge=0
sun.gc.policy.gcTimeLimitExceeded=0
sun.gc.policy.generations=3
sun.gc.policy.increaseOldGenForThroughput=0
sun.gc.policy.increaseYoungGenForThroughput=0
sun.gc.policy.incrementTenuringThresholdForGcCost=0
...
```



#### 性能统计工具-hprof

hprof是个java agent工具，用于监控java应用程序运行时的cpu信息和堆信息

-agentlib:hprof=help：查看帮助文档

```
[root@k8s-node-biyi-test-04 ch6]# java -agentlib:hprof=help

     HPROF: Heap and CPU Profiling Agent (JVMTI Demonstration Code)

hprof usage: java -agentlib:hprof=[help]|[<option>=<value>, ...]

Option Name and Value  Description                    Default
---------------------  -----------                    -------
heap=dump|sites|all    heap profiling                 all
cpu=samples|times|old  CPU usage                      off
monitor=y|n            monitor contention             n
format=a|b             text(txt) or binary output     a
file=<file>            write data to file             java.hprof[{.txt}]
net=<host>:<port>      send data over a socket        off
depth=<size>           stack trace depth              4
interval=<ms>          sample interval in ms          10
cutoff=<value>         output cutoff point            0.0001
lineno=y|n             line number in traces?         y
thread=y|n             thread in traces?              n
doe=y|n                dump on exit?                  y
msa=y|n                Solaris micro state accounting n
force=y|n              force output to <file>         y
verbose=y|n            print messages about dumps     y
...
```



#### demo展示



```

public class HProfTest {

    public void slowMethod() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void slowerMethod() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void fastMethod() {
        try {
            Thread.yield();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        HProfTest test = new HProfTest();
        test.fastMethod();
        test.slowMethod();
        test.slowerMethod();
    }

}

```



##### 查看cpu的使用情况

```
[root@k8s-node-biyi-test-04 ch6]#  java -agentlib:hprof=cpu=times,interval=10 HProfTest
Dumping CPU usage by timing methods ... done.
```

在当前文件夹下查看java.hprof.txt文件

```
[root@k8s-node-biyi-test-04 ch6]# cat java.hprof.txt 
JAVA PROFILE 1.0.1, created Sun Feb 27 17:21:41 2022

...
--------

THREAD START (obj=5000017e, id = 200002, name="HPROF gc_finish watcher", group="system")
THREAD START (obj=5000017e, id = 200001, name="main", group="main")
THREAD END (id = 200001)
THREAD START (obj=5000017e, id = 200003, name="DestroyJavaVM", group="main")
THREAD END (id = 200003)
TRACE 302120:
        HProfTest.slowerMethod(HProfTest.java:Unknown line)
        HProfTest.main(HProfTest.java:Unknown line)
TRACE 302119:
        HProfTest.slowMethod(HProfTest.java:Unknown line)
        HProfTest.main(HProfTest.java:Unknown line)
TRACE 301553:
        java.lang.ClassLoader.loadLibrary1(ClassLoader.java:Unknown line)
        java.lang.ClassLoader.loadLibrary0(ClassLoader.java:Unknown line)
        java.lang.ClassLoader.loadLibrary(ClassLoader.java:Unknown line)
        java.lang.Runtime.loadLibrary0(Runtime.java:Unknown line)
TRACE 300131:
        java.lang.ClassLoader.findBootstrapClassOrNull(ClassLoader.java:Unknown line)
        java.lang.ClassLoader.loadClass(ClassLoader.java:Unknown line)
        java.lang.ClassLoader.loadClass(ClassLoader.java:Unknown line)
        sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:Unknown line)
TRACE 300312:
        java.io.UnixFileSystem.normalize(UnixFileSystem.java:Unknown line)
        java.io.File.<init>(File.java:Unknown line)
        sun.misc.URLClassPath$JarLoader.<init>(URLClassPath.java:Unknown line)
        sun.misc.URLClassPath$3.run(URLClassPath.java:Unknown line)
TRACE 300724:
        java.util.zip.ZipFile.<init>(ZipFile.java:Unknown line)
        java.util.zip.ZipFile.<init>(ZipFile.java:Unknown line)
        java.util.jar.JarFile.<init>(JarFile.java:Unknown line)
        java.util.jar.JarFile.<init>(JarFile.java:Unknown line)
TRACE 301660:
        sun.nio.fs.UnixFileSystem.<init>(UnixFileSystem.java:Unknown line)
        sun.nio.fs.LinuxFileSystem.<init>(LinuxFileSystem.java:Unknown line)
        sun.nio.fs.LinuxFileSystemProvider.newFileSystem(LinuxFileSystemProvider.java:Unknown line)
        sun.nio.fs.LinuxFileSystemProvider.newFileSystem(LinuxFileSystemProvider.java:Unknown line)
TRACE 301760:
        java.io.FilePermission.<clinit>(FilePermission.java:Unknown line)
        sun.net.www.protocol.file.FileURLConnection.getPermission(FileURLConnection.java:Unknown line)
        java.net.URLClassLoader.getPermissions(URLClassLoader.java:Unknown line)
        sun.misc.Launcher$AppClassLoader.getPermissions(Launcher.java:Unknown line)
CPU TIME (ms) BEGIN (total = 11075) Sun Feb 27 17:21:52 2022
rank   self  accum   count trace method
   1 90.30% 90.30%       1 302120 HProfTest.slowerMethod
   2  9.03% 99.33%       1 302119 HProfTest.slowMethod
   3  0.11% 99.44%       1 301553 java.lang.ClassLoader.loadLibrary1
   4  0.02% 99.46%       8 300131 java.lang.ClassLoader.findBootstrapClassOrNull
   5  0.02% 99.48%       8 300312 java.io.UnixFileSystem.normalize
   6  0.02% 99.49%       2 300724 java.util.zip.ZipFile.<init>
   7  0.02% 99.51%       1 301660 sun.nio.fs.UnixFileSystem.<init>
   8  0.02% 99.53%       1 301760 java.io.FilePermission.<clinit>
CPU TIME (ms) END	
```

##### 查看内存的使用情况

-agentlib:hprof=heap=sites

```
[root@k8s-node-biyi-test-04 ch6]#  java -agentlib:hprof=heap=sites,interval=10 HProfTest
Dumping allocation sites ... done.
```

查看java.hprof.txt文件：

```
[root@k8s-node-biyi-test-04 ch6]# cat java.hprof.txt 
...
TRACE 300198:
        sun.misc.IOUtils.readNBytes(IOUtils.java:176)
        sun.misc.IOUtils.readAllBytes(IOUtils.java:116)
        java.util.jar.JarFile.getBytes(JarFile.java:391)
        java.util.jar.JarFile.hasClassPathAttribute(JarFile.java:496)
TRACE 300023:
        java.util.Arrays.copyOf(Arrays.java:2373)
        java.lang.AbstractStringBuilder.expandCapacity(AbstractStringBuilder.java:130)
        java.lang.AbstractStringBuilder.ensureCapacityInternal(AbstractStringBuilder.java:114)
        java.lang.AbstractStringBuilder.append(AbstractStringBuilder.java:415)        
...        
          percent          live          alloc'ed  stack class
 rank   self  accum     bytes objs     bytes  objs trace name
    1  4.00%  4.00%     16416    2     16416     2 300198 byte[]
    2  2.19%  6.19%      8984   48      8984    48 300023 char[]
    3  0.65%  6.84%      2664   10      2664    10 300052 byte[]
    4  0.43%  7.27%      1752   19      1752    19 300053 byte[]
    5  0.43%  7.69%      1744    9      1744     9 300025 char[]
    6  0.42%  8.11%      1712    9      1712     9 300043 char[]
    7  0.42%  8.53%      1712    9      1712     9 300045 char[]
    8  0.41%  8.93%      1664    8      1664     8 300047 char[]
    9  0.30%  9.23%      1232    5      1232     5 300259 char[]
   10  0.26%  9.49%      1056    2      1056     2 300194 byte[]
   11  0.23%  9.71%       928    3       928     3 300000 java.lang.Thread
   12  0.21%  9.93%       864    1       864     1 300247 byte[]
   13  0.18% 10.11%       744    2       744     2 300005 sun.launcher.LauncherHelper[]
   14  0.18% 10.28%       720    1       720     1 300000 java.lang.Object
   15  0.18% 10.46%       720    1       720     1 300000 java.lang.CharacterData
   16  0.18% 10.63%       720    1       720     1 300000 java.lang.Class
   17  0.18% 10.81%       720    1       720     1 300000 java.lang.Class[]
   18  0.18% 10.98%       720    1       720     1 300000 sun.misc.Launcher$AppClassLoader$1
   19  0.18% 11.16%       720    1       720     1 300000 java.util.Collections$SynchronizedSet
   20  0.18% 11.34%       720    1       720     1 300000 java.lang.Byte
   21  0.18% 11.51%       720    1       720     1 300000 sun.nio.cs.ArrayDecoder
   22  0.18% 11.69%       720    1       720     1 300000 java.lang.IncompatibleClassChangeError
   23  0.18% 11.86%       720    1       720     1 300000 java.util.HashMap$EntrySet
   24  0.18% 12.04%       720    1       720     1 300000 java.io.ExpiringCache$1
   25  0.18% 12.21%       720    1       720     1 300000 java.lang.ThreadLocal
   26  0.18% 12.39%       720    1       720     1 300000 java.lang.StringBuilder
   27  0.18% 12.56%       720    1       720     1 300000 java.lang.ClassCastException
   28  0.18% 12.74%       720    1       720     1 300000 java.nio.ByteOrder
   29  0.18% 12.91%       720    1       720     1 300000 java.nio.charset.CodingErrorAction
   30  0.18% 13.09%       720    1       720     1 300000 sun.reflect.generics.repository.AbstractRepository
   31  0.18% 13.27%       720    1       720     1 300000 java.lang.reflect.Method
   32  0.18% 13.44%       720    1       720     1 300000 sun.misc.Signal
   33  0.18% 13.62%       720    1       720     1 300000 java.nio.Bits
   ...
```

rank1 和rank2的跟踪点为TRACE 300198、 300023，可以在跟踪点出对应的调用栈



# 分析java堆

java可能出现的oom的区域有：堆、直接内存、方法区

### 对症才能下药：找到内存溢出的原因

### 堆溢出

创建大量的强引用对象，即使经过了gc，仍旧无法有足够的空间容纳新对象从而出现OOM

#### demo展示

jvm参数：-Xmx5m

```
public class SimpleHeapOOM {
    public static void main(String args[]){
        ArrayList<byte[]> list=new ArrayList<byte[]>();
        for(int i=0;i<1024;i++){
            list.add(new byte[1024*1024]);
        }
    }
}

```

结果：

```
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
	at com.ctbiyi.jvm.ch7.oom.SimpleHeapOOM.main(SimpleHeapOOM.java:14)
```

解决方案：

- 通过-Xmx分配一个更大的内存
- dump出堆文件分析创建大量对象的代码进行优化

#### 直接内存溢出

java堆和直接内存的受到应用申请的最大内存的限制。



#### 过多线程导致OOM

java中的一个线程对应一个操作系统上的一个线程，需要占用系统的内存资源，类似于直接内存。要想创建更多的线程需要减少堆的空间

demo展示：

```
public class MultiThreadOOM {
    public static class SleepThread implements Runnable{
        public void run(){
            try {
                Thread.sleep(10000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String args[]){
        for(int i=0;i<1500000000;i++){
            new Thread(new SleepThread(),"Thread"+i).start();
            System.out.println("Thread"+i+" created");
        }
    }
}
```

场景1：

jvm参数：-Xmx1g

```
Thread233675 created
Exception in thread "main" java.lang.OutOfMemoryError: unable to create new native thread
	at java.lang.Thread.start0(Native Method)
	at java.lang.Thread.start(Thread.java:717)
	at com.ctbiyi.jvm.ch7.oom.MultiThreadOOM.main(MultiThreadOOM.java:21)
```

场景2：

jvm参数：-Xmx512m

```
Thread319623 created
Exception in thread "main" java.lang.OutOfMemoryError: unable to create new native thread
	at java.lang.Thread.start0(Native Method)
	at java.lang.Thread.start(Thread.java:717)
	at com.ctbiyi.jvm.ch7.oom.MultiThreadOOM.main(MultiThreadOOM.java:21)
```

场景3：

较少Xss

jvm参数：-Xmx10g -Xss128k

```
Thread293675 created
Exception in thread "main" java.lang.OutOfMemoryError: unable to create new native thread
	at java.lang.Thread.start0(Native Method)
	at java.lang.Thread.start(Thread.java:717)
	at com.ctbiyi.jvm.ch7.oom.MultiThreadOOM.main(MultiThreadOOM.java:21)
```





### 元空间溢出

jvm加载的类信息都保存在metaspace，当加载的类过多导致metaspace无法存放metaspace会报OOM

#### demo展示：

jvm参数：-XX:MaxMetaspaceSize=15m

```
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
```

结果：

```
Exception in thread "main" java.lang.IllegalStateException: Unable to load cache item
	at net.sf.cglib.core.internal.LoadingCache.createEntry(LoadingCache.java:79)
	at net.sf.cglib.core.internal.LoadingCache.get(LoadingCache.java:34)
	at net.sf.cglib.core.AbstractClassGenerator$ClassLoaderData.get(AbstractClassGenerator.java:119)
	at net.sf.cglib.core.AbstractClassGenerator.create(AbstractClassGenerator.java:294)
	at net.sf.cglib.beans.BeanMap$Generator.create(BeanMap.java:127)
	at net.sf.cglib.beans.BeanMap.create(BeanMap.java:59)
	at com.ctbiyi.jvm.ch7.oom.DirectBufferOOM$CglibBean.<init>(DirectBufferOOM.java:54)
	at com.ctbiyi.jvm.ch7.oom.MetaspaceOOM.main(MetaspaceOOM.java:16)
Caused by: java.lang.OutOfMemoryError: Metaspace
	at java.lang.Class.forName0(Native Method)
	at java.lang.Class.forName(Class.java:348)
	at net.sf.cglib.core.ReflectUtils.defineClass(ReflectUtils.java:467)
	at net.sf.cglib.core.AbstractClassGenerator.generate(AbstractClassGenerator.java:339)
	at net.sf.cglib.core.AbstractClassGenerator$ClassLoaderData$3.apply(AbstractClassGenerator.java:96)
	at net.sf.cglib.core.AbstractClassGenerator$ClassLoaderData$3.apply(AbstractClassGenerator.java:94)
	at net.sf.cglib.core.internal.LoadingCache$2.call(LoadingCache.java:54)
	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
	at net.sf.cglib.core.internal.LoadingCache.createEntry(LoadingCache.java:61)
	... 7 more
```



##### 如何解决？

- 扩大MaxMetaspaceSize的值
- 减少不必要类的加载

### GC效率低下引起的OOM

当堆空间过小，gc占用过多的系统时间或者每次释放的内存过少，jvm会直接抛出OOM。

触发条件：

- gc所用的时间超过98%
- 老年代释放的内存小于2%
- eden释放的内存小于2%
- 连续5次同时出现上述条件

此时jvm会抛出如下OOM：

```
Exception in thread "main" java.lang.OutOfMemoryError: GC overhead limit exceeded
	at java.lang.Integer.toString(Integer.java:403)
	at java.lang.String.valueOf(String.java:3099)
	at com.ctbiyi.jvm.ch7.string.StringIntern.main(StringIntern.java:22)
```

该OOM只是辅助的，可以通过参数-XX:-UseGCOverheadLimit来禁止这种OOM

## 无处不在的字符串：String在虚拟机中的实现

由于字符串在程序中广泛的使用，java对String也做了优化

#### String的特点

- 不变性

  不变模式的应用。创建字符串后不再允许改变该字符串的内容。在多线程环境下减少了同步的开销。String.substring()其实是返回了新的字符串

- 常量池优化

  当字符串的内容相同时，其都引用常量池中的地址。

  ```
          String s1 = new String("hello"),s2= new String("hello");
          System.out.println(s1 == s2);
          System.out.println(s1 == s2.intern());
          System.out.println("hello"== s2.intern());
          System.out.println(s1.intern()== s2.intern());
  ```

  结果：

  ```
  false
  false
  true
  true
  ```

  ![1645963477305](D:\code\jvm-in-action\assets\1645963477305.png)

  s1和s2引用不同，但是其引用内部指向常量池的引用都相同。避免创建过多相同的字符串浪费内存

- 类final的定义

  保证了String的安全

#### String常量池的位置

常量池保存字符串常量，在java8中常量池位于堆中。



#### demo展示

jvm参数：  -Xmx20m -XX:-UseGCOverheadLimit

```
public class StringIntern {

    public static void main(String[] args) {
        ArrayList<String> al=new ArrayList<String>();
        for(int i=0;i<1024*1024*7;i++){
            al.add(String.valueOf(i).intern());
        }

    }

}
```

结果：

```
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
```

虽然字符串常量都在常量池中，但是经过gc后其引用位置会发送变动

##### demo展示：

场景1：发生gc

```
    public static void main(String[] args) {
        if(args.length==0)return;
        System.out.println(System.identityHashCode((args[0]+Integer.toString(0))));
        System.out.println(System.identityHashCode((args[0]+Integer.toString(0)).intern()));
        System.gc();
        System.out.println(System.identityHashCode((args[0]+Integer.toString(0)).intern()));
     }
```

结果：

```
2133927002
1836019240
325040804
```

场景2：没有gc

```
public class ConstantPool {
    public static void main(String[] args) {
        if(args.length==0)return;
        System.out.println(System.identityHashCode((args[0]+Integer.toString(0))));
        System.out.println(System.identityHashCode((args[0]+Integer.toString(0)).intern()));
//        System.gc();
        System.out.println(System.identityHashCode((args[0]+Integer.toString(0)).intern()));
     }
}

```

结果：

```
2133927002
1836019240
1836019240
```



## 使用MAT分析堆

MAT可以dump出来的堆文件。



### 深堆和浅堆

浅堆：对象占用内存的多少

深堆：只能被该对象直接或者间接访问到的对象的浅堆和

保留集：只能被该对象直接或者间接访问到的对象集合



#### demo展示：

场景一：A对象持有C、D的引用，B对象持有C、E的引用

![1646004994947](D:\code\jvm-in-action\assets\1646004994947.png)

A的浅堆为A所占内存的值

A的深堆为A+C浅堆之和的值

B的深堆为B+E浅堆之和的值

场景二：浅堆的计算方式

```
class Person{
	int age;
	String name;
	boolean sex;
}
```

person占用的浅堆和=12byte（压缩后的对象头）+4（int）+4（string）+1（boolean）+4byte（向8byte对齐填充）=24byte

对象占用内存=对象头+实例数据+对齐填充。

- 对象头
  - 标记部分：包括hashcode、锁标志位、年龄。32位机器上为4byte，64位机器上为8byte
  - 指向类的指针：32位机器为4byte，64位机器为8byte。默认开启压缩后都为4byte
  - 数组长度：数组对象有该部分。32位机器和64位机器都占4byte

- 实例数据

  | 类型    | 长度  |
  | ------- | ----- |
  | byte    | 1byte |
  | boolean | 1byte |
  | short   | 2byte |
  | int     | 4byte |
  | long    | 8byte |
  | float   | 4byte |
  | double  | 8byte |
  | char    | 2byte |

- 对齐部分：jvm要求java对象占用的字节数应该是8byte的整数倍，没有的话要用空白填充

场景三：

- mat中String对象对象的浅堆、深堆

  ![1646006529634](D:\code\jvm-in-action\assets\1646006529634.png)

  String对象的浅堆为24=8（对象头中的标志位）+4（压缩后的执行类的引用）+4（int类型的hash）+4（ref类型的value）+4（对齐填充）=24byte

  String对象的深度为3136=24（string对象浅堆）+3112（char数组对象的浅堆）

- String对象中char数组的浅堆大小

  ![1646006766342](D:\code\jvm-in-action\assets\1646006766342.png)

  char数组的浅堆大小为3112=8（对象头中的MarkWord）+4（对象头中类型引用）+4（对象头中的数组长度）+3096（1548个char所占用的空间）+0（对齐填充）

  

### 例解MAT堆分析



#### demo展示

Student类：保存浏览过的网页

```
public class Student {
    private int id;
    private String name;
    private List<WebPage> history=new Vector<WebPage>();
    
    public Student(int id, String name) {
        super();
        this.id = id;
        this.name = name;
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public void visit(WebPage w){
        history.add(w);
    }
}
```

WebPage：

```
public class WebPage {
    private String url;
    private String content;
    
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String cotent) {
        this.content = cotent;
    }
    
}
```

jvm参数：-XX:+HeapDumpBeforeFullGC -XX:HeapDumpPath=D:/stu.hprof

```
public class TraceStudent {

    static List<WebPage> webpages = new Vector<WebPage>();

    public static void createWebPages() {
        for (int i = 0; i < 20; i++) {
            WebPage wp = new WebPage();
            wp.setUrl("http://www." + Integer.toString(i) + ".com");
            wp.setContent(Integer.toString(i));
            webpages.add(wp);
        }
    }

    public static void main(String[] args) {
        createWebPages();
        Student st3 = new Student(3, "billy");
        Student st5 = new Student(5, "alice");
        Student st7 = new Student(7, "taotao");
        for (int i = 0; i < webpages.size(); i++) {
            if (i % st3.getId() == 0)
                st3.visit(webpages.get(i));
            if (i % st5.getId() == 0)
                st5.visit(webpages.get(i));
            if (i % st7.getId() == 0)
                st7.visit(webpages.get(i));
        }

        webpages.clear();
        System.gc();
    }

}
```

结果：

```
Dumping heap to D:/stu.hprof ...
Heap dump file created [1524789 bytes in 0.010 secs]
```

利用MAT查看导出的hprof文件。

场景一：如何定位到Student对象

![1646010357411](D:\code\jvm-in-action\assets\1646010357411.png)

在显示Thread中找到main线程，可以定位到student变量。

场景二：student为taobao的深堆和浅堆的大小

![1646010616248](D:\code\jvm-in-action\assets\1646010616248.png)

student浅堆24=8（对象头中的MarkWord）+4（压缩后的类指针）+4（int）+4（ref为history）+4（ref为name）=24byte

student深堆=408=24(student浅堆)+name保留集的浅堆和+history保留集的浅堆

##### name保留集的浅堆和

taobao字符串被两个对象引用

![1646014166913](D:\code\jvm-in-action\assets\1646014166913.png)

所以name不在student=taobao的保留集内。

##### history保留集的浅堆和

- history的浅堆![1646014571072](D:\code\jvm-in-action\assets\1646014571072.png)

  history的浅堆和=32=8（对象头中的MarkWord）+4（类对象指针）+4*3（3个int的空间）+4（ref）+4（对齐填充）

- elementData的浅堆和

  ![1646014777847](D:\code\jvm-in-action\assets\1646014777847.png)

  elementData 的浅堆和=56=8（对象头中的MarkWord）+4（对象头中压缩后的类型指针）+4（对象头中数组长度）+4*10（10个ref的大小）

- ref[0]引用的浅堆和

  ![1646015049332](D:\code\jvm-in-action\assets\1646015049332.png)

  ref[0]即WebPage的内容为0被三个student引用，不在student=taobao的保留集内。

- ref[1]引用的浅堆和

  ![1646015257634](D:\code\jvm-in-action\assets\1646015257634.png)

  ![1646015302264](D:\code\jvm-in-action\assets\1646015302264.png)

  webpage(id=7)在student(name=taobao)的保留集中，查看其属性content、url的incoming引用也在webpage(id=7)的保留集内。所以ref[1]引用的浅堆和=webpage(id=7)的retained size=144

- ref[2]引用的浅堆和

  ![1646015555996](D:\code\jvm-in-action\assets\1646015555996.png)

![1646015578045](D:\code\jvm-in-action\assets\1646015578045.png)

webpage(id=14)也在student(name=taobao)的保留集中。查看其属性content、url的incoming引用也在webpage(id=14)的保留集内。所以ref[2]引用的浅堆和=webpage(id=14)的retained size=152

所以，history保留集的浅堆和=32（history的浅堆）+56（elementData的浅堆）+152（webpage(id=14)的深堆和）+144（webpage(id=7)的深堆和）=384

综上：student(name=taobao)的深堆=408=24(student浅堆)+0(name保留集的浅堆和）+384（history保留集的浅堆）





## 支配树（Dominator Tree）

在对象的引用图中达到B的所有引用路径都经过A，则称A支配B。如果对象A是B的最近的一个支配者，则称A为B的直接支配者。

支配树的性质如下：

- 对象A的所有子树（所有被A支配的对象集合）表示对象A的保留集，即深堆
- 如果对象A支配对象B，那么A的直接支配者也支配B
- 支配树的边与对象引用图的边不直接对应

#### demo展示

左图为对象直接的引用关系，右图为其对应的支配图

![1646017723821](D:\code\jvm-in-action\assets\1646017723821.png)

A、B的直接支配者为根节点。A和B都达到C，C的直接支配者为根节点。只有C可以到E，E的直接支配者为C。只有E可以到达H，H的直接支配者为E。只有D可以到达F，F的直接支配者为D。在到达D的所用路径中都经过C，D的直接支配者为。F和H都可以到达G，G的直接根节点为C。



场景一：查看mat中student(name=taobao)的支配树

![1646018280512](D:\code\jvm-in-action\assets\1646018280512.png)

list中只有2个元素：webpage(id=7)，webpage(id=14)。因为webpage(id=0)也会添加到student(name=billy)、student(name=alice)中。





# 锁与并发

锁用于在多线程访问临界资源时保证数据的一致性。

### 对象头和锁

java中锁需要对象头MarkWorld的配合实现。

#### 32位机器中对象的MarkWorld

总共占用4byte

![1646021158096](D:\code\jvm-in-action\assets\1646021158096.png)

- 无锁状态：25bit的hashcode，4bit的分代年龄，1bit是否偏向标志，2bit的锁标志位
- 偏向锁：23bit的线程id，2bit的偏向时间戳，1bit是否偏向标志，2bit的锁标志位
- 轻量级锁：30bit的指向栈中锁记录的指针，2bit的锁标志位
- 重量级锁：30bit的执行monitor的指针，2bit的锁标志位

## 避免残酷的竞争：锁在Jvm中的优化

根据竞争的激烈程度，尽可能在jvm层面解决竞争，尽可能避免重量级锁。

锁优化的常用方法：偏向锁、轻量级锁、自旋锁、锁销除、锁膨胀

#### 偏向锁

在锁没有竞争的场景下，当线程获得锁后该锁进入偏向模式，当该线程再次获取该锁时不需要同步操作而直接获取该锁，如果该锁被其它线程获取则退出偏向模式而升级为轻量级锁

开启偏向锁：-XX:+UseBiasedLocking

当锁进入偏向状态时，会在对象头中的MarkWord中记录偏向的threadId，这样当该线程再次尝试获取锁时可以通过markword中的threadId判断锁是否偏向该线程

![1646025230592](D:\code\jvm-in-action\assets\1646025230592.png)

缺点：当锁竞争激烈时使用偏向锁会造成大量偏向锁的撤销反而降低了系统的性能，可以使用-XX:-UseBiasedLocking关闭偏向锁

#### demo展示

```
public class Biased {
	public static List<Integer> numberList =new Vector<Integer>();
	public static void main(String[] args) throws InterruptedException {
		long begin=System.currentTimeMillis();
		int count=0;
		int startnum=0;
		while(count<10000000){
			numberList.add(startnum);
			startnum+=2;
			count++;
		}
		long end=System.currentTimeMillis();
		System.out.println(end-begin);
	}
}
```

场景一：开启偏向锁

jvm参数：-XX:+UseBiasedLocking -XX:BiasedLockingStartupDelay=0 -client -Xmx512m -Xms512m

- -XX:+UseBiasedLocking 开启偏向锁
- -XX:BiasedLockingStartupDelay=0 设置jvm启动后立马开启偏向锁，默认情况下回延时4s才开启

```
297
```

场景二：关闭偏向锁

jvm参数：-XX:-UseBiasedLocking -XX:BiasedLockingStartupDelay=0 -client -Xmx512m -Xms512m

结果：

```
359
```



#### 轻量级锁

如果偏向锁失败，锁升级为轻量级锁。请求锁的线程通过cas修改对象头的markword指向自己线程的栈记录，修改成功则表明获得了该锁。如果失败则锁会升级为重量级锁

轻量级锁的markword：

![1646043504386](D:\code\jvm-in-action\assets\1646043504386.png)

#### 轻量级锁原理

轻量级锁时通过java栈中的BasicLockObject实现的。BasicLockObject内部包含BasicLock和Object对象，BasicLock通过其成员变量displaced_header保存锁对象原有的markword。object对象指向锁对象。

![1646044071741](D:\code\jvm-in-action\assets\1646044071741.png)

#### 源码

```cpp
void ObjectSynchronizer::slow_enter(Handle obj, BasicLock* lock, TRAPS) {
  	markOop mark = obj->mark();	
    lock->set_displaced_header(mark);
    if (mark == (markOop) Atomic::cmpxchg_ptr(lock, obj()->mark_addr(), mark)) {
      TEVENT (slow_enter: release stacklock) ;
      return ;
    }
      ...
}    
```

将锁对象的markword保存至lock中，然后利用cas将object的markword中的指针修改为lock，修改成功表示获取到了锁。

### 锁膨胀

当轻量级的锁获取失败锁会膨胀为重量级锁。获取对象的objectmonitor并调用其enter方法尝试抢该对象锁。抢锁失败的线程可能会被挂起，需要线程上下文的切换比较耗时。

#### 重量级锁标志

![1646045121601](D:\code\jvm-in-action\assets\1646045121601.png)

#### 源码

```
  // The object header will never be displaced to this lock,
  // so it does not matter what the value is, except that it
  // must be non-zero to avoid looking like a re-entrant lock,
  // and must not look locked either.
  lock->set_displaced_header(markOopDesc::unused_mark());
  ObjectSynchronizer::inflate(THREAD, obj())->enter(THREAD);
```

轻量级锁抢占失败，先将basiclock中的displaced_header废弃调，然后调用inflate方法获得obj对像的ObjectMonitor，调用ObjectMonitor的enter方法尝试抢该对象锁。

### 自旋锁

在objectMonitor的enter方法中，抢锁失败的不会立即被挂起，而是进入自旋状态，执行几次空循环，如果自旋抢到锁了执行正常程序，否则才会将线程挂起。

针对竞争不是很激烈，线程持有锁的时间较短的程序，自旋锁可以提高程序的性能，但是对于锁竞争激烈，线程持有的锁的时间较长，自旋只会白白浪费cpu反而降低了系统的性能。

### 锁销除

JIT编译器通过逃逸分析出代码块不可能发生竞争就消除调代码块中的锁，从而较少了获取锁的时间提高了系统的性能。在jdk自带的Vector、StringBuffer大量使用锁同步方法，如果程序员在误用的情况下可能会在没有线程竞争的环境中使用这些方法。

jvm参数：-XX:+EliminateLocks打开锁消除

#### demo展示

```
public class LockEliminate {
	private static final int CIRCLE = 2000000; 
	public static void main(String args[]) throws InterruptedException {
		long start = System.currentTimeMillis();
		for (int i = 0; i < CIRCLE; i++) {
			craeteStringBuffer("JVM", "Diagnosis");
		}
		long bufferCost = System.currentTimeMillis() - start;
		System.out.println("craeteStringBuffer: " + bufferCost + " ms");
	}

	public static String craeteStringBuffer(String s1, String s2) {
		StringBuffer sb = new StringBuffer();
		sb.append(s1);
		sb.append(s2);
		return sb.toString();
	}
}
```

场景一：关闭锁消除

jvm参数：-server -XX:+DoEscapeAnalysis -XX:-EliminateLocks -Xcomp -XX:-BackgroundCompilation -XX:BiasedLockingStartupDelay=0

```
craeteStringBuffer: 206 ms
```



场景二：打开锁消除

jvm参数：-server -XX:+DoEscapeAnalysis -XX:+EliminateLocks -Xcomp -XX:-BackgroundCompilation -XX:BiasedLockingStartupDelay=0

```
craeteStringBuffer: 165 ms
```

### 锁在应用层面的优化思路

#### 减少锁持有时间

线程持有锁的时间意味着会有更多的线程进入等待状态，内核上下文的切换耗时更多，对锁的竞争更加激烈，也无法利用锁优化中的技术比如自旋带来的好处。在代码层面需要尽可能减少锁持有的时间。可以提供系统的并发能力

假如如下方法中只有method2需要同步，此时使用方法级别的锁显然不是最优选：

```
	public synchronized void hanler() {
		method1();
		method2();
		method3();
	}
```

可以改成如下写法：

```
	public synchronized void hanler() {
		method1();
		synchronized (this) {
			method2();
		}
		method3();
	}
```



##### jdk中也使用了这种优化方案

在正则处理类Pattern中有如下方法：

```
    public Matcher matcher(CharSequence input) {
        if (!compiled) {
            synchronized(this) {
                if (!compiled)
                    compile();
            }
        }
        Matcher m = new Matcher(this, input);
        return m;
    }
```

第一个if判断尽可能的避免方法进入同步块，第二个if减少了同步块的执行时间。

### 减少锁的粒度

减少加锁的范围可以提高系统的并发度。这方面的代表类为ConcurrentHashMap，jdk7中的ConcurrentHashMap分为16个段，每个段都有自己的锁同步段内的操作。比如当需要查询或者插入一个元素时，先根据key计算元素所属的段，获得该段锁执行插入操作。从而将并发度提升16倍。

![1646049646646](D:\code\jvm-in-action\assets\1646049646646.png)

但是当对于需要其它段参与的操作时比如size()时，需要获取每个段的锁，而这需要的时间远远大于对整个map使用一把锁执行size所用的时间。

#### 锁分离

将独占锁分离为多个锁，典型代表为LinkedBlockingQueue中take锁和put锁的分离，可以实现在队尾添加元素和队首删除元素同时进行，提高系统的并发度。

![1646050969772](D:\code\jvm-in-action\assets\1646051134039.png)

```
    /** Lock held by take, poll, etc */
    private final ReentrantLock takeLock = new ReentrantLock();

    /** Wait queue for waiting takes */
    private final Condition notEmpty = takeLock.newCondition();

    /** Lock held by put, offer, etc */
    private final ReentrantLock putLock = new ReentrantLock();

    /** Wait queue for waiting puts */
    private final Condition notFull = putLock.newCondition();
```

### 锁粗化

锁的获取和释放也会比较消耗系统资源，将多次对同一个锁加锁、解锁操作合并为一次的加锁、解锁操作可以降低这种消耗，从而提高系统的并发度。这意味着不能一味的减少锁的持有时间，需要考虑到加锁、解锁本身带来的损耗。

比如下面这种写法虽然减少锁持有的时间，但是造成了大量的加锁、解锁操作：

```
			for(int i=0;i<CIRCLE;i++){
				synchronized(lock) {
				}
			}
```

可以优化这种写法：

```
		synchronized(lock){
			for(int i=0;i<CIRCLE;i++){
				
			}

		}
```



#### demo展示

```
public class LockCoarsen {
	public static Object lock=new Object();
	public static final int CIRCLE=1000000;
	
	public static void main(String[] args) {


			long begintime=System.currentTimeMillis();
			for(int i=0;i<CIRCLE;i++){
				synchronized(lock) {
				}
			}
			long endtime=System.currentTimeMillis();
			System.out.println("sync in loop:"+(endtime-begintime));

		long begintime1=System.currentTimeMillis();
		synchronized(lock){
			for(int i=0;i<CIRCLE;i++){
				
			}

		}
		long endtime1=System.currentTimeMillis();
		System.out.println("sync out loop:"+(endtime1-begintime1));

	}

}
```

结果：

```
sync in loop:22
sync out loop:3
```

### 无锁

也可以通过非阻塞同步的方法实现数据同步。

#### ThreadLocal

threadlocal将每个线程中都保存一个临界对象，即使不加锁可以实现该对象的线程安全

#### CAS（compare and swap）

cas可以实现非阻塞的数据同步。在CAS(V,E,N)函数中，V表示要更新的变量，E表示该变量的期望值，N表示修改的值。当内存中V的值为E时CAS操作成功，修改V的值为N，否则CAS失败。失败后应用层面可以选择重试或者放弃更改。CAS返回V当前的值。不需要加锁也可以实现数据同步。cas是种乐观锁的方案，总是认为可以获得锁成功。在锁竞争不太激烈的场合下比较实用，轻量级锁就使用到cas。

### 原子操作

在j.u.c包下的原子类比如AtomicInteger、AtomLong使用cas实现Integer、Long类型的线程安全操作。

比如在AtomicInteger中的getAndIncrement方法中：

```
    public final long incrementAndGet() {
        return unsafe.getAndAddLong(this, valueOffset, 1L) + 1L;
    }
```

 会在循环中重试确保操作最终成功

```
    public final long getAndAddLong(Object var1, long var2, long var4) {
        long var6;
        do {
            var6 = this.getLongVolatile(var1, var2);
        } while(!this.compareAndSwapLong(var1, var2, var6, var6 + var4));

        return var6;
    }
```

##### demo展示：

```
public class Atomic {
	private static final int MAX_THREADS = 3;					//线程数
	private static final int TASK_COUNT = 3;						//任务数
	private static final int TARGET_COUNT = 1000000;				//目标总数
	
	private AtomicLong acount =new AtomicLong(0L);			//无锁的原子操作
	private LongAdder lacount=new LongAdder();
	private long count=0;
	
	static CountDownLatch cdlsync=new CountDownLatch(TASK_COUNT);
	static CountDownLatch cdlatomic=new CountDownLatch(TASK_COUNT);
	static CountDownLatch cdladdr=new CountDownLatch(TASK_COUNT);
	
	protected synchronized long inc(){							//有锁的加法
		return ++count;
	}
	
	protected synchronized long getCount(){						//有锁的操作
		return count;
	}
	
	public void clearCount(){
		count=0;
	}

	public class SyncThread implements Runnable{
		protected String name;
		protected long starttime;
		Atomic out;										// TestAtomic为当前类名
		public SyncThread(Atomic o,long starttime){
			out=o;
			this.starttime=starttime;
		}
		@Override
		public void run() {
			long v=out.getCount();
			while(v<TARGET_COUNT){						//在到达目标值前，不停循环
				v=out.inc();
			}
			long endtime=System.currentTimeMillis();
			System.out.println("SyncThread spend:"+(endtime-starttime)+"ms"+" v="+v);
			cdlsync.countDown();
		}
	}
	
	public void testSync() throws InterruptedException{
		ExecutorService exe=Executors.newFixedThreadPool(MAX_THREADS);
		long starttime=System.currentTimeMillis();
		SyncThread sync=new SyncThread(this,starttime);
		for(int i=0;i<TASK_COUNT;i++){
			exe.submit(sync); 								//提交线程开始计算
		}
		cdlsync.await();
		exe.shutdown();
	}
	public class AtomicThread implements Runnable{
		protected String name;
		protected long starttime;
		public AtomicThread(long starttime){
			this.starttime=starttime;
		}
		@Override
		public void run() {									//在到达目标值前，不停循环
			long v=acount.get();
			while(v<TARGET_COUNT){
				v=acount.incrementAndGet();					//无锁的加法
			}
			long endtime=System.currentTimeMillis();
			System.out.println("AtomicThread spend:"+(endtime-starttime)+"ms"+" v="+v);
			cdlatomic.countDown();
		}
	}
	
	public void testAtomic() throws InterruptedException{
		ExecutorService exe=Executors.newFixedThreadPool(MAX_THREADS);
		long starttime=System.currentTimeMillis();
		AtomicThread atomic=new AtomicThread(starttime);
		for(int i=0;i<TASK_COUNT;i++){
			exe.submit(atomic);								//提交线程开始计算
		}
		cdlatomic.await();
		exe.shutdown();
	}

	
	public static void main(String args[]) throws InterruptedException{
		Atomic a=new Atomic();
		a.testSync();
		a.testAtomic();
	}
}

```

结果：

```
SyncThread spend:45ms v=1000000
SyncThread spend:45ms v=1000001
SyncThread spend:45ms v=1000002
AtomicThread spend:22ms v=1000000
AtomicThread spend:22ms v=1000002
AtomicThread spend:22ms v=1000001
```

结论：多线程下实现整数加法，cas比加锁方法性能更优

#### 新宠儿LongAdder

AtomicLong会存在由于竞争激烈导致多个线程使用cas循环重试，浪费了cpu资源。可以仿照ConcurrentHashMap的减少锁粒度的思想，在多个分段中对值进行cas操作，计算所有cell中的累积和。

![1646093899168](D:\code\jvm-in-action\assets\1646093899168.png)

#### demo展示

```
public class Atomic {
	private static final int MAX_THREADS = 3;					//线程数
	private static final int TASK_COUNT = 3;						//任务数
	private static final int TARGET_COUNT = 100000000;				//目标总数
	
	private AtomicLong acount =new AtomicLong(0L);			//无锁的原子操作
	private LongAdder lacount=new LongAdder();
	private long count=0;
	
	static CountDownLatch cdlsync=new CountDownLatch(TASK_COUNT);
	static CountDownLatch cdlatomic=new CountDownLatch(TASK_COUNT);
	static CountDownLatch cdladdr=new CountDownLatch(TASK_COUNT);
	
	protected synchronized long inc(){							//有锁的加法
		return ++count;
	}
	
	protected synchronized long getCount(){						//有锁的操作
		return count;
	}
	
	public void clearCount(){
		count=0;
	}

	public class SyncThread implements Runnable{
		protected String name;
		protected long starttime;
		Atomic out;										// TestAtomic为当前类名
		public SyncThread(Atomic o,long starttime){
			out=o;
			this.starttime=starttime;
		}
		@Override
		public void run() {
			long v=out.getCount();
			while(v<TARGET_COUNT){						//在到达目标值前，不停循环
				v=out.inc();
			}
			long endtime=System.currentTimeMillis();
			System.out.println("SyncThread spend:"+(endtime-starttime)+"ms"+" v="+v);
			cdlsync.countDown();
		}
	}
	
	public void testSync() throws InterruptedException{
		ExecutorService exe=Executors.newFixedThreadPool(MAX_THREADS);
		long starttime=System.currentTimeMillis();
		SyncThread sync=new SyncThread(this,starttime);
		for(int i=0;i<TASK_COUNT;i++){
			exe.submit(sync); 								//提交线程开始计算
		}
		cdlsync.await();
		exe.shutdown();
	}
	public class AtomicThread implements Runnable{
		protected String name;
		protected long starttime;
		public AtomicThread(long starttime){
			this.starttime=starttime;
		}
		@Override
		public void run() {									//在到达目标值前，不停循环
			long v=acount.get();
			while(v<TARGET_COUNT){
				v=acount.incrementAndGet();					//无锁的加法
			}
			long endtime=System.currentTimeMillis();
			System.out.println("AtomicThread spend:"+(endtime-starttime)+"ms"+" v="+v);
			cdlatomic.countDown();
		}
	}
	
	public void testAtomic() throws InterruptedException{
		ExecutorService exe=Executors.newFixedThreadPool(MAX_THREADS);
		long starttime=System.currentTimeMillis();
		AtomicThread atomic=new AtomicThread(starttime);
		for(int i=0;i<TASK_COUNT;i++){
			exe.submit(atomic);								//提交线程开始计算
		}
		cdlatomic.await();
		exe.shutdown();
	}

	public class LongAddrThread implements Runnable{
		protected String name;
		protected long starttime;
		public LongAddrThread(long starttime){
			this.starttime=starttime;
		}
		@Override
		public void run() {									
			long v=lacount.sum();
			while(v<TARGET_COUNT){
				lacount.increment();	
				v=lacount.sum();
			}
			long endtime=System.currentTimeMillis();
			System.out.println("LongAdder spend:"+(endtime-starttime)+"ms"+" v="+v);
			cdladdr.countDown();
		}
	}
	
	public void testAtomicLong() throws InterruptedException{
		ExecutorService exe=Executors.newFixedThreadPool(MAX_THREADS);
		long starttime=System.currentTimeMillis();
		LongAddrThread atomic=new LongAddrThread(starttime);
		for(int i=0;i<TASK_COUNT;i++){
			exe.submit(atomic);								//提交线程开始计算
		}
		cdladdr.await();
		exe.shutdown();
	}
	
	public static void main(String args[]) throws InterruptedException{
		Atomic a=new Atomic();
		a.testSync();
		a.testAtomic();
		a.testAtomicLong();
	}
}

```

结果：

```
SyncThread spend:2645ms v=100000001
SyncThread spend:2645ms v=100000000
SyncThread spend:2645ms v=100000002
AtomicThread spend:2156ms v=100000001
AtomicThread spend:2156ms v=100000000
AtomicThread spend:2156ms v=100000002
LongAdder spend:1635ms v=100000001
LongAdder spend:1635ms v=100000001
LongAdder spend:1635ms v=100000002
```



### 将随件变为可控：理解java内存模型

#### 原子性

具有原子性的指令在执行是不可中断的，要么都执行，要么都不执行。原子性的指令没有中间的状态也就避免了数据不一致的情况出现。

java是一种高级语言，语句可能不是原子的，需要多条指令才能完成。比如：

```
        i++;
```

对应的字节码如下：

![1646098300930](D:\code\jvm-in-action\assets\1646098300930.png)

多线程环境下不同的线程读取到不同的指令会导致数据不一致

#### 有序性

cpu为了提高指令的效率可能会对指令进行重排，但是需要确保重排不影响单线程语义。

##### 可以重排

```
a=1;	(1)
flag=true;	(2)
```

重排（1）、（2）后的结果不影响单线程语义

##### 不可以重排的

- 写后读	

  ```
  a=1;
  b=a;
  ```

  不重排的结果为：a和b都为1，重排后的结果：a为1，b为修改前的a。重排造成了单线程的执行结果和原始语义不同，所以在这种场景下不会重排

- 写后写

  ```
  a=1;
  a=2;
  ```

  不重排的结果为：a为2，重排后的结果为：a为1。重排影响了原始语义，所以不会重排

- 读后写

  ```
  b=a;
  a=1;
  ```

  不重排的结果为：b为原始的a值，a为1，重排后的结果：a和b都为1。重排改变了原始语义，所以不会重排

#### demo展示

```
public class SortedDemo {

    private int a=0;
    private boolean flag=false;

    public void write() {
        a=1; // (1)
        flag=true; // (2)
    }

    public void read() {
        if (flag) {
           int i=a+1;
        }
    }

}
```

在writeThread中执行write()，(1)、（2）重排序不影响write()方法的语义。在readThread中执行read()方法，如果发生重排序的话i=0(a为0)+1=1，如果没有重排序的话i=1(a在flag=true之前被赋值为1）+1=2，重排序影响了a的结果，引发数据的不一致。

![1646104388345](D:\code\jvm-in-action\assets\1646104628442.png)

##### 如何解决

同步方法，这样read线程在看到flag=ture时，a=1,保证了数据的一致性。

```
public class SortedDemo {

    private int a=0;
    private boolean flag=false;

    public synchronized void write() {
        a=1;
        flag=true;
    }

    public synchronized void read() {
        if (flag) {
          int i=a+1;
        }
    }


}
```

### 可见性

一个线程中对临界变量的修改能否立马被其它的线程察觉到这中变动。临界变量保存在内存中，cpu读取变量不会直接读取内存，而是读取高速缓存中临界变量的值。cpu写入变量也不会立即写入内存，而是写入到高速缓存中。高速缓存中的值会在合适的时间和内存同步。这个造成了多线程环境下一个线程的写入操作不会立即被其它的线程察觉。

可以通过volatile值解决可见性问题。被volatile修饰的变量不会被缓存到高速缓存中，cpu会直接从内存中读取变量值，修改也会直接写入到内存。

#### demo展示

```
public class VolatileTest {

	private static boolean stop = false;  	//确保stop变量在多线程中可见

	public static class MyThread extends Thread{
		public void run() {  
			int i = 0;  
			while (!stop) { 					//在其他线程中改变stop的值
				i++;  
			}
			System.out.println("Stop Thread");  
		}  
	}

	public static void main(String[] args) throws InterruptedException {
		MyThread t = new MyThread();  
		t.start(); 
		Thread.sleep(1000);
		stop=true;
		Thread.sleep(1000);

	}
}
```

结果：修改stop的值不会使得MyThread停止

##### 如何修改？

为stop添加volatile变量。

```
public class VolatileTest {

	private static volatile boolean stop = false;  	//确保stop变量在多线程中可见

	public static class MyThread extends Thread{
		public void run() {  
			int i = 0;  
			while (!stop) { 					//在其他线程中改变stop的值
				i++;  
			}
			System.out.println("Stop Thread");  
		}  
	}

	public static void main(String[] args) throws InterruptedException {
		MyThread t = new MyThread();  
		t.start(); 
		Thread.sleep(1000);
		stop=true;
		Thread.sleep(1000);

	}
}
```

结果：

```
Stop Thread
```

### Happens-Before原则

happens-before原则规定了不能进行指令重排的语句

- 程序顺序原则：一个线程内







# Class文件结构



通过class文件使得jvm具有夸语言的特性。

### 虚拟机的基石：Class文件

class文件中的数据类型都为无符号整形，包括：u1、u2、u4、u8，表示无符号1字节、2字节、4字节、8字节。

### class文件格式

```
ClassFile{
    u4 magic;
    u2 minor_version;
    u2 major_version;
    u2 constant_pool_count;
    cp_info constant_pool[constant_pool_count-1];
    u2 access_flags;
    u2 this_class;
    u2 super_class;
    u2 interfaces_count;
    u2 interfaces[interfaces_count];
    u2 fields_count;
    field_info fields[fields_count];
    u2 methods_count;
    method_info methods[methods_count];
    u2 attributes_count;
    attribute_info attributes[attributes_count];
}
```

- 文件开头占用4byte的魔数，标识为字节码文件。大小版本号表示该class文件对应的编译版本
- 常量池的长度为constant_pool_count，但是实际包含的个数为constant_pool_count-1
- 该类的修饰符、类名、父类已经父接口
- 类的成员变量个数及其具体信息
- 成员方法个数及其方法信息
- 属性的个数及其属性信息



### Class文件的标志-魔数

java的魔数为0xcafababe。

#### demo展示

```

public class SimpleUser {
	public static final int TYPE = 1;

	private int id;
	private String name;

	public int getId() {
		return id;
	}

	public void setId(int id) throws IllegalStateException {
		try {
			this.id = id;
		} catch (IllegalStateException e) {
			System.out.println(e.toString());
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

```

字节码查看：

![1646128094604](D:\code\jvm-in-action\assets\1646128094604.png)

### Class文件的版本

java大、小版本和jdk的对应关系

| 大版本 | 小版本 | jdk版本 |
| ------ | ------ | ------- |
| 45     | 3      | 1.1     |
| 46     | 0      | 1.2     |
| 47     | 0      | 1.3     |
| 48     | 0      | 1.4     |
| 49     | 0      | 1.5     |
| 50     | 0      | 1.6     |
| 51     | 0      | 1.7     |
| 52     | 0      | 1.8     |

小版本除了jdk1.1之外都为0，jdk1.8的大版本为52.

![1646128975561](D:\code\jvm-in-action\assets\1646128975561.png)

高版本的jvm可以运行低版本jdk编译出的class文件，低版本的jvm运行高版本编译出来的class文件会报错。

jdk7加载jdk8的字节码报如下错：

```
Exception in thread "main" java.lang.UnsupportedClassVersionError: HelloWorld : Unsupported major.minor version 52.0
        at java.lang.ClassLoader.defineClass1(Native Method)
        at java.lang.ClassLoader.defineClass(ClassLoader.java:808)
        at java.security.SecureClassLoader.defineClass(SecureClassLoader.java:142)
        at java.net.URLClassLoader.defineClass(URLClassLoader.java:443)
        at java.net.URLClassLoader.access$100(URLClassLoader.java:65)
        at java.net.URLClassLoader$1.run(URLClassLoader.java:355)
        at java.net.URLClassLoader$1.run(URLClassLoader.java:349)
        at java.security.AccessController.doPrivileged(Native Method)
        at java.net.URLClassLoader.findClass(URLClassLoader.java:348)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:430)
        at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:326)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:363)
        at sun.launcher.LauncherHelper.checkAndLoadMain(LauncherHelper.java:482)
```

### 存放所有常量-常量池

常量池中的数据有自己的数据类型，数据按照类型、长度、内容或者类型、内容的方式保存。常量池通过数组的形式保存数据，索引从1开始。

#### demo展示

![1646134382217](D:\code\jvm-in-action\assets\1646134382217.png)

0x003C的10进制为60，常量池的实际最大索引为60-1=59.

常量池的数据类型为：

| 数据类型                    | TAG  |
| --------------------------- | ---- |
| CONSTANT_Class              | 7    |
| CONSTANT_Methodref          | 10   |
| CONSTANT_String             | 8    |
| CONSTANT_Float              | 4    |
| CONSTANT_Double             | 6    |
| CONSTANT_Utf8               | 1    |
| CONSTANT_MethodType         | 16   |
| CONSTANT_Fieldref           | 9    |
| CONSTANT_InterfaceMethodref | 11   |
| CONSTANT_Integer            | 3    |
| CONSTANT_Long               | 5    |
| CONSTANT_NameAndType        | 12   |
| CONSTANT_MethodHandle       | 15   |
| CONSTANT_InvokeDynamic      | 18   |

tag为1byte

#### CONSTANT_Utf8

```
CONSTANT_Utf8{
    u1 tag;
    u2 length;
    u1 bytes[length];
}
```

tag为1，length为数组的长度，bytes存放内容。



![1646135079469](D:\code\jvm-in-action\assets\1646135079469.png)

01表示数据类型为CONSTANT_Utf8，长度为0004=4byte，保存的字符串常量为TYPE

#### 

#### CONSTANT_Class

```
CONSTANT_Class_Info{
    u1 tag;
    u2 name_index;
}
```

保存类信息。tag为7，name_index表示类名在常量池中的位置（从1开始）

![1646135443835](D:\code\jvm-in-action\assets\1646135443835.png)

07表示数据类型为CONSTANT_Class，0X002B=43，表示类名称在常量池的索引为43。通过javap可以得知43处的数据类型为CONSTANT_Utf8，内容为SimpleUser。

![1646135550292](D:\code\jvm-in-action\assets\1646135550292.png)

#### CONSTANT_Integer、CONSTANT_Float、CONSTANT_Long、CONSTANT_Double

```
CONSTANT_Integer_info{
    u1 tag;
    u4 bytes;
}

CONSTANT_Float_info{
    u1 tag;
    u4 bytes;
}

CONSTANT_Long_info{
    u1 tag;
    u4 high_bytes;
    u4 low_bytes;
}

CONSTANT_Double_info{
    u1 tag;
    u4 high_bytes;
    u4 low_bytes;
}


```

对应的tag分别为：3、4、5、6.CONSTANT_Integer、CONSTANT_Float分别占据4byte，CONSTANT_Long、CONSTANT_Double分别占据8bytes。

![1646136070479](D:\code\jvm-in-action\assets\1646136070479.png)

0x03表示为CONSTANT_Integer，内容为1.





#### CONSTANT_String

```
CONSTANT_String_info{
    u1 tag;
    u2 string_index;
}
```

对应的tag为8，string_index指向常量池对应的索引处，类型为utf8。



#### CONSTANT_NameAndType

```
CONSTANT_NameAndType_Info{
    u1 tag;
    u2 name_index;
    u2 descriptor_index;
}
```

对应的tag为12。name_index指向常量池对应的索引处，类型为utf8，descriptor_index指向常量池对应的索引处，类型为utf8。表示该类型包含名称和对应的类型。

##### java类型在class文件中的表示

| 字符串       | 类型    |
| ------------ | ------- |
| B            | byte    |
| D            | double  |
| I            | int     |
| S            | short   |
| V            | void    |
| [            | 数组    |
| C            | char    |
| F            | float   |
| J            | long    |
| Z            | boolean |
| L类的全路径; | 对象    |

java.lang.Object对应的字节码表示为"Ljava/lang/Object;";String[[]]对应的字节码表示为"[[java/lang/String;"。

![1646136952047](D:\code\jvm-in-action\assets\1646136952047.png)

![1646136990241](D:\code\jvm-in-action\assets\1646136990241.png)

java代码中为：

```
	private int id;
	private String name;
```



#### CONSTANT_Methodref

```
CONSTANT_Methodref_Info{
    u1 tag;
    u2 class_index;
    u2 name_and_type_index;
}
```

对应的tag为10；name_index指向常量池对应的索引处，类型为constant_class；name_and_type_index指向常量池对应的索引处，类型为constant_name_and_type 。

![1646137594742](D:\code\jvm-in-action\assets\1646137594742.png)

0x28=40,0x29=41

 ![1646137647023](D:\code\jvm-in-action\assets\1646137647023.png)



##### System.out.println( String s）在字节码中的表示

![1646138297514](D:\code\jvm-in-action\assets\1646138297514.png)

利用面对对象的思想逐步分解，最终是用utf8类型来表示方法对应的类、名称、参数和返回值。

#### CONSTANT_Fieldref

```
CONSTANT_Fieldref_Info{
    u1 tag;
    u2 class_index;
    u2 name_and_type_index;
}
```

对应的tag为9，类型和CONSTANT_Methodref一致。



java代码：

```
	private String name;
```

![1646138648281](D:\code\jvm-in-action\assets\1646138648281.png)

0x0008=8,0x002a=42.

![1646138799723](D:\code\jvm-in-action\assets\1646138799723.png)

#### CONSTANT_InterfaceMethodref

```
CONSTANT_InterfaceMethodref_Info{
    u1 tag;
    u2 class_index;
    u2 name_and_type_index;
}
```

对应的tag为11，类似于constant_methodref。

### Class的访问标记(Access Flag)

访问标记占用2byte，表示该类的特性，比如是否是class、interface、public等。

##### Access Flag的标识和含义

| 标记名称       | 数值   | 描述                                     |
| -------------- | ------ | ---------------------------------------- |
| ACC_PUBLIC     | 0x0001 | 表示为public类                           |
| ACC_FINAL      | 0x0010 | 表示为final类                            |
| ACC_SUPER      | 0x0020 | 使用增强的方法调用父类方法（默认为true） |
| ACC_INTERFACE  | 0x0200 | 是否为接口                               |
| ACC_ABSTRACT   | 0x0400 | 是否为抽象类                             |
| ACC_SYNTHETIC  | 0x1000 | 由编译器产生的类，没有源码对应           |
| ACC_ANNOTATION | 0x2000 | 是否是注释                               |
| ACC_ENUM       | 0x4000 | 是否是枚举                               |

通过组合的方式表示该类的数据，比如一个类是public，并且是final的，则为ACC_PUBLIC|ACC_FINAL，对应的值为0x0011。

![1646139626667](D:\code\jvm-in-action\assets\1646139626667.png)



#### 当前类、父类和接口

![1646139986400](D:\code\jvm-in-action\assets\1646139986400.png)

this_class、super_class指向常量池中的索引，对应的类型为constant_class。interfaces_count表示接口的个数。然后是长度为interfaces_count的接口数组，接口数组中的值也为常量池中的索引，对应的类型为constant_class。

java代码：

```
public class SimpleUser {}
```

![1646140148789](D:\code\jvm-in-action\assets\1646140148789.png)

0x0008=8,0x0009=9,0x0000=0表示接口的个数为0.常量池索引8、9处的值如下：

![1646140225995](D:\code\jvm-in-action\assets\1646140225995.png)

#### Class文件的字段

![1646178164838](D:\code\jvm-in-action\assets\1646178164838.png)



java代码：

```
	public static final int TYPE = 1;

	private int id;
	private String name;
```

![1646178984040](D:\code\jvm-in-action\assets\1646179001869.png)

0x0003表示fields_count为3.

```
field_info{
    u2 access_flags;
    u2 name_index;
    u2 descriptor_index;
    u2 attributes_count;
    attribute_info attributes[attributes_count];
}
```

access_flag表示字段的访问标识。

| 标记名称      | 数值    | 描述                             |
| ------------- | ------- | -------------------------------- |
| ACC_PUBLIC    | 0X00001 | 字段为public                     |
| ACC_PRIVATE   | 0X0002  | 字段为private                    |
| ACC_PROTECTED | 0X0004  | 字段为protected                  |
| ACC_STATIC    | 0X0008  | 字段为static                     |
| ACC_FINAL     | 0X0010  | 字段为final                      |
| ACC_VOLATILE  | 0X0040  | 字段为volatile                   |
| ACC_TRANSIENT | 0X0080  | 瞬时字段                         |
| ACC_SYNTHENIC | 0X1000  | 由编译器产生的字段，没有源码对应 |
| ACC_ENUM      | 0X4000  | 是否为枚举                       |

不同标记组合表示不同的类型，如ACC_PUBLIC|ACC_STATIC表示该属性为public static

![1646179066240](D:\code\jvm-in-action\assets\1646179066240.png)

0x0019表示访问标识符为：ACC_FINAL|ACC_PUBLIC|ACC_STATIC  

name_index对应常量池中的索引，常量池类型为utf8,

![1646179177478](D:\code\jvm-in-action\assets\1646179177478.png)

![1646179194701](D:\code\jvm-in-action\assets\1646179194701.png)

0x000A=10，对应常量池索引位置10处的utf8，即TYPE

descriptor_index对应常量池中的索引，表示字段的类型，常量池类型为utf8

![1646179252951](D:\code\jvm-in-action\assets\1646179252951.png)

![1646179300942](D:\code\jvm-in-action\assets\1646179300942.png)

0x000b=11，对应常量池中索引位置11处的utf8，即I，表示该字段的类型为int

attributes_count表示attributes的长度

![1646179335683](D:\code\jvm-in-action\assets\1646179335683.png)

0x0001=1,表示attribute_info数组的长度为1

attributes_info表示属性结构。常量属性的结构如下：

```
attributes_info{
    u2 attribute_name_index;
    u4 attribute_length;
    u2 constantvalue_index;
}
```

attribute_name_index对应常量池中的索引，常量恒为ConstantValue

![1646179509795](D:\code\jvm-in-action\assets\1646179509795.png)

![1646179524651](D:\code\jvm-in-action\assets\1646179524651.png)

attribute_length表示剩余属性的长度，不包括attribute_length占用的4字节

![1646179586098](D:\code\jvm-in-action\assets\1646179586098.png)

0x00000002=2表示剩余2byte，常量属性该值恒为0

constantvalue_index表示常量对应的值在常量池中的索引好，

java类型和常量池类型的对应关系

| 字段类型                    | 常量池表项类型   |
| --------------------------- | ---------------- |
| long                        | CONSTANT_Long    |
| float                       | CONSTANT_Float   |
| double                      | CONSTANT_Double  |
| int,short,char,byte,boolean | CONSTANT_Integer |
| String                      | CONSTANT_String  |

![1646179869353](D:\code\jvm-in-action\assets\1646179869353.png)

![1646179880888](D:\code\jvm-in-action\assets\1646179880888.png)

0x000D=13，常量池索引13处的类型为CONSTANT_Integer，值为1

### Class文件的方法基本结构

![1646180577950](D:\code\jvm-in-action\assets\1646180577950.png)

methods_count表示methods的个数

![1646181137957](D:\code\jvm-in-action\assets\1646181167737.png)

method_info的结构：

```
method_info{
    u2 access_flags;
    u2 name_index;
    u2 descriptor_index;
    u2 attibutes_counts;
    attribute_info attributes[attibutes_counts];
}
```

access_flags表示方法的访问标识

| 标记名称         | 值     | 作用                 |
| ---------------- | ------ | -------------------- |
| ACC_PUBLIC       | 0x0001 | 方法为public         |
| ACC_PRIVATE      | 0x0002 | 方法为private        |
| ACC_PROTECTED    | 0x0004 | 方法为protected      |
| ACC_STATIC       | 0x0008 | 静态方法             |
| ACC_FINAL        | 0x0010 | final方法            |
| ACC_SYNCHRONIZED | 0x0020 | synchronized方法     |
| ACC_BRIDGE       | 0x0040 | 由编译产生的桥接方法 |
| ACC_VARARGS      | 0x0080 | 可变参数方法         |
| ACC_NATIVE       | 0x0100 | native方法           |
| ACC_ABSTRACT     | 0x0400 | 抽象方法             |
| ACC_STRICT       | 0x0800 | 浮点模式为FP-strict  |
| ACC_SYNTHETIC    | 0x1000 | 编译器产生的方法     |

通过组合表示不同的类型，如public static 的方法表示为ACC_PUBLIC|ACC_STATIC=0x0009.

![1646181792781](D:\code\jvm-in-action\assets\1646181792781.png)

0x0001对应的标识符为ACC_PUBLIC，该方法为public

name_index

![1646180963316](D:\code\jvm-in-action\assets\1646180963316.png)

表示方法名称在常量池中的索引，常量池类型为utf8.

![1646182037419](D:\code\jvm-in-action\assets\1646182037419.png)

![1646182056620](D:\code\jvm-in-action\assets\1646182056620.png)

descriptor_index表示方法类型在常量池中的索引，常量池类型为utf8.

方法的描述符为：(方法参数列表)返回值。比如：String hello(int a,Double b,Thread c)对应的描述符为(IDLjava.long.Thread;)Ljava.long.String;

![1646182317697](D:\code\jvm-in-action\assets\1646182317697.png)

![1646182330215](D:\code\jvm-in-action\assets\1646182330215.png)

attibutes_counts表示方法中的attibutes的个数

![1646182404322](D:\code\jvm-in-action\assets\1646182404322.png)

0x0002=2，表示setId方法有两个attribute

```
attribute_info{
    u2 attribute_name_index;
    u4 attribute_length;
    u1 info[attribute_length];
}
```

attribute_name_index表示属性名称在常量池中的索引号，常量池类型为utf8。attribute_length表示接下来有attribute_length长度的bytes数组表示属性内容。

| 属性               | 作用                                                  |
| ------------------ | ----------------------------------------------------- |
| ConstantValue      | 表示字段常量                                          |
| Code               | 表示方法的字节码                                      |
| StackMapTable      | code属性的描述属性，用于字节码变量类型验证            |
| Exceptions         | 方法的异常信息                                        |
| SourceFile         | 类文件的属性，表示生成这个类的源码                    |
| LineNumberTable    | code的描述属性，描述行号和字节码的对应关系            |
| LocalVariableTable | code的描述属性，描述函数局部变量                      |
| BootstrapMethods   | 类文件的描述文件，存放类的引导方法。用于invokeDynamic |

### 方法的执行主体-Code属性

code中保存方法的字节码信息

```
Code_attribute{
    u2 attribute_name_index;
    u4 attribute_length;
    u2 max_stack;
    u2 max_locals;
    u4 code_length;
    u1 code[code_length];
    u2 exception_table_length;
    {
        u2 start_pc;
        u2 end_pc;
        u2 handler_pc;
        u2 catch_type;
    } exception_table[exception_table_length];
    u2 attributes_count;
    attribute_info attributes[attributes_count];
}
```

attribute_name_index表示属性名称，指向常量池总的索引，该处固定为Code。



![1646194856540](D:\code\jvm-in-action\assets\1646194856540.png)

![1646194895398](D:\code\jvm-in-action\assets\1646194895398.png)

attribute_length表示接下来有attribute_length长度的byte存放内容。

![1646195027243](D:\code\jvm-in-action\assets\1646195027243.png)

0x00000051=81，

![1646195193413](D:\code\jvm-in-action\assets\1646195193413.png)

max_stack表示最大的操作数栈长度，由于操作数栈一直在变化，只需要提供最大栈长度就行

![1646195341065](D:\code\jvm-in-action\assets\1646195341065.png)

0x0002=2

max_locals表示局部变量表的最大槽位数，一个槽位为4byte，int、float占用一个槽位，long和double占据两个槽位

![1646195508267](D:\code\jvm-in-action\assets\1646195508267.png)

0x0003=3

code_length表示接下来有多少byte保存code内容。

![1646195622983](D:\code\jvm-in-action\assets\1646195622983.png)

0x00000014=20

code[code_length]表示code的内容，即jvm指令。每个jvm指令占用1byte

![1646195690586](D:\code\jvm-in-action\assets\1646195690586.png)

0x2A=42=aload_0

0x1B=27=iload_1

0xB5=181=putfield   0x00,0x02表示putfield操作的值 为0x0002=2，指向常量池中索引为2，常量池类型为Fieldref

![1646196320268](D:\code\jvm-in-action\assets\1646196320268.png)

![1646196255588](D:\code\jvm-in-action\assets\1646196255588.png)

0xA7=167=goto  0x00,0x0e为go具体的位置，即为0x000e=14，（这块和javap中的19不符，不是很理解）

![1646196336104](D:\code\jvm-in-action\assets\1646196336104.png)

![1646197601185](D:\code\jvm-in-action\assets\1646197601185.png)

0x4D=77，对应的指令为astore_2

![1646197848555](D:\code\jvm-in-action\assets\1646197848555.png)

![1646198129959](D:\code\jvm-in-action\assets\1646198129959.png)

0xB2=178，对应的指令为getstatic，获取对象的静态字段。操作的值为(0x00<<1)|0x04=0x0004=4，操作的具体属性在常量池中的索引值为4

![1646197905296](D:\code\jvm-in-action\assets\1646197905296.png)

![1646198072783](D:\code\jvm-in-action\assets\1646198072783.png)

0x2C=44，对应的指令为aload_2，加载局部变量表索引为2处的值到操作数栈，

![1646198239011](D:\code\jvm-in-action\assets\1646198239011.png)

![1646198303361](D:\code\jvm-in-action\assets\1646198303361.png)

0xB6=182对应的指令为invokevirtual，调用实例方法，根据实例的类型进行分派。操作的值为(0x00<8)|0x05=5，表示操作的值在常量池中的索引号为5.

![1646198351565](D:\code\jvm-in-action\assets\1646198351565.png)

![1646198442913](D:\code\jvm-in-action\assets\1646198442913.png)

![1646198466692](D:\code\jvm-in-action\assets\1646198466692.png)

![1646198547370](D:\code\jvm-in-action\assets\1646198547370.png)

0xB6=182对应的指令为invokevirtual，操作值为(0x00<8)|0x06=6，表示操作值在常量池中索引号为6处。

![1646198854002](D:\code\jvm-in-action\assets\1646198854002.png)

![1646198593538](D:\code\jvm-in-action\assets\1646198830405.png)

0xB1=177，对应的指令为return，表示方法返回，当前的栈帧出栈

![1646198924193](D:\code\jvm-in-action\assets\1646198924193.png)

![1646199009603](D:\code\jvm-in-action\assets\1646199009603.png)

![1646199037417](D:\code\jvm-in-action\assets\1646199037417.png)

接下来是方法异常表。

exception_table_length表示异常方法表数组的长度。

![1646199188384](D:\code\jvm-in-action\assets\1646199188384.png)

0x0001=1，表示异常方法表数组的长度为1

exception_table[exception_table_length]保存异常信息，每条异常信息包括:u2 start_pc，u2 end_pc，u2 handler_pc，u2 catch_type。异常处理过程为：从start_pc到end_pc（不包括end_pc）之间的代码如果发送catch_type类型的异常，跳转到handler_pc处执行。start_pc、end_pc和hanlder_pc表示code[code_length]中的偏移量。catch_type表示常量池中的索引号，对应的常量池类型为constant_class。

![1646199310984](D:\code\jvm-in-action\assets\1646199310984.png)

0x0000=0，表示start_pc为0；0x0005=5，表示end_pc为5；0x0008=8表示handler_pc为8；0x0003=3，表示catch_type为3。可以通过start_pc，end_pc和handler_pc的值直接定位到code数组中位置，可以获得对应位置处的指令。

![1646199901588](D:\code\jvm-in-action\assets\1646199901588.png)

### 记录行号-LineNumberTable属性

行号表记录code[code_length]中指令的偏移和源码中的行号之间的对应关系，可以在debug模式根据指令定位到源码的行号。

```
LineNumberTable_attribute{
    u2 attribute_name_index;
    u4 attribute_length;
    u2 line_number_table_length;
    {
        u2 start_pc;
        u2 line_number;
    }line_number_table[line_number_table_length];
}
```

attribute_name_index表示属性名称，对应常量池中的索引号，常量池类型我utf8.此处恒为LineNumberTable

![1646216755863](D:\code\jvm-in-action\assets\1646216755863.png)

0x0014=20，常量池中索引20处为LineNumberTable

![1646216778826](D:\code\jvm-in-action\assets\1646216778826.png)

attribute_length表示之后attribute_length长度为属性内容。

![1646216840948](D:\code\jvm-in-action\assets\1646216840948.png)

0x00000016=22

line_number_table_length表示line_number_table数组的长度

![1646217034127](D:\code\jvm-in-action\assets\1646217034127.png)

0x0005=5表示总共有5个结构为start_pc,line_numer的对象。

line_number_table[line_number_table_length]表示lineNumberTable数组。

![1646216879743](D:\code\jvm-in-action\assets\1646217179283.png)

- 0x0000=0,0x000e=14，表示start_pc为0，line_number为14
- 0x0005=5,0x0011=17，表示start_pc为5，line_number为17
- 0x0008=8,0x000F=，表示start_pc为8，line_number为15
- 0x0009=9,0x0010=16，表示start_pc为9，line_number为16
- 0x0013=19,0x0012=18，表示start_pc为19，line_number为18

![1646217272594](D:\code\jvm-in-action\assets\1646217272594.png)

### 保存局部变量和参数-LocalVariableTable属性

LocalVariableTable中保存局部变量信息

```
LocalVariableTable_attribute{
    u2 attribute_name_index;
    u4 attribute_length;
    u2 local_variable_table_length;
    {
        u2 start_pc;
        u2 length;
        u2 name_index;
        u2 descriptor_index;
        u2 index;
    }local_variable_table[local_variable_table_length];
}
```

attribute_name_index为属性名称在常量池中的索引号，局部变量表属性恒为LocalVariableTable

![1646218563243](D:\code\jvm-in-action\assets\1646218563243.png)

0x0014=20

![1646218588090](D:\code\jvm-in-action\assets\1646218588090.png)

javac  -g:vars  *.java时，使用javap -v才能看到局部变量表的信息

attribute_length表示接下来有attribute_length长度的byte为属性内容。

![1646218607991](D:\code\jvm-in-action\assets\1646218607991.png)

0x0000000020=32

local_variable_table_length表示local_variable_table中元素的个数

![1646218706071](D:\code\jvm-in-action\assets\1646218706071.png)

0x0003=3

local_variable_table[local_variable_table_length]表示local_variable_tables数组.start_pc表示code[code_length]中的位置，length表示从start_pc开始的length个byte，name_index表示属性名称在常量池中的索引，descriptor_index表示属性描述符在常量池中的索引，index表示槽位的索引号

![1646218793736](D:\code\jvm-in-action\assets\1646218793736.png)

- 0x0009=9,0x000A=10，0x001B=27,0x001C=28,0x0002=2，表示start_pc为9，length为10，name_index为27，descriptor_index为28，index为2

  ![1646219385654](D:\code\jvm-in-action\assets\1646219385654.png)

- 0x0000=0,0x0014=20，0x0015=21,0x0016=22,0x0000=0，表示start_pc为0，length为20，name_index为21，descriptor_index为22，index为0

  ![1646219640930](D:\code\jvm-in-action\assets\1646219640930.png)

- 0x0000=0,0x0014=20，0x000E=14,0x000B=11,0x0001=1，表示start_pc为0，length为20，name_index为14，descriptor_index为11，index为0

  ![1646219785357](D:\code\jvm-in-action\assets\1646219785357.png)

  最终如下结果：

![1646219406407](D:\code\jvm-in-action\assets\1646219406407.png)

### 加快字节码校验-StackMapTable属性





### 抛出异常-Exception属性

该属性保存方法可能会抛出的异常

```
Exception_attribute{
    u2 attribute_name_index;
    u4 attribute_length;
    u2 number_of_exceptions;
    u2 exception_index_table[number_of_exceptions];
}
```

attribute_name_index表示属性名在常量池中的索引号，对于Exception属性属性名恒为Exception。

![1646223853276](D:\code\jvm-in-action\assets\1646223853276.png)

0x001F=31

![1646223881298](D:\code\jvm-in-action\assets\1646223881298.png)

attribute_length表示接下来attribute_length个byte为属性内容。

![1646223975562](D:\code\jvm-in-action\assets\1646223975562.png)

0x00000004=4，

number_of_exceptions表示异常的个数

![1646224040713](D:\code\jvm-in-action\assets\1646224040713.png)

0x0001=1

exception_index_table[number_of_exceptions]表示异常数组，每个元素的长度为u2，表示异常类在常量池中的索引，常量池类型为constant_class。

![1646224055648](D:\code\jvm-in-action\assets\1646224055648.png)

0x0003=3

![1646224213567](D:\code\jvm-in-action\assets\1646224213567.png)





### 我来做哪里-SourceFile属性

SourceFile描述class来自于哪个文件

```
SourceFile_attribute{
    u2 attribute_name_index;
    u4 attribute_length;
    u2 sourcefile_index;
}
```

attribute_name_index表示属性名在常量池中的位置，SourceFile属性恒为SourceFile



![1646225677929](D:\code\jvm-in-action\assets\1646225677929.png)

0x0020=32

![1646225696589](D:\code\jvm-in-action\assets\1646225696589.png)

attribute_length表示接下来attribute_length个byte为熟悉内容

![1646225714342](D:\code\jvm-in-action\assets\1646225714342.png)

0x00000002=2

![1646225786404](D:\code\jvm-in-action\assets\1646225786404.png)

0x0021=33

![1646225882919](D:\code\jvm-in-action\assets\1646225882919.png)

### 内部类-InnerClasses属性

InnerClass属性是class文件的属性，描述内部类

```
InnerClasses_attribute{
    u2 attribute_name_index;
    u4 attribute_length;
    u2 number_of_classes;
    {
        u2 inner_class_info_index;
        u2 outer_class_info_index;
        u2 inner_name_index;
        u2 inner_class_access_flag;
    } classes[number_of_classes];
}
```

attribute_name_index表示属性名在常量池中的索引，InnerClasses属性恒为InnerClasses。

java代码：

```
public class SimpleInnerClass {

	public static class In{

	}
}
```

![1646226458420](D:\code\jvm-in-action\assets\1646226458420.png)

0x000E=14，

![1646226484653](D:\code\jvm-in-action\assets\1646226484653.png)

attribute_length表示接下来attribute_length个byte为属性内容

![1646226537616](D:\code\jvm-in-action\assets\1646226537616.png)

0X000A=10

number_of_classes表示内部类的个数

![1646226567670](D:\code\jvm-in-action\assets\1646226567670.png)

0x0001=1

classes[number_of_classes]表示class数组，包含number_of_classes个class信息

inner_class_info_index表示内部类的类型，指向常量池中的索引，常量池的类型为constant_class

![1646226719294](D:\code\jvm-in-action\assets\1646226719294.png)

0x0002=2

![1646226735914](D:\code\jvm-in-action\assets\1646226735914.png)

outer_class_info_index表示外部类的类型，指向常量池中的索引，常量池的类型为constant_class

![1646226783989](D:\code\jvm-in-action\assets\1646226783989.png)

0x000B=11

![1646226802788](D:\code\jvm-in-action\assets\1646226802788.png)

inner_name_index表示内部类的名称，指向常量池中的索引，常量池的类型为constant_utf8.

![1646226872833](D:\code\jvm-in-action\assets\1646226872833.png)

0x000D=13

![1646226892324](D:\code\jvm-in-action\assets\1646226892324.png)

inner_class_access_flag表示内部类的访问修饰符

| 访问标记      | 值     | 含义         |
| ------------- | ------ | ------------ |
| ACC_PUBLIC    | 0x0001 | public类     |
| ACC_PRIVATE   | 0x0002 | private类    |
| ACC_PROTECTED | 0x0004 | protected类  |
| ACC_STATIC    | 0x0008 | static类     |
| ACC_FINAL     | 0x0010 | final类      |
| ACC_INTERFACE | 0x0020 | 接口         |
| ACC_ABSTRACT  | 0x0040 | 抽象类       |
| ACC_SYNTHETIC | 0x1000 | 编译器产生的 |
| ACC_ANNOTAION | 0x2000 | 注释         |
| ACC_ENUM      | 0x4000 | 枚举         |

![1646227137328](D:\code\jvm-in-action\assets\1646227137328.png)

0x0009=9，其值为ACC_STATIC|ACC_PUBLIC



































































































































































