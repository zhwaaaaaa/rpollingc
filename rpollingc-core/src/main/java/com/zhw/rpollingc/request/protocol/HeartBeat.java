package com.zhw.rpollingc.request.protocol;

public final class HeartBeat {
    public static final int NOT_INIT = 100;
    private int status;

    public HeartBeat() {
    }

    public HeartBeat(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}