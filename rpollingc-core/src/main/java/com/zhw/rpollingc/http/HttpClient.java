package com.zhw.rpollingc.http;

import com.zhw.rpollingc.common.RpcException;

public interface HttpClient<O> {

    HttpResponse get(String url, O options) throws RpcException;

    HttpResponse post(String url, Object body, O options) throws RpcException;

}
