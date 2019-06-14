package com.zhw.rpollingc.common;

import java.lang.reflect.Type;

public abstract class TypeReference<T> {

    public static TypeReference from(Type type) {
        return new SimpleTypeReference(type);
    }

    public abstract Type getType();

}
