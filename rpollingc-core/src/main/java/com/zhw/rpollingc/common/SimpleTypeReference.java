package com.zhw.rpollingc.common;

import java.lang.reflect.Type;

public class SimpleTypeReference extends TypeReference {
    private final Type type;

    public SimpleTypeReference(Type type) {
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }
}
