package com.zhw.rpollingc.http.protocol;

import com.zhw.rpollingc.common.TypeReference;

public class ReqOptions {
    public ReqOptions(TypeReference type) {
        this.type = type;
    }

    private TypeReference type;

    public TypeReference getType() {
        return type;
    }
}
