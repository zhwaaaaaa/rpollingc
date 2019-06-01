package com.zhw.rpollingc.http.protocol;

public class Request {

    enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }

    private final Method method;

    public Request(Method method) {
        this.method = method;
    }

}
