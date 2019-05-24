package com.zhw.rpollingc.http.conn;

import com.zhw.rpollingc.common.EndPoint;
import com.zhw.rpollingc.common.RpcException;

public interface HttpEndPoint extends EndPoint {

    void send(HttpRequest request) throws RpcException;

}
