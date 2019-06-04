package com.zhw.rpollingc.http.protocol;

import com.zhw.rpollingc.http.HttpResponse;

import java.util.LinkedHashMap;

public class DefaultHttpResponse<T> implements HttpResponse<T> {
    private final LinkedHashMap<String, String> headers;
    private final int code;
    private final T content;
    private Object err;

    public DefaultHttpResponse(int code, LinkedHashMap<String, String> headers, T content) {
        this.headers = headers;
        this.code = code;
        this.content = content;
    }

    public void setErr(Object err) {
        this.err = err;
    }

    @Override
    public int status() {
        return code;
    }

    @Override
    public String header(String header) {
        return header(header, null);
    }

    @Override
    public String header(String header, String defaultVal) {
        return headers.getOrDefault(header, defaultVal);
    }

    @Override
    public T content() {
        return content;
    }

    @Override
    public Object err() {
        return err;
    }
}
