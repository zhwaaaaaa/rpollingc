package com.zhw.rpollingc.common;

public interface Request<R> {

    String getService();

    Object getBody();

    int getTimeoutMs();

    void onErr(Throwable err);

    void onResp(R response);
}
