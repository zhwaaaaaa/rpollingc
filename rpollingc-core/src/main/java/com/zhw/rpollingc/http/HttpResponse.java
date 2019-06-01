package com.zhw.rpollingc.http;

public interface HttpResponse<T> {

    int status();

    String header(String header);

    String header(String header, String defaultVal);

    T content();

}
