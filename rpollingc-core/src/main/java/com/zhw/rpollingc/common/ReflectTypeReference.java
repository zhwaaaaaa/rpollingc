package com.zhw.rpollingc.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class ReflectTypeReference<T> extends TypeReference {

    private final Type _type;

    protected ReflectTypeReference() {
        Type superClass = this.getClass().getGenericSuperclass();
        if (superClass instanceof Class) {
            throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
        } else {
            this._type = ((ParameterizedType)superClass).getActualTypeArguments()[0];
        }
    }

    @Override
    public Type getType() {
        return _type;
    }

}
