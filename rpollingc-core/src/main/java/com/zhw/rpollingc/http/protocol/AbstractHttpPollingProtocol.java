package com.zhw.rpollingc.http.protocol;

import com.zhw.rpollingc.common.PollingProtocol;
import com.zhw.rpollingc.common.TypeRefrence;
import com.zhw.rpollingc.http.conn.HttpRequest;

public abstract class AbstractHttpPollingProtocol implements PollingProtocol<HttpRequest> {


    @Override
    public HttpRequest createRequest(String service, Object msg, TypeRefrence<?> retType) {

        return null;
    }
}
