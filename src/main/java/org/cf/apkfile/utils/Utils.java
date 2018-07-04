package org.cf.apkfile.utils;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.TObjectLongMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Utils {

    public static Map<Object, Object> convertToJava(TObjectIntMap map) {
        Map<Object, Object> javaMap = new HashMap<>();
        for (Object key : map.keySet()) {
            javaMap.put(key, map.get(key));
        }
        return javaMap;
    }

    public static int leToBeI(int num) {
        return ((num << 24) & 0xff000000) | ((num << 8) & 0x00ff0000) | ((num >> 8) & 0x0000ff00) | ((num >> 24) & 0x000000ff);
    }

    public static short leToBeS(short num) {
        return (short) (((num >> 8) & 0x00ff) | ((num << 8) & 0xff00));
    }

    public static <E> Collection<E> makeCollection(Iterable<E> iter) {
        Collection<E> list = new LinkedList<E>();
        for (E item : iter) {
            list.add(item);
        }
        return list;
    }

    public static void rollUp(TIntIntMap dest, TIntIntMap src) {
        for (int key : src.keys()) {
            int value = src.get(key);
            dest.adjustOrPutValue(key, value, value);
        }
    }

    public static void rollUp(TObjectIntMap dest, TObjectIntMap src) {
        for (Object key : src.keySet()) {
            int value = src.get(key);
            dest.adjustOrPutValue(key, value, value);
        }
    }

    public static void rollUp(TObjectLongMap dest, TObjectLongMap src) {
        for (Object key : src.keySet()) {
            long value = src.get(key);
            dest.adjustOrPutValue(key, value, value);
        }
    }
}
