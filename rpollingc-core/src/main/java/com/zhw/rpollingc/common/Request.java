package com.zhw.rpollingc.common;

public interface Request<R, O> {

    String getService();

    Object getBody();

    O getOptions();

    int getTimeoutMs();

    void onErr(Throwable err);

    void onResp(R response);
}
