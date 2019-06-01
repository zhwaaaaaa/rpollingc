package com.zhw.rpollingc.common;

public interface PollingProtocol<REQ> {

    REQ createExecRequest(String service,
                          Object msg,
                          PollingOptions options) throws ProtocolException;



}
