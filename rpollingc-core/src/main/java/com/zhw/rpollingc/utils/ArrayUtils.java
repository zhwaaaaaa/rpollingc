package com.zhw.rpollingc.utils;

public class ArrayUtils {
    public static <T> boolean isEmpty(T[] arr) {
        return arr == null || arr.length == 0;
    }
}
