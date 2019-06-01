package com.zhw.rpollingc.http;

import com.zhw.rpollingc.common.RpcException;

public interface HttpClient {

    HttpResponse get(String url) throws RpcException;

    HttpResponse post(String url, Object body) throws RpcException;

}
