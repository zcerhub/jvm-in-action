package com.ctbiyi.jvm.ch7.oom;

import com.ctbiyi.jvm.BeanGeneratorObj;
import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.beans.BeanMap;
import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * -Xmx512m -XX:+PrintGCDetails  ok  
 * -Xmx1g -XX:+PrintGCDetails    OOM 强制GC可以
 * DirectBuffer到达-XX:MaxDirectMemorySize之前，
 * java不会由DirectBuffer来触发GC，但是在GC时会回收DirectBuffer
 * @author Geym
 *
 */
public class DirectBufferOOM {
    public static void main(String args[]){
//        List<ByteBuffer> list = new ArrayList<>();
        for(int i=0;i<1024*10;i++){
            ByteBuffer b=ByteBuffer.allocateDirect(1024*1024*5);
//            list.add(b);
            System.out.println(i);
//            System.gc();
        }
    }

    public static class CglibBean {
        /**
         * 实体Object
         */
        public Object object = null;

        /**
         * 属性map
         */
        public BeanMap beanMap = null;

        public CglibBean() {
            super();
        }

        @SuppressWarnings("unchecked")
        public CglibBean(Map propertyMap) {
            this.object = generateBean(propertyMap);
            this.beanMap = BeanMap.create(this.object);
        }

        public CglibBean(String className,Map propertyMap) {
            this.object = generateBean(className,propertyMap);
            this.beanMap = BeanMap.create(this.object);
        }

        /**
         * 给bean属性赋值
         *
         * @param property
         *            属性名
         * @param value
         *            值
         */
        public void setValue(String property, Object value) {
            beanMap.put(property, value);
        }

        /**
         * 通过属性名得到属性值
         *
         * @param property
         *            属性名
         * @return 值
         */
        public Object getValue(String property) {
            return beanMap.get(property);
        }

        /**
         * 得到该实体bean对象
         *
         * @return
         */
        public Object getObject() {
            return this.object;
        }

        private Object generateBean(Map propertyMap) {
            BeanGenerator generator = new BeanGenerator();
            Set keySet = propertyMap.keySet();
            for (Iterator i = keySet.iterator(); i.hasNext();) {
                String key = (String) i.next();
                generator.addProperty(key, (Class) propertyMap.get(key));
            }
            return generator.create();
        }

        private Object generateBean(final String className,Map propertyMap) {
            BeanGeneratorObj generator = new BeanGeneratorObj();
            generator.setUseCache(false);
            generator.setNamingPolicy(new NamingPolicy() {
                @Override
                public String getClassName(String prefix, String source, Object key, Predicate names) {
                    return className;
                }
            });

            Set keySet = propertyMap.keySet();
            for (Iterator i = keySet.iterator(); i.hasNext();) {
                String key = (String) i.next();
                generator.addProperty(key, (Class) propertyMap.get(key));
            }
            return generator.create();
        }
    }
}
